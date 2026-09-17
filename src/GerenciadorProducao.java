import java.util.ArrayList;

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

        this.budget = budget;
    }

    public void registrarDemanda(String tipoProduto, int quantidade) {
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

    public void atualizarDemanda(String tipoProduto, int quantidadeAdicional) {
        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda == null) {
            System.out.println("Não existe demanda registrada para " + tipoProduto + ". Registrando nova.");
            registrarDemanda(tipoProduto, quantidadeAdicional);
            return;
        }
        demanda.atualizarQuantidade(quantidadeAdicional);
        System.out.println("Demanda de " + tipoProduto + " atualizada. Pendente: "
                + demanda.getQuantidadeProdutos() + " unidade(s).");
    }

    public void fabricarDemanda(String tipoProduto) {
        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda == null || demanda.getQuantidadeProdutos() <= 0) {
            System.out.println("Não há demanda pendente para " + tipoProduto + ".");
            return;
        }

        Produto produto = criarProduto(tipoProduto);
        if (produto == null) {
            System.out.println("Tipo de produto desconhecido: " + tipoProduto);
            return;
        }

        double custoEstimado = calcularCustoProducao(produto);
        if (custoEstimado > budget) {
            System.out.println("Budget insuficiente para fabricar " + tipoProduto
                    + ". Custo estimado: R$" + String.format("%.2f", custoEstimado)
                    + " | Budget atual: R$" + String.format("%.2f", budget));
            return;
        }

        corrosora.ligar();
        embaladora.ligar();
        estacaoInspecao.ligar();

        corrosora.processar(materiaPrimaPrincipal, produto);
        if (!produto.getStatus().equals("processado")) {
            System.out.println("Produção interrompida na corrosão.");
            desligarMaquinas();
            return;
        }
        budget -= corrosora.getCustoOperacao();

        embaladora.processar(materiaPrimaEmbalagem, produto);
        budget -= embaladora.getCustoOperacao();

        estacaoInspecao.processar(materiaPrimaSelo, produto);
        budget -= estacaoInspecao.getCustoOperacao();

        desligarMaquinas();

        if (produto.getStatus().equals("inspecionado")) {
            produtosFabricados.add(produto);
            demanda.registrarProducao(1);
            System.out.println("[OK] " + produto.getId() + " (" + produto.getTipo() + ") fabricado e aprovado!");
        } else {
            System.out.println("[FALHA] " + produto.getId() + " (" + produto.getTipo()
                    + ") não passou na inspeção. Status final: " + produto.getStatus());
        }
    }

    public void comprarMateriaPrima(String qual, double quantidade) {
        MateriaPrima mp = escolherMateriaPrima(qual);
        if (mp == null) {
            System.out.println("Matéria-prima desconhecida: " + qual);
            return;
        }
        double custoTotal = quantidade * mp.getCustoPorUnidade();
        if (custoTotal > budget) {
            System.out.println("Budget insuficiente para comprar " + quantidade + " " + mp.getUnidade()
                    + " de " + mp.getNome() + ".");
            return;
        }
        mp.adicionarEstoque(quantidade);
        budget -= custoTotal;
        System.out.println("[OK] Compradas " + quantidade + " " + mp.getUnidade() + " de " + mp.getNome()
                + " por R$" + String.format("%.2f", custoTotal));
    }

    public void exibirBudget() {
        System.out.println("\nBUDGET ATUAL: R$" + String.format("%.2f", budget));
    }

    public void exibirArmazem() {
        System.out.println("\n*** ARMAZÉM ***");
        if (produtosFabricados.isEmpty()) {
            System.out.println("Nenhum produto fabricado ainda.");
            return;
        }
        for (Produto p : produtosFabricados) {
            System.out.println(" - " + p.getId() + " | Tipo: " + p.getTipo()
                    + " | Status: " + p.getStatus());
        }
        System.out.println("Total fabricado (histórico): " + Produto.getTotalProdutosFabricados());
    }

    public void exibirEstoqueMateriaPrima() {
        System.out.println("\n*** ESTOQUE DE MATÉRIA-PRIMA ***");
        System.out.println(materiaPrimaPrincipal.getId() + " - " + materiaPrimaPrincipal.getNome() + ": "
                + materiaPrimaPrincipal.getQuantidade() + " " + materiaPrimaPrincipal.getUnidade());
        System.out.println(materiaPrimaEmbalagem.getId() + " - " + materiaPrimaEmbalagem.getNome() + ": "
                + materiaPrimaEmbalagem.getQuantidade() + " " + materiaPrimaEmbalagem.getUnidade());
        System.out.println(materiaPrimaSelo.getId() + " - " + materiaPrimaSelo.getNome() + ": "
                + materiaPrimaSelo.getQuantidade() + " " + materiaPrimaSelo.getUnidade());
    }

    private double calcularCustoProducao(Produto produto) {
        double custoMaterial = produto.getQuantidadeMateriaPrimaPorUnidade()
                * materiaPrimaPrincipal.getCustoPorUnidade();
        double custoMaquinas = corrosora.getCustoOperacao()
                + embaladora.getCustoOperacao()
                + estacaoInspecao.getCustoOperacao();
        return custoMaterial + custoMaquinas;
    }

    private Demanda buscarDemanda(String tipoProduto) {
        for (Demanda d : demandas) {
            if (d.getTipoProduto().equalsIgnoreCase(tipoProduto)) {
                return d;
            }
        }
        return null;
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
        contadorPlacas++;
        String id = "PCB-" + contadorPlacas;
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

    private void desligarMaquinas() {
        corrosora.desligar();
        embaladora.desligar();
        estacaoInspecao.desligar();
    }
}