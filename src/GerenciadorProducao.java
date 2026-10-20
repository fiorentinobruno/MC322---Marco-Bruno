import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GerenciadorProducao {

    private static int contadorPlacas = 0;

    private ArrayList<Demanda> demandas;
    private ArrayList<Produto> produtosFabricados;
    private ArrayList<Maquina> maquinas;

    private MateriaPrima materiaPrimaPrincipal;
    private MateriaPrima materiaPrimaEmbalagem;
    private MateriaPrima materiaPrimaSelo;

    private Corrosora corrosora;
    private Embaladora embaladora;
    private EstacaoInspecao estacaoInspecao;

    private EstrategiaProducao estrategia;

    private double budget;

    public GerenciadorProducao(MateriaPrima materiaPrimaPrincipal, MateriaPrima materiaPrimaEmbalagem,
                               MateriaPrima materiaPrimaSelo, Corrosora corrosora, Embaladora embaladora,
                               EstacaoInspecao estacaoInspecao, double budget) {
        this.demandas = new ArrayList<>();
        this.produtosFabricados = new ArrayList<>();
        this.maquinas = new ArrayList<>();
        this.maquinas.add(corrosora);
        this.maquinas.add(embaladora);
        this.maquinas.add(estacaoInspecao);

        this.materiaPrimaPrincipal = materiaPrimaPrincipal;
        this.materiaPrimaEmbalagem = materiaPrimaEmbalagem;
        this.materiaPrimaSelo = materiaPrimaSelo;

        this.corrosora = corrosora;
        this.embaladora = embaladora;
        this.estacaoInspecao = estacaoInspecao;

        this.estrategia = new EstrategiaOrdemChegada();
        this.budget = budget;
    }

    public void registrarDemanda(String tipoProduto, int quantidade) {
        if (quantidade <= 0) {
            throw new DemandaInvalidaException(
                    "A quantidade pedida precisa ser maior que zero (recebido: " + quantidade + ").");
        }
        exigirTipoConhecido(tipoProduto);

        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda == null) {
            demanda = new Demanda(tipoProduto, quantidade);
            demandas.add(demanda);
            System.out.println("Demanda registrada: " + quantidade + " unidade(s) de " + tipoProduto);
        } else {
            demanda.atualizarQuantidade(quantidade);
            System.out.println("Demanda atualizada: " + tipoProduto + " agora com "
                    + demanda.getQuantidadeProdutos() + " unidade(s) pendente(s)");
        }
    }

    public void fabricarDemanda(String tipoProduto) {
        exigirTipoConhecido(tipoProduto);

        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda == null) {
            throw new DemandaInvalidaException(
                    "Não existe demanda registrada para " + tipoProduto
                    + ". Registre a demanda antes de mandar fabricar.");
        }
        if (!demanda.estaProduzivel()) {
            throw new DemandaInvalidaException(
                    "A demanda de " + tipoProduto + " não está produzível (saldo: "
                    + demanda.getQuantidadeProdutos() + ", situação: "
                    + demanda.getStatus().getDescricao() + ").");
        }

        executarCiclo(demanda);
    }

    public void fabricarProximaDemanda() {
        Demanda escolhida = estrategia.selecionarDemanda(Collections.unmodifiableList(demandas), budget);
        if (escolhida == null) {
            throw new DemandaInvalidaException(
                    "A estratégia \"" + estrategia.getNomeEstrategia()
                    + "\" não encontrou demanda elegível. Registre uma demanda ou verifique o orçamento.");
        }
        System.out.println("\nEstratégia \"" + estrategia.getNomeEstrategia()
                + "\" escolheu: " + escolhida.getTipoProduto()
                + " (" + escolhida.getQuantidadeProdutos() + " pendente(s))");
        executarCiclo(escolhida);
    }

    private void executarCiclo(Demanda demanda) {
        String tipoProduto = demanda.getTipoProduto();
        Produto produto = criarProduto(tipoProduto);

        validarRecursos(produto);

        demanda.iniciarProducao();

        corrosora.ligar();
        embaladora.ligar();
        estacaoInspecao.ligar();

        System.out.println("\n--- Iniciando produção de " + tipoProduto + " ---");
        System.out.println("[OK] Linha ligada: " + corrosora.getNome() + ", "
                + embaladora.getNome() + ", " + estacaoInspecao.getNome());

        try {
            corrosora.processar(materiaPrimaPrincipal, produto);
            budget -= corrosora.getCustoOperacao();

            embaladora.processar(materiaPrimaEmbalagem, produto);
            budget -= embaladora.getCustoOperacao();

            estacaoInspecao.processar(materiaPrimaSelo, produto);
            budget -= estacaoInspecao.getCustoOperacao();
        } finally {
            desligarMaquinas();
        }

        produtosFabricados.add(produto);

        if (produto.getStatus().equals("inspecionado")) {
            demanda.registrarProducao(1);
            System.out.println("[OK] " + produto.getId() + " (" + produto.getTipo()
                    + ") fabricado e aprovado!");
            if (demanda.getStatus() == StatusDemanda.CONCLUIDA) {
                System.out.println("[OK] Demanda de " + tipoProduto + " concluída.");
            }
        } else {
            System.out.println("[FALHA] " + produto.getId() + " (" + produto.getTipo()
                    + ") não passou na inspeção e foi para o armazém como refugo. "
                    + "A demanda continua pendente.");
        }
    }

    private void validarRecursos(Produto produto) {
        double custoCiclo = consultarCustoOperacao();
        if (custoCiclo > budget) {
            throw new RecursoInsuficienteException(
                    "Orçamento insuficiente para o ciclo de " + produto.getTipo()
                    + ". Custo: R$" + String.format("%.2f", custoCiclo)
                    + " | Disponível: R$" + String.format("%.2f", budget) + ".");
        }
        materiaPrimaPrincipal.exigirDisponibilidade(produto.getQuantidadeMateriaPrimaPorUnidade());
        materiaPrimaEmbalagem.exigirDisponibilidade(embaladora.getQuantidadeEsdBagPorEmbalagem());
        materiaPrimaSelo.exigirDisponibilidade(estacaoInspecao.getQuantidadeSeloPorInspecao());
    }

    public void comprarMateriaPrima(String qual, double quantidade) {
        if (quantidade <= 0) {
            throw new DemandaInvalidaException(
                    "A quantidade comprada precisa ser maior que zero.");
        }
        MateriaPrima mp = escolherMateriaPrima(qual);
        if (mp == null) {
            throw new DemandaInvalidaException("Matéria-prima desconhecida: " + qual + ".");
        }
        double custoTotal = quantidade * mp.getCustoPorUnidade();
        if (custoTotal > budget) {
            throw new RecursoInsuficienteException(
                    "Orçamento insuficiente para comprar " + String.format("%.1f", quantidade)
                    + " " + mp.getUnidade() + " de " + mp.getNome()
                    + ". Custo: R$" + String.format("%.2f", custoTotal)
                    + " | Disponível: R$" + String.format("%.2f", budget) + ".");
        }
        mp.adicionarEstoque(quantidade);
        budget -= custoTotal;
        System.out.println("[OK] Compradas " + String.format("%.1f", quantidade) + " "
                + mp.getUnidade() + " de " + mp.getNome()
                + " por R$" + String.format("%.2f", custoTotal));
    }

    public void cancelarDemanda(String tipoProduto) {
        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda == null) {
            throw new DemandaInvalidaException(
                    "Não existe demanda registrada para " + tipoProduto + ".");
        }
        demanda.cancelar();
        System.out.println("[OK] Demanda de " + tipoProduto + " cancelada.");
    }

    public void definirEstrategia(EstrategiaProducao estrategia) {
        if (estrategia == null) {
            throw new DemandaInvalidaException("Estratégia de produção inválida.");
        }
        this.estrategia = estrategia;
        System.out.println("[OK] Estratégia de produção agora é: " + estrategia.getNomeEstrategia());
    }

    public String getNomeEstrategia() {
        return estrategia.getNomeEstrategia();
    }

    public void exibirBudget() {
        System.out.println("\nBUDGET ATUAL: R$" + String.format("%.2f", budget));
    }

    public void exibirDemandas() {
        System.out.println("\n*** DEMANDAS ***");
        if (demandas.isEmpty()) {
            System.out.println("Nenhuma demanda registrada ainda.");
            return;
        }
        for (Demanda d : demandas) {
            System.out.println(" - " + d.getTipoProduto() + " | Pendente: "
                    + d.getQuantidadeProdutos() + " | Situação: " + d.getStatus().getDescricao());
        }
    }

    public void exibirArmazem() {
        System.out.println("\n*** ARMAZÉM ***");
        if (produtosFabricados.isEmpty()) {
            System.out.println("Nenhum produto fabricado ainda.");
            return;
        }
        for (Produto p : produtosFabricados) {
            System.out.println(" - " + p.getId() + " | Tipo: " + p.getTipo()
                    + " | Status: " + p.getStatus()
                    + " | Laminado: " + p.getMateriaPrimaUsada());
        }
        System.out.println("Total de placas instanciadas (histórico): "
                + Produto.getTotalProdutosFabricados());
    }

    public void exibirEstoqueMateriaPrima() {
        System.out.println("\n*** ESTOQUE DE MATÉRIA-PRIMA ***");
        exibirLinhaEstoque(materiaPrimaPrincipal);
        exibirLinhaEstoque(materiaPrimaEmbalagem);
        exibirLinhaEstoque(materiaPrimaSelo);
    }

    private void exibirLinhaEstoque(MateriaPrima mp) {
        String alerta = mp.getQuantidade() < mp.getQuantidadeMinima() ? "  <= ABAIXO DA RESERVA" : "";
        System.out.println(mp.getId() + " - " + mp.getNome() + ": "
                + String.format("%.1f", mp.getQuantidade()) + " " + mp.getUnidade() + alerta);
    }

    private Demanda buscarDemanda(String tipoProduto) {
        for (Demanda d : demandas) {
            if (d.getTipoProduto().equalsIgnoreCase(tipoProduto)) {
                return d;
            }
        }
        return null;
    }

    private void exigirTipoConhecido(String tipoProduto) {
        if (criarProdutoOuNulo(tipoProduto, false) == null) {
            throw new DemandaInvalidaException(
                    "Tipo de placa desconhecido: " + tipoProduto
                    + ". O catálogo tem Placa Simples, Placa Dupla e Placa Multi-camada.");
        }
    }

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
                return null;
        }
    }

    private Produto criarProduto(String tipoProduto) {
        Produto produto = criarProdutoOuNulo(tipoProduto, true);
        if (produto == null) {
            throw new DemandaInvalidaException("Tipo de placa desconhecido: " + tipoProduto + ".");
        }
        return produto;
    }

    private Produto criarProdutoOuNulo(String tipoProduto, boolean consumirIdentificador) {
        String id = "PCB-" + (consumirIdentificador ? ++contadorPlacas : contadorPlacas);
        switch (tipoProduto.toLowerCase()) {
            case "placa simples":
                return new PlacaSimples(id, "Placa Simples", 4.0);
            case "placa dupla":
                return new PlacaDupla(id, "Placa Dupla", 12.0);
            case "placa multi-camada":
            case "placa multicamada":
                return new PlacaMultiCamada(id, "Placa Multi-camada", 30.0);
            default:
                return null;
        }
    }

    public double getBudget() {
        return budget;
    }

    public List<Maquina> getMaquinas() {
        return Collections.unmodifiableList(maquinas);
    }

    private void desligarMaquinas() {
        corrosora.desligar();
        embaladora.desligar();
        estacaoInspecao.desligar();
    }

    public double consultarCustoOperacao() {
        return corrosora.getCustoOperacao()
                + embaladora.getCustoOperacao()
                + estacaoInspecao.getCustoOperacao();
    }
}
