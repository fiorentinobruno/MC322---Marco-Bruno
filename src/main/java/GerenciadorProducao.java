import java.util.ArrayList;
import java.util.List;

/**
 * Coordena a planta industrial: demandas, linha de produção, estoque de matéria-prima, orçamento,
 * estratégia de produção e auditoria.
 * <p>
 * <b>Estrutura.</b> O gerenciador conhece apenas a {@link LinhaDeProducao} (o Agrupador). Ele não
 * guarda referências às máquinas nem às suas partes: para produzir, consultar custos, reparar ou
 * auditar, pede à linha, que delega às máquinas, que por sua vez delegam às suas partes
 * (Lei de Deméter).
 * <p>
 * <b>Tratamento de erros e consistência de estado.</b> As operações públicas que podem falhar
 * lançam subclasses de {@link PlantaException}; quem interage com o usuário ({@link Main}) as
 * captura. Uma produção que falha não deixa estoque, orçamento nem demanda parcialmente
 * modificados: todas as verificações de recurso são feitas antes de qualquer consumo e, se algo
 * falhar durante a linha (por exemplo, uma falha técnica aleatória), o estoque é restaurado ao
 * valor anterior e o orçamento só é debitado depois que a linha termina. O desgaste das máquinas
 * e de suas partes que chegaram a operar é mantido, pois representa uso físico real.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class GerenciadorProducao {

    private static final String AVISO_SEM_ALTERACAO =
            "Nenhuma alteração foi realizada no estoque nem no orçamento.";
    private static final int ETAPAS_DA_LINHA = 3;

    private static int contadorPlacas = 0;
    private int contadorLotes = 0;

    private List<Demanda> demandas;
    private List<Produto> produtosFabricados;

    private MateriaPrima materiaPrimaPrincipal;
    private MateriaPrima materiaPrimaEmbalagem;
    private MateriaPrima materiaPrimaSelo;

    private final LinhaDeProducao linha;

    private double budget;
    private final Cenario cenario;
    private EstrategiaProducao estrategiaAtual;

    /**
     * Monta a planta e aplica o cenário a todas as máquinas da linha.
     *
     * @param materiaPrimaPrincipal laminado consumido pela primeira etapa (corrosão)
     * @param materiaPrimaEmbalagem sacos antiestáticos consumidos pela segunda etapa (embalagem)
     * @param materiaPrimaSelo      selos de cera consumidos pela terceira etapa (inspeção)
     * @param linha                 linha de produção com as três etapas, na ordem corrosão, embalagem e inspeção
     * @param cenario               cenário de operação, que define o orçamento inicial
     * @param estrategiaInicial     estratégia de produção inicial
     * @throws EntradaInvalidaException se a linha for nula ou não tiver exatamente três máquinas
     */
    public GerenciadorProducao(MateriaPrima materiaPrimaPrincipal, MateriaPrima materiaPrimaEmbalagem,
                               MateriaPrima materiaPrimaSelo, LinhaDeProducao linha, Cenario cenario,
                               EstrategiaProducao estrategiaInicial) {
        if (linha == null || linha.getQuantidadeMaquinas() != ETAPAS_DA_LINHA) {
            throw new EntradaInvalidaException("A linha de produção deve ter exatamente " + ETAPAS_DA_LINHA
                    + " máquinas: corrosão, embalagem e inspeção.");
        }
        this.demandas = new ArrayList<>();
        this.produtosFabricados = new ArrayList<>();

        this.materiaPrimaPrincipal = materiaPrimaPrincipal;
        this.materiaPrimaEmbalagem = materiaPrimaEmbalagem;
        this.materiaPrimaSelo = materiaPrimaSelo;

        this.linha = linha;

        this.cenario = cenario;
        this.budget = cenario.getBudgetInicial();
        this.estrategiaAtual = estrategiaInicial;

        linha.aplicarCenario(cenario);
    }

    /**
     * Troca a estratégia de produção em tempo de execução.
     *
     * @param nova nova estratégia
     * @throws EntradaInvalidaException se a estratégia for nula
     */
    public void setEstrategia(EstrategiaProducao nova) {
        if (nova == null) {
            throw new EntradaInvalidaException("Estratégia de produção inválida.");
        }
        this.estrategiaAtual = nova;
        System.out.println("Estratégia alterada para: " + nova.getNomeEstrategia());
    }

    /**
     * Retorna a estratégia de produção em uso.
     *
     * @return a estratégia atual
     */
    public EstrategiaProducao getEstrategiaAtual() {
        return estrategiaAtual;
    }

    /**
     * Retorna o cenário de operação.
     *
     * @return o cenário ativo
     */
    public Cenario getCenario() {
        return cenario;
    }

    /**
     * Retorna o nome da linha de produção.
     *
     * @return o nome da linha
     */
    public String getNomeLinha() {
        return linha.getNome();
    }

    /**
     * Fabrica uma unidade da demanda escolhida pela estratégia atual.
     *
     * @throws DemandaInvalidaException       se nenhuma demanda é elegível para a estratégia
     * @throws OrcamentoInsuficienteException se o orçamento não cobre uma produção (a demanda é cancelada)
     * @throws RecursoInsuficienteException   se falta matéria-prima em alguma etapa
     * @throws MaquinaIndisponivelException   se alguma máquina está quebrada, superaquecida, desligada ou falha
     */
    public void executarProximaProducao() {
        Demanda d = estrategiaAtual.selecionarDemanda(demandas, budget);
        if (d == null) {
            throw new DemandaInvalidaException("Nenhuma demanda elegível para a estratégia "
                    + estrategiaAtual.getNomeEstrategia()
                    + ". Registre uma demanda ou verifique se o orçamento cobre o custo de produção.");
        }
        System.out.println("Estratégia [" + estrategiaAtual.getNomeEstrategia() + "] escolheu: " + d.getTipoProduto());
        fabricarUmaUnidade(d);
    }

    /**
     * Registra uma nova demanda ou acrescenta unidades a uma demanda existente do mesmo tipo.
     *
     * @param tipoProduto tipo de produto (ex.: {@code "Placa Simples"})
     * @param quantidade  unidades a registrar; deve ser maior que zero
     * @throws EntradaInvalidaException se a quantidade não for positiva
     * @throws DemandaInvalidaException se o tipo é desconhecido ou a demanda existente foi cancelada
     */
    public void registrarDemanda(String tipoProduto, int quantidade) {
        if (quantidade <= 0) {
            throw new EntradaInvalidaException("A quantidade precisa ser maior que zero.");
        }
        if (!ehTipoConhecido(tipoProduto)) {
            throw new DemandaInvalidaException("Tipo de produto desconhecido: " + tipoProduto);
        }
        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda == null) {
            demandas.add(new Demanda(tipoProduto, quantidade, consultarCustoOperacao()));
            System.out.println("Demanda registrada: " + quantidade + " unidade(s) de " + tipoProduto);
            return;
        }
        demanda.atualizarQuantidade(quantidade);
        System.out.println("Demanda atualizada: " + tipoProduto + " agora com "
                + demanda.getQuantidadeProdutos() + " unidade(s) pendente(s)");
    }

    /**
     * Exibe todas as demandas com quantidade pendente e status.
     */
    public void listarDemandas() {
        System.out.println("\n*** DEMANDAS ***");
        if (demandas.isEmpty()) {
            System.out.println("Nenhuma demanda registrada.");
            return;
        }
        for (Demanda d : demandas) {
            System.out.printf(" - %s | Pendente: %d | Status: %s%n",
                    d.getTipoProduto(), d.getQuantidadeProdutos(), d.getStatus().getDescricao());
        }
    }

    /**
     * Fabrica uma unidade da demanda do tipo informado.
     *
     * @param tipoProduto tipo de produto a fabricar
     * @throws DemandaInvalidaException       se não há demanda pendente do tipo, ou ela foi cancelada
     * @throws OrcamentoInsuficienteException se o orçamento não cobre uma produção (a demanda é cancelada)
     * @throws RecursoInsuficienteException   se falta matéria-prima em alguma etapa
     * @throws MaquinaIndisponivelException   se alguma máquina está quebrada, superaquecida, desligada ou falha
     */
    public void fabricarDemanda(String tipoProduto) {
        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda == null) {
            throw new DemandaInvalidaException("Não existe demanda registrada para " + tipoProduto
                    + ". Registre uma no menu 'Demandas'.");
        }
        if (demanda.getStatus() == StatusDemanda.CANCELADA) {
            throw new DemandaInvalidaException("A demanda de " + tipoProduto + " foi cancelada e não pode ser produzida.");
        }
        if (demanda.getQuantidadeProdutos() <= 0) {
            throw new DemandaInvalidaException("Não há unidades pendentes de " + tipoProduto + ".");
        }
        fabricarUmaUnidade(demanda);
    }

    /**
     * Executa a linha completa para uma unidade da demanda.
     * <p>
     * Ordem das operações, pensada para preservar a consistência:
     * <ol>
     *   <li>confere o orçamento (se faltar, a demanda é cancelada, conforme a regra de negócio);</li>
     *   <li>cria o produto (rejeita tipo desconhecido);</li>
     *   <li>pede à linha que confira capacidade e matéria-prima de <b>todas</b> as etapas, sem consumir nada;</li>
     *   <li>pede à linha que produza; se qualquer etapa falhar (inclusive por erro inesperado),
     *       restaura o estoque e relança a exceção;</li>
     *   <li>somente ao final debita o orçamento e atualiza demanda, armazém e contadores.</li>
     * </ol>
     * A linha sempre desliga as máquinas ao terminar, com sucesso ou não. Uma placa rejeitada na
     * inspeção não é erro: os recursos gastos permanecem e a placa não vai para o armazém.
     *
     * @param demanda demanda a atender
     * @throws OrcamentoInsuficienteException se o orçamento não cobre o custo da produção
     * @throws RecursoInsuficienteException   se falta matéria-prima em alguma etapa
     * @throws MaquinaIndisponivelException   se alguma máquina está quebrada, superaquecida, desligada, falha ou não comporta o lote
     */
    private void fabricarUmaUnidade(Demanda demanda) {
        double custo = consultarCustoOperacao();
        if (custo > budget) {
            demanda.cancelar();
            throw new OrcamentoInsuficienteException(String.format(
                    "Orçamento insuficiente para produzir %s: custo R$%.2f, orçamento atual R$%.2f. "
                            + "A demanda foi cancelada por falta de orçamento.",
                    demanda.getTipoProduto(), custo, budget));
        }

        Produto produto = criarProduto(demanda.getTipoProduto());

        try {
            linha.verificarRecursos(produto, materiaPrimaPrincipal, materiaPrimaEmbalagem, materiaPrimaSelo);
        } catch (PlantaException e) {
            throw e.comObservacao(AVISO_SEM_ALTERACAO);
        }

        double principalAntes = materiaPrimaPrincipal.getQuantidade();
        double embalagemAntes = materiaPrimaEmbalagem.getQuantidade();
        double seloAntes = materiaPrimaSelo.getQuantidade();

        produto.setLote("LOTE-" + (contadorLotes + 1));

        try {
            linha.produzir(produto, materiaPrimaPrincipal, materiaPrimaEmbalagem, materiaPrimaSelo);
        } catch (RuntimeException e) {
            // Qualquer falha no meio da linha desfaz o consumo antes de propagar o erro;
            // a exceção não é tratada aqui, apenas relançada para a interface.
            restaurarEstoque(materiaPrimaPrincipal, principalAntes);
            restaurarEstoque(materiaPrimaEmbalagem, embalagemAntes);
            restaurarEstoque(materiaPrimaSelo, seloAntes);
            if (e instanceof PlantaException pe) {
                throw pe.comObservacao("Produção abortada. " + AVISO_SEM_ALTERACAO);
            }
            throw e;
        }

        budget -= custo;
        contadorLotes++;
        contadorPlacas++;
        demanda.iniciarProducao();

        if ("inspecionado".equals(produto.getStatus())) {
            produtosFabricados.add(produto);
            demanda.registrarProducao(1);
            System.out.println("[OK] " + produto.getId() + " (" + produto.getTipo() + ") fabricado e aprovado!");
        } else {
            System.out.println("[FALHA] " + produto.getId() + " (" + produto.getTipo() + ") não passou na inspeção.");
        }
    }

    /**
     * Compra matéria-prima, debitando o custo do orçamento.
     *
     * @param qual       matéria-prima: {@code laminado}, {@code esd}/{@code embalagem} ou {@code selo}/{@code cera}
     * @param quantidade quantidade a comprar; deve ser um número positivo
     * @throws EntradaInvalidaException       se a matéria-prima é desconhecida ou a quantidade é inválida
     * @throws OrcamentoInsuficienteException se o custo da compra excede o orçamento
     */
    public void comprarMateriaPrima(String qual, double quantidade) {
        MateriaPrima mp = escolherMateriaPrima(qual);
        if (!(quantidade > 0) || Double.isInfinite(quantidade)) {
            throw new EntradaInvalidaException("A quantidade a comprar deve ser um número maior que zero.");
        }
        double custoTotal = quantidade * mp.getCustoPorUnidade();
        if (custoTotal > budget) {
            throw new OrcamentoInsuficienteException(String.format(
                    "Orçamento insuficiente para comprar %.1f %s de %s: custo R$%.2f, orçamento atual R$%.2f.",
                    quantidade, mp.getUnidade(), mp.getNome(), custoTotal, budget));
        }
        mp.adicionarEstoque(quantidade);
        budget -= custoTotal;
        System.out.println("[OK] Compradas " + quantidade + " " + mp.getUnidade() + " de " + mp.getNome()
                + " por R$" + String.format("%.2f", custoTotal));
    }

    /**
     * Exibe o orçamento atual.
     */
    public void exibirBudget() {
        System.out.println("\nBUDGET ATUAL: R$" + String.format("%.2f", budget));
    }

    /**
     * Exibe o armazém: produtos acabados agrupados por tipo, com quantidade, qualidade média,
     * lote e risco.
     */
    public void exibirArmazem() {
        System.out.println("\n*** ARMAZÉM (produtos acabados) ***");
        if (produtosFabricados.isEmpty()) {
            System.out.println("Nenhum produto fabricado ainda.");
            return;
        }

        String[] tipos = {"Placa Simples", "Placa Dupla", "Placa Multi-camada"};

        for (String tipo : tipos) {
            int quantidade = 0;
            double somaQualidade = 0;

            for (Produto p : produtosFabricados) {
                if (p.getTipo().equals(tipo)) {
                    quantidade++;
                    somaQualidade += p.getQualidade();
                }
            }

            if (quantidade == 0) continue;

            System.out.printf("%s | Quantidade: %d | Qualidade média: %.2f%n",
                    tipo, quantidade, somaQualidade / quantidade);

            for (Produto p : produtosFabricados) {
                if (p.getTipo().equals(tipo)) {
                    System.out.printf("   - %s | Lote: %s | Qualidade: %.2f | Risco: %s%n",
                            p.getId(), p.getLote(), p.getQualidade(),
                            p.precisaManutencao() ? "DESCARTE" : "OK");
                }
            }
        }
    }

    /**
     * Exibe o estoque atual de cada matéria-prima.
     */
    public void exibirEstoqueMateriaPrima() {
        System.out.println("\n*** ESTOQUE DE MATÉRIA-PRIMA ***");
        MateriaPrima[] materias = {materiaPrimaPrincipal, materiaPrimaEmbalagem, materiaPrimaSelo};
        for (MateriaPrima mp : materias) {
            System.out.println(mp.getId() + " - " + mp.getNome() + ": " + mp.getQuantidade() + " " + mp.getUnidade());
        }
    }

    /**
     * Gera a auditoria geral, percorrendo a linha de produção e os produtos de forma polimórfica
     * como {@link Auditavel}. O relatório da linha já desce em cascata até as partes de cada máquina.
     */
    public void gerarAuditoriaGeral() {
        List<Auditavel> itens = new ArrayList<>();
        itens.add(linha);
        itens.addAll(produtosFabricados);
        System.out.println("\n*** AUDITORIA GERAL ***");
        for (Auditavel a : itens) {
            System.out.println((a.precisaManutencao() ? "[!] " : "[OK] ") + a.gerarRelatorioDiagnostico());
        }
    }

    /**
     * Exibe a auditoria estrutural em cascata (linha, máquinas e partes de cada máquina).
     */
    public void gerarAuditoriaEstrutural() {
        System.out.println("\n*** AUDITORIA ESTRUTURAL ***");
        System.out.println(linha.gerarRelatorioDiagnostico());
    }

    /**
     * Exibe a estrutura da linha: as máquinas, em ordem, e os componentes de cada uma.
     */
    public void exibirEstruturaLinha() {
        System.out.println("\n*** ESTRUTURA DA LINHA ***");
        System.out.println(linha.descreverEstrutura());
    }

    /**
     * Exibe as leituras atuais de temperatura, ventilação e motor de cada máquina da linha.
     */
    public void exibirLeiturasSensores() {
        System.out.println("\n*** LEITURAS DOS SENSORES ***");
        System.out.println(linha.gerarLeiturasSensores());
    }

    /**
     * Busca uma demanda pelo tipo, ignorando maiúsculas e minúsculas.
     *
     * @param tipoProduto tipo procurado
     * @return a demanda encontrada, ou {@code null}
     */
    private Demanda buscarDemanda(String tipoProduto) {
        for (Demanda d : demandas) {
            if (d.getTipoProduto().equalsIgnoreCase(tipoProduto)) return d;
        }
        return null;
    }

    /**
     * Converte o nome informado na matéria-prima correspondente.
     *
     * @param qual nome da matéria-prima
     * @return a matéria-prima
     * @throws EntradaInvalidaException se o nome não corresponde a nenhuma matéria-prima
     */
    private MateriaPrima escolherMateriaPrima(String qual) {
        switch (qual.toLowerCase()) {
            case "laminado":
                return materiaPrimaPrincipal;
            case "esd":
            case "embalagem":
                return materiaPrimaEmbalagem;
            case "selo":
            case "cera":
                return materiaPrimaSelo;
            default:
                throw new EntradaInvalidaException("Matéria-prima desconhecida: " + qual);
        }
    }

    /**
     * Indica se o tipo de produto é fabricado pela planta.
     *
     * @param tipoProduto tipo a verificar
     * @return {@code true} se o tipo é conhecido
     */
    private boolean ehTipoConhecido(String tipoProduto) {
        if (tipoProduto == null) return false;
        switch (tipoProduto.toLowerCase()) {
            case "placa simples":
            case "placa dupla":
            case "placa multi-camada":
            case "placa multicamada":
                return true;
            default:
                return false;
        }
    }

    /**
     * Instancia o produto do tipo pedido, sem consumir o contador de placas (que só avança
     * quando a produção termina).
     *
     * @param tipoProduto tipo de produto
     * @return o novo produto
     * @throws DemandaInvalidaException se o tipo é desconhecido
     */
    private Produto criarProduto(String tipoProduto) {
        String id = "PCB-" + (contadorPlacas + 1);
        switch (tipoProduto.toLowerCase()) {
            case "placa simples":
                return new PlacaSimples(id, "Placa Simples", 4.0);
            case "placa dupla":
                return new PlacaDupla(id, "Placa Dupla", 12.0);
            case "placa multi-camada":
            case "placa multicamada":
                return new PlacaMultiCamada(id, "Placa Multi-camada", 30.0);
            default:
                throw new DemandaInvalidaException("Tipo de produto desconhecido: " + tipoProduto);
        }
    }

    /**
     * Devolve ao estoque o que foi consumido desde a quantidade registrada antes da produção.
     *
     * @param mp    matéria-prima a restaurar
     * @param antes quantidade que havia antes da produção
     */
    private void restaurarEstoque(MateriaPrima mp, double antes) {
        double consumido = antes - mp.getQuantidade();
        if (consumido > 0) {
            mp.adicionarEstoque(consumido);
        }
    }

    /**
     * Retorna o orçamento atual.
     *
     * @return o budget, em reais
     */
    public double getBudget() {
        return budget;
    }

    /**
     * Retorna o custo de produzir uma unidade, calculado pela linha de produção.
     *
     * @return o custo por unidade produzida, em reais
     */
    public double consultarCustoOperacao() {
        return linha.calcularCustoPorUnidade();
    }

    /**
     * Exibe o resumo numerado de cada máquina da linha.
     */
    public void exibirMaquinas() {
        System.out.println("\n*** MÁQUINAS ***");
        System.out.println(linha.listarMaquinas());
    }

    /**
     * Repara uma máquina da linha (estrutura e partes), debitando o custo de reparo do orçamento.
     *
     * @param indice posição da máquina na linha (começando em zero)
     * @throws EntradaInvalidaException       se o índice não corresponde a nenhuma máquina
     * @throws OrcamentoInsuficienteException se o custo do reparo excede o orçamento
     */
    public void repararMaquina(int indice) {
        double custoReparo = linha.consultarCustoReparo(indice);
        if (custoReparo > budget) {
            throw new OrcamentoInsuficienteException(String.format(
                    "Orçamento insuficiente para reparar a máquina %d: custo R$%.2f, orçamento atual R$%.2f.",
                    indice + 1, custoReparo, budget));
        }
        budget -= custoReparo;
        linha.repararMaquina(indice);
    }
}
