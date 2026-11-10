package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.exception.PersistenciaException.Motivo;
import br.unicamp.ic.mc322.exception.PlantaException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Corrosora;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.Embaladora;
import br.unicamp.ic.mc322.model.EstacaoInspecao;
import br.unicamp.ic.mc322.model.FabricaDePlacas;
import br.unicamp.ic.mc322.model.LinhaDeProducao;
import br.unicamp.ic.mc322.model.Maquina;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.Produto;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.strategy.EstrategiaMaiorDemanda;
import br.unicamp.ic.mc322.strategy.EstrategiaMaximoProdutos;
import br.unicamp.ic.mc322.strategy.EstrategiaOrdemChegada;
import br.unicamp.ic.mc322.strategy.EstrategiaProducao;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Salva e recupera o estado da planta de PCBs em um arquivo XML.
 * <p>
 * <b>Separação de responsabilidades.</b> Esta é a única classe do projeto que conhece XML e
 * arquivos. As entidades de domínio não sabem que são persistidas: a classe lê o estado delas por
 * getters e, no carregamento, reconstrói os objetos pelos mesmos construtores e métodos de
 * restauração do domínio, que validam as invariantes (saúde entre 0 e 100, demanda concluída sem
 * unidades pendentes etc.).
 * <p>
 * <b>O que é salvo.</b> Configuração geral (cenário, estratégia, orçamento e contadores), estoque de
 * matérias-primas, a linha de produção com suas máquinas (configuração e estado) e as partes de
 * cada máquina (motor, sensor e ventoinhas), as demandas com seus status e as placas do armazém.
 * <p>
 * <b>Consistência.</b> O {@link #carregar()} monta uma planta <i>nova</i> e só a devolve se tudo for
 * lido e validado; quem chama substitui a planta atual por ela. Se algo falhar, uma
 * {@link PersistenciaException} é lançada e a planta em uso não é tocada. O {@link #salvar} grava
 * primeiro em um arquivo temporário e só então o move para o lugar do arquivo definitivo, para que
 * uma falha de escrita não corrompa um estado salvo anteriormente.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class PersistenciaPlanta {

    /** Versão do formato do arquivo, gravada no elemento raiz. */
    public static final String VERSAO_FORMATO = "1";

    private static final String TIPO_CORROSORA = "corrosora";
    private static final String TIPO_EMBALADORA = "embaladora";
    private static final String TIPO_INSPECAO = "estacaoInspecao";
    private static final String[] ORDEM_DAS_ETAPAS = {TIPO_CORROSORA, TIPO_EMBALADORA, TIPO_INSPECAO};

    private static final String ESTRATEGIA_ORDEM_CHEGADA = "ORDEM_CHEGADA";
    private static final String ESTRATEGIA_MAIOR_DEMANDA = "MAIOR_DEMANDA";
    private static final String ESTRATEGIA_MAXIMO_PRODUTOS = "MAXIMO_PRODUTOS";

    private static final String PAPEL_LAMINADO = "laminado";
    private static final String PAPEL_EMBALAGEM = "embalagem";
    private static final String PAPEL_SELO = "selo";

    private final Path arquivo;

    /**
     * Cria o módulo de persistência para o arquivo informado.
     *
     * @param arquivo caminho do arquivo XML (pode ainda não existir)
     * @throws PersistenciaException se o caminho for nulo
     */
    public PersistenciaPlanta(Path arquivo) {
        if (arquivo == null) {
            throw new PersistenciaException(Motivo.FALHA_ESCRITA, "O caminho do arquivo de persistência não foi informado.");
        }
        this.arquivo = arquivo;
    }

    /**
     * Retorna o caminho do arquivo de persistência.
     *
     * @return o caminho do arquivo
     */
    public Path getArquivo() {
        return arquivo;
    }

    /**
     * Indica se já existe um estado salvo no arquivo.
     *
     * @return {@code true} se o arquivo existe
     */
    public boolean existeEstadoSalvo() {
        return Files.isRegularFile(arquivo);
    }

    // ------------------------------------------------------------------------------------------
    // Salvar
    // ------------------------------------------------------------------------------------------

    /**
     * Grava o estado atual da planta no arquivo, substituindo um estado salvo anteriormente (é
     * também a operação de "atualizar"). A pasta do arquivo é criada se ainda não existir.
     *
     * @param planta gerenciador da planta a salvar
     * @throws PersistenciaException com motivo {@link Motivo#FALHA_ESCRITA} se não for possível gravar
     */
    public void salvar(GerenciadorProducao planta) {
        if (planta == null) {
            throw new PersistenciaException(Motivo.FALHA_ESCRITA, "Não há planta para salvar.");
        }
        Document doc = criarDocumento(planta);
        Path temporario = null;
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            if (pasta != null) {
                Files.createDirectories(pasta);
            }
            temporario = Files.createTempFile(pasta, "planta-", ".xml.tmp");
            try (OutputStream saida = Files.newOutputStream(temporario)) {
                Transformer t = TransformerFactory.newInstance().newTransformer();
                t.setOutputProperty(OutputKeys.INDENT, "yes");
                t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                t.transform(new DOMSource(doc), new StreamResult(saida));
            }
            moverSubstituindo(temporario, arquivo);
            temporario = null;
        } catch (IOException | TransformerException e) {
            throw new PersistenciaException(Motivo.FALHA_ESCRITA,
                    "Não foi possível salvar o estado da planta em " + arquivo + " (" + e.getMessage() + ").", e);
        } finally {
            if (temporario != null) {
                try {
                    Files.deleteIfExists(temporario);
                } catch (IOException ignorada) {
                    // O arquivo temporário é descartável; o erro principal já foi reportado.
                }
            }
        }
    }

    /**
     * Move o arquivo temporário para o destino, de forma atômica quando o sistema permitir.
     *
     * @param origem  arquivo temporário já gravado
     * @param destino arquivo definitivo
     * @throws IOException se a movimentação falhar
     */
    private static void moverSubstituindo(Path origem, Path destino) throws IOException {
        try {
            Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Monta o documento XML com o estado da planta.
     *
     * @param planta gerenciador da planta
     * @return o documento pronto para ser gravado
     */
    private Document criarDocumento(GerenciadorProducao planta) {
        Document doc = novoConstrutor().newDocument();
        Element raiz = doc.createElement("planta");
        raiz.setAttribute("versao", VERSAO_FORMATO);
        doc.appendChild(raiz);

        Element config = filho(doc, raiz, "configuracao");
        config.setAttribute("cenario", planta.getCenario().name());
        config.setAttribute("estrategia", chaveEstrategia(planta.getEstrategiaAtual()));
        texto(doc, config, "budget", planta.getBudget());
        texto(doc, config, "contadorPlacas", planta.getContadorPlacas());
        texto(doc, config, "contadorLotes", planta.getContadorLotes());

        Element mps = filho(doc, raiz, "materiasPrimas");
        escreverMateriaPrima(doc, mps, PAPEL_LAMINADO, planta.getMateriaPrimaPrincipal());
        escreverMateriaPrima(doc, mps, PAPEL_EMBALAGEM, planta.getMateriaPrimaEmbalagem());
        escreverMateriaPrima(doc, mps, PAPEL_SELO, planta.getMateriaPrimaSelo());

        Element linha = filho(doc, raiz, "linhaDeProducao");
        linha.setAttribute("nome", planta.getNomeLinha());
        int ordem = 1;
        for (Maquina m : planta.getMaquinas()) {
            escreverMaquina(doc, linha, m, ordem++);
        }

        Element demandas = filho(doc, raiz, "demandas");
        for (Demanda d : planta.getDemandas()) {
            Element e = filho(doc, demandas, "demanda");
            e.setAttribute("tipoProduto", d.getTipoProduto());
            e.setAttribute("status", d.getStatus().name());
            texto(doc, e, "quantidadePendente", d.getQuantidadeProdutos());
            texto(doc, e, "custoUnitarioEstimado", d.getCustoUnitarioEstimado());
        }

        Element armazem = filho(doc, raiz, "armazem");
        for (Produto p : planta.getProdutosFabricados()) {
            Element e = filho(doc, armazem, "placa");
            e.setAttribute("id", p.getId());
            e.setAttribute("tipo", p.getTipo());
            texto(doc, e, "lote", p.getLote());
            texto(doc, e, "status", p.getStatus());
            texto(doc, e, "materiaPrimaUsada", p.getMateriaPrimaUsada() == null ? "" : p.getMateriaPrimaUsada());
            texto(doc, e, "riscoAcumulado", p.getProbabilidadeFalhaAcumulada());
        }
        return doc;
    }

    /**
     * Escreve uma matéria-prima.
     *
     * @param doc   documento
     * @param pai   elemento {@code materiasPrimas}
     * @param papel etapa que consome a matéria-prima
     * @param mp    matéria-prima
     */
    private void escreverMateriaPrima(Document doc, Element pai, String papel, MateriaPrima mp) {
        Element e = filho(doc, pai, "materiaPrima");
        e.setAttribute("id", mp.getId());
        e.setAttribute("papel", papel);
        texto(doc, e, "nome", mp.getNome());
        texto(doc, e, "quantidade", mp.getQuantidade());
        texto(doc, e, "unidade", mp.getUnidade());
        texto(doc, e, "quantidadeMinima", mp.getQuantidadeMinima());
        texto(doc, e, "custoPorUnidade", mp.getCustoPorUnidade());
    }

    /**
     * Escreve uma máquina, com sua configuração, seu estado e suas partes (composição).
     *
     * @param doc   documento
     * @param pai   elemento {@code linhaDeProducao}
     * @param m     máquina
     * @param ordem posição da máquina na linha, começando em 1
     */
    private void escreverMaquina(Document doc, Element pai, Maquina m, int ordem) {
        Element e = filho(doc, pai, "maquina");
        e.setAttribute("ordem", String.valueOf(ordem));
        e.setAttribute("tipo", chaveTipo(m));
        texto(doc, e, "nome", m.getNome());

        Element conf = filho(doc, e, "configuracao");
        texto(doc, conf, "capacidadeMax", m.getCapacidadeMax());
        texto(doc, conf, "custoOperacao", m.getCustoOperacao());
        if (m instanceof Corrosora c) {
            texto(doc, conf, "chanceAumentarFalha", c.getChanceAumentarFalha());
        } else if (m instanceof Embaladora emb) {
            texto(doc, conf, "chanceAumentarFalha", emb.getChanceAumentarFalha());
            texto(doc, conf, "esdPorEmbalagem", emb.getQuantidadeEsdBagPorEmbalagem());
        } else if (m instanceof EstacaoInspecao insp) {
            texto(doc, conf, "probabilidadeFalha", insp.getProbabilidadeFalhaBase());
            texto(doc, conf, "fatorRigor", insp.getFator());
            texto(doc, conf, "seloPorInspecao", insp.getQuantidadeSeloPorInspecao());
        }

        Element estado = filho(doc, e, "estado");
        texto(doc, estado, "saude", m.getSaude());

        Element comp = filho(doc, e, "componentes");
        Element motor = filho(doc, comp, "motor");
        motor.setAttribute("potenciaKw", String.valueOf(m.getPotenciaMotor()));
        motor.setAttribute("saude", String.valueOf(m.getSaudeMotor()));
        Element sensor = filho(doc, comp, "sensorTemperatura");
        sensor.setAttribute("temperaturaMaxima", String.valueOf(m.getTemperaturaMaxima()));
        sensor.setAttribute("temperaturaAtual", String.valueOf(m.lerTemperaturaAtual()));
        Element vents = filho(doc, comp, "ventoinhas");
        for (int i = 0; i < m.getQuantidadeVentoinhas(); i++) {
            Element v = filho(doc, vents, "ventoinha");
            v.setAttribute("numero", String.valueOf(i + 1));
            v.setAttribute("saude", String.valueOf(m.getSaudeVentoinha(i)));
        }
    }

    // ------------------------------------------------------------------------------------------
    // Carregar
    // ------------------------------------------------------------------------------------------

    /**
     * Lê o arquivo e reconstrói uma planta completa a partir dele. A planta atual da aplicação não
     * é alterada por este método: se a leitura for bem-sucedida, quem chama passa a usar a planta
     * devolvida (substituição completa, sem mistura de dados).
     *
     * @return o gerenciador da planta recuperada
     * @throws PersistenciaException com o motivo específico: arquivo inexistente, vazio, formato
     *                               inválido, dados incompatíveis com o domínio ou falha de leitura
     */
    public GerenciadorProducao carregar() {
        if (!Files.exists(arquivo)) {
            throw new PersistenciaException(Motivo.ARQUIVO_INEXISTENTE,
                    "Não existe estado salvo em " + arquivo + ".");
        }
        Document doc = lerDocumento();
        try {
            return reconstruir(doc.getDocumentElement());
        } catch (PersistenciaException e) {
            throw e;
        } catch (NumberFormatException e) {
            throw new PersistenciaException(Motivo.DADOS_INVALIDOS,
                    "O arquivo de persistência contém um número inválido (" + e.getMessage() + ").", e);
        } catch (PlantaException | IllegalArgumentException e) {
            // Regras do domínio violadas (saúde fora do intervalo, estoque negativo, status incoerente...).
            throw new PersistenciaException(Motivo.DADOS_INVALIDOS,
                    "Os dados salvos são incompatíveis com as regras da planta: " + e.getMessage(), e);
        }
    }

    /**
     * Lê e interpreta o arquivo XML.
     *
     * @return o documento lido
     * @throws PersistenciaException se o arquivo está vazio, não é XML válido ou não pôde ser lido
     */
    private Document lerDocumento() {
        try {
            if (!Files.isRegularFile(arquivo) || !Files.isReadable(arquivo)) {
                throw new PersistenciaException(Motivo.FALHA_LEITURA,
                        "O arquivo " + arquivo + " não pode ser lido (não é um arquivo comum ou falta permissão).");
            }
            if (Files.size(arquivo) == 0 || Files.readString(arquivo).isBlank()) {
                throw new PersistenciaException(Motivo.ARQUIVO_VAZIO,
                        "O arquivo de persistência " + arquivo + " está vazio.");
            }
            DocumentBuilder construtor = novoConstrutor();
            // Sem este tratador, o leitor imprime "[Fatal Error]" no terminal antes da exceção.
            construtor.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
                @Override
                public void fatalError(org.xml.sax.SAXParseException e) throws SAXException {
                    throw e;
                }
            });
            Document doc = construtor.parse(arquivo.toFile());
            doc.getDocumentElement().normalize();
            return doc;
        } catch (SAXException e) {
            throw new PersistenciaException(Motivo.FORMATO_INVALIDO,
                    "O arquivo de persistência está corrompido ou não é um XML válido.", e);
        } catch (IOException e) {
            throw new PersistenciaException(Motivo.FALHA_LEITURA,
                    "Não foi possível ler o arquivo " + arquivo + " (" + e.getMessage() + ").", e);
        }
    }

    /**
     * Reconstrói a planta a partir do elemento raiz, validando estrutura e valores.
     *
     * @param raiz elemento {@code planta}
     * @return a planta reconstruída
     */
    private GerenciadorProducao reconstruir(Element raiz) {
        if (!"planta".equals(raiz.getTagName())) {
            throw formato("o elemento raiz deve ser <planta>, mas é <" + raiz.getTagName() + ">");
        }
        if (!VERSAO_FORMATO.equals(raiz.getAttribute("versao"))) {
            throw formato("versão do formato não suportada: '" + raiz.getAttribute("versao") + "'");
        }

        Element config = unico(raiz, "configuracao");
        Cenario cenario = lerCenario(atributo(config, "cenario"));
        EstrategiaProducao estrategia = lerEstrategia(atributo(config, "estrategia"));
        double budget = numero(config, "budget");
        int contadorPlacas = inteiro(config, "contadorPlacas");
        int contadorLotes = inteiro(config, "contadorLotes");

        MateriaPrima laminado = null;
        MateriaPrima embalagem = null;
        MateriaPrima selo = null;
        for (Element e : filhos(unico(raiz, "materiasPrimas"), "materiaPrima")) {
            MateriaPrima mp = new MateriaPrima(atributo(e, "id"), textoDe(e, "nome"), numero(e, "quantidade"),
                    textoDe(e, "unidade"), numero(e, "quantidadeMinima"), numero(e, "custoPorUnidade"));
            String papel = atributo(e, "papel");
            switch (papel) {
                case PAPEL_LAMINADO -> laminado = exigirUnica(laminado, mp, papel);
                case PAPEL_EMBALAGEM -> embalagem = exigirUnica(embalagem, mp, papel);
                case PAPEL_SELO -> selo = exigirUnica(selo, mp, papel);
                default -> throw formato("papel de matéria-prima desconhecido: '" + papel + "'");
            }
        }
        if (laminado == null || embalagem == null || selo == null) {
            throw formato("faltam matérias-primas: são necessárias as de laminado, embalagem e selo");
        }

        Element elementoLinha = unico(raiz, "linhaDeProducao");
        LinhaDeProducao linha = new LinhaDeProducao(atributo(elementoLinha, "nome"));
        List<Element> maquinas = filhos(elementoLinha, "maquina");
        if (maquinas.size() != ORDEM_DAS_ETAPAS.length) {
            throw formato("a linha deve ter " + ORDEM_DAS_ETAPAS.length + " máquinas, mas tem " + maquinas.size());
        }
        for (int i = 0; i < maquinas.size(); i++) {
            Element e = maquinas.get(i);
            if (!ORDEM_DAS_ETAPAS[i].equals(e.getAttribute("tipo"))) {
                throw formato("a etapa " + (i + 1) + " da linha deve ser '" + ORDEM_DAS_ETAPAS[i]
                        + "', mas é '" + e.getAttribute("tipo") + "'");
            }
            linha.adicionarMaquina(lerMaquina(e));
        }

        List<Demanda> demandas = new ArrayList<>();
        for (Element e : filhos(unico(raiz, "demandas"), "demanda")) {
            String tipo = atributo(e, "tipoProduto");
            if (!FabricaDePlacas.ehTipoConhecido(tipo)) {
                throw dados("tipo de produto desconhecido na demanda: '" + tipo + "'");
            }
            demandas.add(Demanda.restaurar(tipo, inteiro(e, "quantidadePendente"),
                    numero(e, "custoUnitarioEstimado"), lerStatus(atributo(e, "status"))));
        }

        List<Produto> produtos = new ArrayList<>();
        for (Element e : filhos(unico(raiz, "armazem"), "placa")) {
            produtos.add(lerPlaca(e));
        }

        // O construtor aplica o cenário às máquinas; em seguida o estado dinâmico é restaurado.
        GerenciadorProducao planta = new GerenciadorProducao(laminado, embalagem, selo, linha, cenario, estrategia);
        planta.restaurarEstado(budget, contadorPlacas, contadorLotes, demandas, produtos);
        return planta;
    }

    /**
     * Reconstrói uma máquina pelo construtor da subclasse e restaura o estado dela e de suas partes.
     *
     * @param e elemento {@code maquina}
     * @return a máquina reconstruída
     */
    private Maquina lerMaquina(Element e) {
        String nome = textoDe(e, "nome");
        Element conf = unico(e, "configuracao");
        Element comp = unico(e, "componentes");
        Element motor = unico(comp, "motor");
        Element sensor = unico(comp, "sensorTemperatura");
        List<Element> vents = filhos(unico(comp, "ventoinhas"), "ventoinha");

        double capacidade = numero(conf, "capacidadeMax");
        double custo = numero(conf, "custoOperacao");
        Maquina m = switch (e.getAttribute("tipo")) {
            case TIPO_CORROSORA -> new Corrosora(nome, capacidade, custo, numero(conf, "chanceAumentarFalha"), vents.size());
            case TIPO_EMBALADORA -> new Embaladora(nome, capacidade, custo, numero(conf, "chanceAumentarFalha"),
                    numero(conf, "esdPorEmbalagem"), vents.size());
            case TIPO_INSPECAO -> new EstacaoInspecao(nome, capacidade, custo, numero(conf, "probabilidadeFalha"),
                    numero(conf, "fatorRigor"), numero(conf, "seloPorInspecao"), vents.size());
            default -> throw formato("tipo de máquina desconhecido: '" + e.getAttribute("tipo") + "'");
        };

        // Potência do motor e temperatura máxima são fixas por tipo de máquina: o arquivo precisa concordar.
        exigirIgual(m.getPotenciaMotor(), Double.parseDouble(atributo(motor, "potenciaKw")), "potência do motor da " + nome);
        exigirIgual(m.getTemperaturaMaxima(), Double.parseDouble(atributo(sensor, "temperaturaMaxima")),
                "temperatura máxima do sensor da " + nome);

        double[] saudeVentoinhas = new double[vents.size()];
        for (int i = 0; i < vents.size(); i++) {
            if (!String.valueOf(i + 1).equals(vents.get(i).getAttribute("numero"))) {
                throw formato("as ventoinhas da " + nome + " devem estar numeradas em sequência a partir de 1");
            }
            saudeVentoinhas[i] = Double.parseDouble(atributo(vents.get(i), "saude"));
        }
        m.restaurarEstado(numero(unico(e, "estado"), "saude"), Double.parseDouble(atributo(motor, "saude")),
                Double.parseDouble(atributo(sensor, "temperaturaAtual")), saudeVentoinhas);
        return m;
    }

    /**
     * Reconstrói uma placa do armazém.
     *
     * @param e elemento {@code placa}
     * @return a placa reconstruída
     */
    private Produto lerPlaca(Element e) {
        String tipo = atributo(e, "tipo");
        if (!FabricaDePlacas.ehTipoConhecido(tipo)) {
            throw dados("tipo de placa desconhecido no armazém: '" + tipo + "'");
        }
        Produto p = FabricaDePlacas.criar(tipo, atributo(e, "id"));
        double risco = numero(e, "riscoAcumulado");
        if (!(risco >= 0) || Double.isInfinite(risco)) {
            throw dados("o risco acumulado da placa " + p.getId() + " deve ser um número não negativo");
        }
        p.setLote(textoDe(e, "lote"));
        p.setStatus(textoDe(e, "status"));
        String mpUsada = textoOpcional(e, "materiaPrimaUsada");
        p.setMateriaPrimaUsada(mpUsada.isEmpty() ? null : mpUsada);
        p.aumentarProbabilidadeFalha(risco);
        return p;
    }

    // ------------------------------------------------------------------------------------------
    // Conversões entre o domínio e os valores do XML
    // ------------------------------------------------------------------------------------------

    /**
     * Converte a estratégia em sua chave no arquivo.
     *
     * @param e estratégia
     * @return a chave da estratégia
     */
    private static String chaveEstrategia(EstrategiaProducao e) {
        if (e instanceof EstrategiaMaiorDemanda) return ESTRATEGIA_MAIOR_DEMANDA;
        if (e instanceof EstrategiaMaximoProdutos) return ESTRATEGIA_MAXIMO_PRODUTOS;
        return ESTRATEGIA_ORDEM_CHEGADA;
    }

    /**
     * Converte a chave do arquivo na estratégia correspondente.
     *
     * @param chave chave lida
     * @return uma nova instância da estratégia
     */
    private static EstrategiaProducao lerEstrategia(String chave) {
        return switch (chave) {
            case ESTRATEGIA_ORDEM_CHEGADA -> new EstrategiaOrdemChegada();
            case ESTRATEGIA_MAIOR_DEMANDA -> new EstrategiaMaiorDemanda();
            case ESTRATEGIA_MAXIMO_PRODUTOS -> new EstrategiaMaximoProdutos();
            default -> throw dados("estratégia de produção desconhecida: '" + chave + "'");
        };
    }

    /**
     * Converte a máquina em sua chave de tipo no arquivo.
     *
     * @param m máquina
     * @return a chave do tipo
     */
    private static String chaveTipo(Maquina m) {
        if (m instanceof Corrosora) return TIPO_CORROSORA;
        if (m instanceof Embaladora) return TIPO_EMBALADORA;
        if (m instanceof EstacaoInspecao) return TIPO_INSPECAO;
        throw new PersistenciaException(Motivo.FALHA_ESCRITA, "Tipo de máquina sem formato de persistência: " + m.getTipo());
    }

    /**
     * Converte o nome lido no cenário correspondente.
     *
     * @param nome nome da constante do enum
     * @return o cenário
     */
    private static Cenario lerCenario(String nome) {
        try {
            return Cenario.valueOf(nome);
        } catch (IllegalArgumentException e) {
            throw dados("cenário desconhecido: '" + nome + "'");
        }
    }

    /**
     * Converte o nome lido no status de demanda correspondente.
     *
     * @param nome nome da constante do enum
     * @return o status
     */
    private static StatusDemanda lerStatus(String nome) {
        try {
            return StatusDemanda.valueOf(nome);
        } catch (IllegalArgumentException e) {
            throw dados("status de demanda desconhecido: '" + nome + "'");
        }
    }

    /**
     * Exige que um valor fixo do tipo de máquina coincida com o valor salvo.
     *
     * @param esperado  valor definido pela classe da máquina
     * @param lido      valor salvo no arquivo
     * @param descricao descrição usada na mensagem de erro
     */
    private static void exigirIgual(double esperado, double lido, String descricao) {
        if (Math.abs(esperado - lido) > 1e-9) {
            throw dados("a " + descricao + " salva (" + lido + ") não corresponde à do modelo (" + esperado + ")");
        }
    }

    /**
     * Garante que cada papel de matéria-prima apareça uma única vez.
     *
     * @param atual valor já lido para o papel (ou {@code null})
     * @param nova  matéria-prima recém-lida
     * @param papel papel da matéria-prima
     * @return a matéria-prima recém-lida
     */
    private static MateriaPrima exigirUnica(MateriaPrima atual, MateriaPrima nova, String papel) {
        if (atual != null) {
            throw formato("há mais de uma matéria-prima com o papel '" + papel + "'");
        }
        return nova;
    }

    // ------------------------------------------------------------------------------------------
    // Utilitários de XML (reaproveitados por todas as leituras e escritas)
    // ------------------------------------------------------------------------------------------

    /**
     * Cria um construtor de documentos seguro (sem DTD nem entidades externas).
     *
     * @return o construtor de documentos
     */
    private static DocumentBuilder novoConstrutor() {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setXIncludeAware(false);
            f.setExpandEntityReferences(false);
            return f.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new PersistenciaException(Motivo.FALHA_LEITURA, "O leitor de XML não pôde ser configurado.", e);
        }
    }

    private static Element filho(Document doc, Element pai, String nome) {
        Element e = doc.createElement(nome);
        pai.appendChild(e);
        return e;
    }

    private static void texto(Document doc, Element pai, String nome, Object valor) {
        filho(doc, pai, nome).setTextContent(String.valueOf(valor));
    }

    /**
     * Lista os elementos filhos diretos com o nome informado.
     *
     * @param pai  elemento pai
     * @param nome nome dos filhos
     * @return os filhos, na ordem do documento
     */
    private static List<Element> filhos(Element pai, String nome) {
        List<Element> lista = new ArrayList<>();
        NodeList nos = pai.getChildNodes();
        for (int i = 0; i < nos.getLength(); i++) {
            Node n = nos.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && nome.equals(n.getNodeName())) {
                lista.add((Element) n);
            }
        }
        return lista;
    }

    /**
     * Retorna o único filho direto com o nome informado.
     *
     * @param pai  elemento pai
     * @param nome nome do filho
     * @return o elemento filho
     * @throws PersistenciaException se o filho não existe ou aparece mais de uma vez
     */
    private static Element unico(Element pai, String nome) {
        List<Element> lista = filhos(pai, nome);
        if (lista.size() != 1) {
            throw formato(lista.isEmpty()
                    ? "falta o elemento <" + nome + "> dentro de <" + pai.getTagName() + ">"
                    : "o elemento <" + nome + "> aparece mais de uma vez dentro de <" + pai.getTagName() + ">");
        }
        return lista.get(0);
    }

    private static String textoDe(Element pai, String nome) {
        String valor = unico(pai, nome).getTextContent().trim();
        if (valor.isEmpty()) {
            throw formato("o elemento <" + nome + "> dentro de <" + pai.getTagName() + "> está vazio");
        }
        return valor;
    }

    private static String textoOpcional(Element pai, String nome) {
        List<Element> lista = filhos(pai, nome);
        return lista.isEmpty() ? "" : lista.get(0).getTextContent().trim();
    }

    private static String atributo(Element e, String nome) {
        if (!e.hasAttribute(nome) || e.getAttribute(nome).isBlank()) {
            throw formato("falta o atributo '" + nome + "' em <" + e.getTagName() + ">");
        }
        return e.getAttribute(nome).trim();
    }

    private static double numero(Element pai, String nome) {
        double valor = Double.parseDouble(textoDe(pai, nome));
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw dados("o valor de <" + nome + "> deve ser um número finito");
        }
        return valor;
    }

    private static int inteiro(Element pai, String nome) {
        return Integer.parseInt(textoDe(pai, nome));
    }

    private static PersistenciaException formato(String detalhe) {
        return new PersistenciaException(Motivo.FORMATO_INVALIDO,
                "O arquivo de persistência não tem a estrutura esperada: " + detalhe + ".");
    }

    private static PersistenciaException dados(String detalhe) {
        return new PersistenciaException(Motivo.DADOS_INVALIDOS,
                "Os dados salvos são incompatíveis com as regras da planta: " + detalhe + ".");
    }
}
