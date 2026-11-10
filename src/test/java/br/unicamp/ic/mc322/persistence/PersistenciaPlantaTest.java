package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.GeradorFixo;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.exception.PersistenciaException.Motivo;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.Maquina;
import br.unicamp.ic.mc322.model.Produto;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.service.PlantaPadrao;
import br.unicamp.ic.mc322.strategy.EstrategiaMaximoProdutos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Salvamento, carregamento e tratamento de falhas da {@link PersistenciaPlanta}.
 * <p>
 * Cada teste usa uma pasta temporária própria ({@code @TempDir}), criada e apagada pelo JUnit, de
 * modo que os testes nunca tocam o arquivo real da aplicação ({@code dados/planta.xml}) e não
 * dependem uns dos outros nem de uma execução anterior.
 */
class PersistenciaPlantaTest {

    @TempDir
    Path pasta;

    private Path arquivo;
    private PersistenciaPlanta persistencia;
    private GerenciadorProducao planta;

    @BeforeEach
    void preparar() {
        arquivo = pasta.resolve("planta.xml");
        persistencia = new PersistenciaPlanta(arquivo);
        planta = PlantaPadrao.criar(Cenario.APOCALIPTICO);
        semFalhas(planta);
    }

    private static void semFalhas(GerenciadorProducao g) {
        for (Maquina m : g.getMaquinas()) {
            m.definirGeradorAleatorio(new GeradorFixo(GeradorFixo.SEM_FALHAS));
        }
    }

    /** Leva a planta a um estado "usado": produção, compra, estratégia e demandas em vários status. */
    private void movimentarPlanta() {
        planta.registrarDemanda("Placa Simples", 1);
        planta.registrarDemanda("Placa Dupla", 3);
        planta.fabricarDemanda("Placa Simples");   // conclui a demanda de placa simples
        planta.fabricarDemanda("Placa Dupla");     // demanda de placa dupla fica em produção
        planta.comprarMateriaPrima("cera", 5.0);
        planta.setEstrategia(new EstrategiaMaximoProdutos());
        planta.getMaquinas().get(1).restaurarEstado(42.5, 27.0, 60.0, new double[]{12.0, 0.0});
    }

    @Test
    @DisplayName("Salvar cria um arquivo XML com a estrutura da planta")
    void salvarCriaArquivoXml() throws IOException {
        persistencia.salvar(planta);

        assertTrue(Files.exists(arquivo));
        String xml = Files.readString(arquivo);
        assertTrue(xml.startsWith("<?xml"));
        for (String elemento : List.of("<planta", "<materiasPrimas>", "<linhaDeProducao", "<maquina",
                "<componentes>", "<motor", "<sensorTemperatura", "<ventoinha ", "<demandas", "<armazem")) {
            assertTrue(xml.contains(elemento), "o XML deveria conter " + elemento);
        }
    }

    @Test
    @DisplayName("Salvar cria a pasta do arquivo quando ela ainda não existe")
    void salvarCriaPasta() {
        Path aninhado = pasta.resolve("dados").resolve("sub").resolve("planta.xml");
        new PersistenciaPlanta(aninhado).salvar(planta);
        assertTrue(Files.isRegularFile(aninhado));
    }

    @Test
    @DisplayName("Carregar recupera configuração, orçamento, contadores e estoque de matérias-primas")
    void preservaConfiguracaoEEstoque() {
        movimentarPlanta();
        persistencia.salvar(planta);

        GerenciadorProducao carregada = persistencia.carregar();

        assertEquals(Cenario.APOCALIPTICO, carregada.getCenario());
        assertEquals("Maximizar Produção", carregada.getEstrategiaAtual().getNomeEstrategia());
        assertEquals(planta.getBudget(), carregada.getBudget(), 1e-9);
        assertEquals(planta.getContadorPlacas(), carregada.getContadorPlacas());
        assertEquals(planta.getContadorLotes(), carregada.getContadorLotes());
        assertEquals(planta.getNomeLinha(), carregada.getNomeLinha());
        assertEquals(planta.getMateriaPrimaPrincipal().getQuantidade(), carregada.getMateriaPrimaPrincipal().getQuantidade(), 1e-9);
        assertEquals(planta.getMateriaPrimaEmbalagem().getQuantidade(), carregada.getMateriaPrimaEmbalagem().getQuantidade(), 1e-9);
        assertEquals(planta.getMateriaPrimaSelo().getQuantidade(), carregada.getMateriaPrimaSelo().getQuantidade(), 1e-9);
        assertEquals("MP3", carregada.getMateriaPrimaSelo().getId());
        assertEquals(planta.getMateriaPrimaPrincipal().getQuantidadeMinima(), carregada.getMateriaPrimaPrincipal().getQuantidadeMinima(), 1e-9);
    }

    @Test
    @DisplayName("Carregar recupera máquinas na ordem da linha, com configuração e estado das partes")
    void preservaMaquinasEComponentes() {
        movimentarPlanta();
        persistencia.salvar(planta);

        List<Maquina> originais = planta.getMaquinas();
        List<Maquina> recuperadas = persistencia.carregar().getMaquinas();

        assertEquals(originais.size(), recuperadas.size());
        for (int i = 0; i < originais.size(); i++) {
            Maquina o = originais.get(i);
            Maquina r = recuperadas.get(i);
            assertEquals(o.getClass(), r.getClass());
            assertEquals(o.getNome(), r.getNome());
            assertEquals(o.getCapacidadeMax(), r.getCapacidadeMax(), 1e-9);
            assertEquals(o.getCustoOperacao(), r.getCustoOperacao(), 1e-9);
            assertEquals(o.getCustoReparo(), r.getCustoReparo(), 1e-9);
            assertEquals(o.getSaude(), r.getSaude(), 1e-9);
            assertEquals(o.getSaudeMotor(), r.getSaudeMotor(), 1e-9);
            assertEquals(o.lerTemperaturaAtual(), r.lerTemperaturaAtual(), 1e-9);
            assertEquals(o.getQuantidadeVentoinhas(), r.getQuantidadeVentoinhas());
            for (int v = 0; v < o.getQuantidadeVentoinhas(); v++) {
                assertEquals(o.getSaudeVentoinha(v), r.getSaudeVentoinha(v), 1e-9);
            }
            // O cenário salvo volta a ser aplicado: a probabilidade de falha é a mesma.
            assertEquals(o.calcularProbabilidadeFalhaAtual(), r.calcularProbabilidadeFalhaAtual(), 1e-9);
        }
        Maquina embaladora = recuperadas.get(1);
        assertEquals(1, embaladora.contarVentoinhasOperantes());
        assertTrue(embaladora.precisaManutencao());
    }

    @Test
    @DisplayName("Carregar recupera as demandas com seus status e o armazém de placas")
    void preservaDemandasEArmazem() {
        movimentarPlanta();
        planta.registrarDemanda("Placa Multi-camada", 2);
        persistencia.salvar(planta);

        GerenciadorProducao carregada = persistencia.carregar();

        List<Demanda> demandas = carregada.getDemandas();
        assertEquals(3, demandas.size());
        assertEquals(StatusDemanda.CONCLUIDA, demandas.get(0).getStatus());
        assertEquals(StatusDemanda.EM_PRODUCAO, demandas.get(1).getStatus());
        assertEquals(2, demandas.get(1).getQuantidadeProdutos());
        assertEquals(StatusDemanda.PENDENTE, demandas.get(2).getStatus());
        assertEquals(planta.getDemandas().get(1).getCustoUnitarioEstimado(), demandas.get(1).getCustoUnitarioEstimado(), 1e-9);

        List<Produto> armazem = carregada.getProdutosFabricados();
        assertEquals(planta.getProdutosFabricados().size(), armazem.size());
        for (int i = 0; i < armazem.size(); i++) {
            Produto o = planta.getProdutosFabricados().get(i);
            Produto r = armazem.get(i);
            assertEquals(o.getId(), r.getId());
            assertEquals(o.getTipo(), r.getTipo());
            assertEquals(o.getLote(), r.getLote());
            assertEquals(o.getStatus(), r.getStatus());
            assertEquals(o.getQualidade(), r.getQualidade(), 1e-9);
            assertEquals(o.getMateriaPrimaUsada(), r.getMateriaPrimaUsada());
            assertEquals(o.getProbabilidadeFalhaAcumulada(), r.getProbabilidadeFalhaAcumulada(), 1e-9);
        }
    }

    @Test
    @DisplayName("Uma demanda cancelada continua cancelada após salvar e carregar")
    void preservaDemandaCancelada() {
        planta.registrarDemanda("Placa Dupla", 4);
        planta.restaurarEstado(5.0, 0, 0, List.copyOf(planta.getDemandas()), List.of());
        assertThrows(RuntimeException.class, () -> planta.fabricarDemanda("Placa Dupla"));
        persistencia.salvar(planta);

        Demanda recuperada = persistencia.carregar().getDemandas().get(0);
        assertEquals(StatusDemanda.CANCELADA, recuperada.getStatus());
        assertEquals(4, recuperada.getQuantidadeProdutos());
    }

    @Test
    @DisplayName("A planta carregada é independente da original e continua produzindo normalmente")
    void plantaCarregadaEIndependenteEFuncional() {
        movimentarPlanta();
        persistencia.salvar(planta);
        GerenciadorProducao carregada = persistencia.carregar();
        semFalhas(carregada);

        assertNotSame(planta, carregada);
        assertNotSame(planta.getMateriaPrimaPrincipal(), carregada.getMateriaPrimaPrincipal());

        double laminadoOriginal = planta.getMateriaPrimaPrincipal().getQuantidade();
        carregada.fabricarDemanda("Placa Dupla");
        assertEquals(laminadoOriginal, planta.getMateriaPrimaPrincipal().getQuantidade(), 1e-9);
        assertEquals(laminadoOriginal - 12.0, carregada.getMateriaPrimaPrincipal().getQuantidade(), 1e-9);
        // O próximo identificador continua a numeração salva, sem repetir placas.
        List<Produto> armazem = carregada.getProdutosFabricados();
        assertEquals("PCB-3", armazem.get(armazem.size() - 1).getId());
        assertEquals("LOTE-3", armazem.get(armazem.size() - 1).getLote());
    }

    @Test
    @DisplayName("Salvar novamente (atualizar) substitui o estado anterior por completo")
    void atualizarSubstituiEstado() {
        persistencia.salvar(planta);
        planta.registrarDemanda("Placa Simples", 2);
        planta.fabricarDemanda("Placa Simples");
        persistencia.salvar(planta);

        GerenciadorProducao carregada = persistencia.carregar();
        assertEquals(1, carregada.getDemandas().size());
        assertEquals(1, carregada.getDemandas().get(0).getQuantidadeProdutos());
        assertEquals(planta.getBudget(), carregada.getBudget(), 1e-9);
        assertEquals(1, carregada.getProdutosFabricados().size());
    }

    // ---------------------------------------------------------------- situações de erro

    @Test
    @DisplayName("Carregar sem arquivo salvo lança PersistenciaException com motivo ARQUIVO_INEXISTENTE")
    void arquivoInexistente() {
        assertFalse(persistencia.existeEstadoSalvo());
        PersistenciaException e = assertThrows(PersistenciaException.class, persistencia::carregar);
        assertEquals(Motivo.ARQUIVO_INEXISTENTE, e.getMotivo());
    }

    @Test
    @DisplayName("Arquivo vazio lança PersistenciaException com motivo ARQUIVO_VAZIO")
    void arquivoVazio() throws IOException {
        Files.writeString(arquivo, "   \n");
        PersistenciaException e = assertThrows(PersistenciaException.class, persistencia::carregar);
        assertEquals(Motivo.ARQUIVO_VAZIO, e.getMotivo());
    }

    @Test
    @DisplayName("XML corrompido (mal formado) lança PersistenciaException com motivo FORMATO_INVALIDO")
    void xmlCorrompido() throws IOException {
        persistencia.salvar(planta);
        String xml = Files.readString(arquivo);
        Files.writeString(arquivo, xml.substring(0, xml.length() / 2));

        PersistenciaException e = assertThrows(PersistenciaException.class, persistencia::carregar);
        assertEquals(Motivo.FORMATO_INVALIDO, e.getMotivo());
    }

    @Test
    @DisplayName("XML bem formado mas com estrutura diferente lança FORMATO_INVALIDO")
    void estruturaIncompativel() throws IOException {
        Files.writeString(arquivo, "<?xml version=\"1.0\"?><fabrica><maquinas/></fabrica>");
        assertEquals(Motivo.FORMATO_INVALIDO, assertThrows(PersistenciaException.class, persistencia::carregar).getMotivo());

        persistencia.salvar(planta);
        String semDemandas = Files.readString(arquivo).replaceAll("(?s)<demandas\\s*/>|<demandas>.*?</demandas>", "");
        Files.writeString(arquivo, semDemandas);
        assertEquals(Motivo.FORMATO_INVALIDO, assertThrows(PersistenciaException.class, persistencia::carregar).getMotivo());
    }

    @Test
    @DisplayName("Valores que violam as regras do domínio lançam DADOS_INVALIDOS")
    void dadosIncompativeisComODominio() throws IOException {
        persistencia.salvar(planta);
        String original = Files.readString(arquivo);

        String estoqueNegativo = original.replaceFirst("<quantidade>200\\.0</quantidade>", "<quantidade>-5.0</quantidade>");
        assertNotEquals(original, estoqueNegativo);
        Files.writeString(arquivo, estoqueNegativo);
        assertEquals(Motivo.DADOS_INVALIDOS, assertThrows(PersistenciaException.class, persistencia::carregar).getMotivo());

        String saudeInvalida = original.replaceFirst("<saude>100\\.0</saude>", "<saude>150.0</saude>");
        assertNotEquals(original, saudeInvalida);
        Files.writeString(arquivo, saudeInvalida);
        assertEquals(Motivo.DADOS_INVALIDOS, assertThrows(PersistenciaException.class, persistencia::carregar).getMotivo());

        String cenarioInexistente = original.replaceFirst("cenario=\"APOCALIPTICO\"", "cenario=\"LUNAR\"");
        Files.writeString(arquivo, cenarioInexistente);
        assertEquals(Motivo.DADOS_INVALIDOS, assertThrows(PersistenciaException.class, persistencia::carregar).getMotivo());

        String numeroInvalido = original.replaceFirst("<budget>[^<]*</budget>", "<budget>muito</budget>");
        Files.writeString(arquivo, numeroInvalido);
        assertEquals(Motivo.DADOS_INVALIDOS, assertThrows(PersistenciaException.class, persistencia::carregar).getMotivo());
    }

    @Test
    @DisplayName("Demanda concluída com unidades pendentes no arquivo é rejeitada como DADOS_INVALIDOS")
    void demandaIncoerenteNoArquivo() throws IOException {
        planta.registrarDemanda("Placa Simples", 1);
        planta.fabricarDemanda("Placa Simples");
        persistencia.salvar(planta);
        String xml = Files.readString(arquivo).replaceFirst("<quantidadePendente>0</quantidadePendente>",
                "<quantidadePendente>7</quantidadePendente>");
        Files.writeString(arquivo, xml);

        assertEquals(Motivo.DADOS_INVALIDOS, assertThrows(PersistenciaException.class, persistencia::carregar).getMotivo());
    }

    @Test
    @DisplayName("Falha de escrita lança FALHA_ESCRITA e preserva o estado salvo anteriormente")
    void falhaDeEscritaPreservaArquivoAnterior() throws IOException {
        // O caminho de destino é uma pasta com conteúdo: o arquivo não pode ser substituído.
        Path destinoInvalido = pasta.resolve("ocupado");
        Files.createDirectories(destinoInvalido);
        Files.writeString(destinoInvalido.resolve("outro.txt"), "conteúdo");
        PersistenciaException e = assertThrows(PersistenciaException.class,
                () -> new PersistenciaPlanta(destinoInvalido).salvar(planta));
        assertEquals(Motivo.FALHA_ESCRITA, e.getMotivo());

        // A pasta do arquivo é, na verdade, um arquivo comum: não é possível criá-la.
        Path arquivoComum = pasta.resolve("comum.txt");
        Files.writeString(arquivoComum, "x");
        PersistenciaException e2 = assertThrows(PersistenciaException.class,
                () -> new PersistenciaPlanta(arquivoComum.resolve("planta.xml")).salvar(planta));
        assertEquals(Motivo.FALHA_ESCRITA, e2.getMotivo());

        // Nenhum arquivo temporário fica para trás.
        try (var itens = Files.list(pasta)) {
            assertTrue(itens.noneMatch(p -> p.getFileName().toString().endsWith(".tmp")));
        }
    }

    @Test
    @DisplayName("Um carregamento que falha não altera a planta em uso")
    void falhaNoCarregamentoNaoAlteraPlantaAtual() throws IOException {
        planta.registrarDemanda("Placa Dupla", 2);
        double budgetAntes = planta.getBudget();
        Files.writeString(arquivo, "<planta versao=\"1\"><configuracao");

        assertThrows(PersistenciaException.class, persistencia::carregar);

        assertEquals(budgetAntes, planta.getBudget(), 1e-9);
        assertEquals(1, planta.getDemandas().size());
        assertEquals(2, planta.getDemandas().get(0).getQuantidadeProdutos());
    }
}
