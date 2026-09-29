import java.util.ArrayList;
import java.util.List;

public class GerenciadorProducao {

    private static int contadorPlacas = 0;
    private int contadorLotes = 0;

    private List<Demanda> demandas;
    private List<Produto> produtosFabricados;
    private List<Maquina> maquinas;

    private MateriaPrima materiaPrimaPrincipal;
    private MateriaPrima materiaPrimaEmbalagem;
    private MateriaPrima materiaPrimaSelo;

    private Corrosora corrosora;
    private Embaladora embaladora;
    private EstacaoInspecao estacaoInspecao;

    private double budget;
    private final Cenario cenario;
    private EstrategiaProducao estrategiaAtual;

    public GerenciadorProducao(MateriaPrima materiaPrimaPrincipal, MateriaPrima materiaPrimaEmbalagem,
                               MateriaPrima materiaPrimaSelo, Corrosora corrosora, Embaladora embaladora,
                               EstacaoInspecao estacaoInspecao, Cenario cenario,
                               EstrategiaProducao estrategiaInicial) {
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

        this.cenario = cenario;
        this.budget = cenario.getBudgetInicial();
        this.estrategiaAtual = estrategiaInicial;

        for (Maquina m : maquinas) m.aplicarCenario(cenario);
    }

    public void setEstrategia(EstrategiaProducao nova) {
        this.estrategiaAtual = nova;
        System.out.println("Estratégia alterada para: " + nova.getNomeEstrategia());
    }

    public EstrategiaProducao getEstrategiaAtual() {
        return estrategiaAtual;
    }

    public Cenario getCenario() {
        return cenario;
    }

    public void executarProximaProducao() {
        Demanda d = estrategiaAtual.selecionarDemanda(demandas, budget);
        if (d == null) {
            System.out.println("Nenhuma demanda elegível para a estratégia " + estrategiaAtual.getNomeEstrategia() + ".");
            return;
        }
        System.out.println("Estratégia [" + estrategiaAtual.getNomeEstrategia() + "] escolheu: " + d.getTipoProduto());
        fabricarUmaUnidade(d);
    }

    public void registrarDemanda(String tipoProduto, int quantidade) {
        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda == null) {
            demandas.add(new Demanda(tipoProduto, quantidade, consultarCustoOperacao()));
            System.out.println("Demanda registrada: " + quantidade + " unidade(s) de " + tipoProduto);
            return;
        }
        if (!demanda.atualizarQuantidade(quantidade)) {
            System.out.println("A demanda de " + tipoProduto + " foi cancelada e não pode ser alterada.");
            return;
        }
        System.out.println("Demanda atualizada: " + tipoProduto + " agora com "
                + demanda.getQuantidadeProdutos() + " unidade(s) pendente(s)");
    }

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

    public void fabricarDemanda(String tipoProduto) {
        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda == null || demanda.getQuantidadeProdutos() <= 0
                || demanda.getStatus() == StatusDemanda.CANCELADA) {
            System.out.println("Não há demanda pendente para " + tipoProduto + ".");
            return;
        }
        fabricarUmaUnidade(demanda);
    }

    private void fabricarUmaUnidade(Demanda demanda) {
        double custo = consultarCustoOperacao();
        if (custo > budget) {
            System.out.println("Budget insuficiente. Custo: R$" + String.format("%.2f", custo)
                    + " | Budget atual: R$" + String.format("%.2f", budget));
            demanda.cancelar();
            System.out.println("Demanda de " + demanda.getTipoProduto() + " cancelada por falta de orçamento.");
            return;
        }

        Produto produto = criarProduto(demanda.getTipoProduto());
        if (produto == null) {
            System.out.println("Tipo de produto desconhecido: " + demanda.getTipoProduto());
            return;
        }

        demanda.iniciarProducao();
        produto.setLote("LOTE-" + (++contadorLotes));

        for (Maquina m : maquinas) m.ligar();

        System.out.println("\n--- Iniciando produção de " + demanda.getTipoProduto() + " (" + produto.getLote() + ") ---");

        corrosora.processar(materiaPrimaPrincipal, produto);
        if (!"processado".equals(produto.getStatus())) {
            System.out.println("Produção interrompida na corrosão.");
            desligarMaquinas();
            return;
        }
        budget -= corrosora.getCustoOperacao();

        embaladora.processar(materiaPrimaEmbalagem, produto);
        if (!"embalado".equals(produto.getStatus())) {
            System.out.println("Produção interrompida na embalagem.");
            desligarMaquinas();
            return;
        }
        budget -= embaladora.getCustoOperacao();

        estacaoInspecao.processar(materiaPrimaSelo, produto);
        boolean inspecionou = "inspecionado".equals(produto.getStatus())
                || "defeituoso".equals(produto.getStatus());
        if (!inspecionou) {
            System.out.println("Produção interrompida na inspeção.");
            desligarMaquinas();
            return;
        }
        budget -= estacaoInspecao.getCustoOperacao();

        desligarMaquinas();

        if ("inspecionado".equals(produto.getStatus())) {
            produtosFabricados.add(produto);
            demanda.registrarProducao(1);
            System.out.println("[OK] " + produto.getId() + " (" + produto.getTipo() + ") fabricado e aprovado!");
        } else {
            System.out.println("[FALHA] " + produto.getId() + " (" + produto.getTipo() + ") não passou na inspeção.");
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

    public void exibirEstoqueMateriaPrima() {
        System.out.println("\n*** ESTOQUE DE MATÉRIA-PRIMA ***");
        MateriaPrima[] materias = {materiaPrimaPrincipal, materiaPrimaEmbalagem, materiaPrimaSelo};
        for (MateriaPrima mp : materias) {
            System.out.println(mp.getId() + " - " + mp.getNome() + ": " + mp.getQuantidade() + " " + mp.getUnidade());
        }
    }

    public void gerarAuditoriaGeral() {
        List<Auditavel> itens = new ArrayList<>();
        itens.addAll(maquinas);
        itens.addAll(produtosFabricados);
        System.out.println("\n*** AUDITORIA GERAL ***");
        for (Auditavel a : itens) {
            System.out.println((a.precisaManutencao() ? "[!] " : "[OK] ") + a.gerarRelatorioDiagnostico());
        }
    }

    private Demanda buscarDemanda(String tipoProduto) {
        for (Demanda d : demandas) {
            if (d.getTipoProduto().equalsIgnoreCase(tipoProduto)) return d;
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
        String id = "PCB-" + (contadorPlacas + 1);
        Produto p;
        switch (tipoProduto.toLowerCase()) {
            case "placa simples":
                p = new PlacaSimples(id, "Placa Simples", 4.0);
                break;
            case "placa dupla":
                p = new PlacaDupla(id, "Placa Dupla", 12.0);
                break;
            case "placa multi-camada":
            case "placa multicamada":
                p = new PlacaMultiCamada(id, "Placa Multi-camada", 30.0);
                break;
            default:
                return null;
        }
        contadorPlacas++;
        return p;
    }

    private void desligarMaquinas() {
        for (Maquina m : maquinas) m.desligar();
    }

    public double getBudget() {
        return budget;
    }

    public double consultarCustoOperacao() {
        double total = 0;
        for (Maquina m : maquinas) total += m.getCustoOperacao();
        return total;
    }

    public void exibirMaquinas() {
        System.out.println("\n*** MÁQUINAS ***");
        for (int i = 0; i < maquinas.size(); i++) {
            System.out.println((i + 1) + " - " + maquinas.get(i).gerarRelatorioDiagnostico());
        }
    }

    public void repararMaquina(int indice) {
        if (indice < 0 || indice >= maquinas.size()) {
            System.out.println("Máquina inválida.");
            return;
        }
        Maquina m = maquinas.get(indice);
        if (m.getCustoReparo() > budget) {
            System.out.println("Budget insuficiente para reparar " + m.getNome() + ".");
            return;
        }
        budget -= m.getCustoReparo();
        m.reparar();
    }
}