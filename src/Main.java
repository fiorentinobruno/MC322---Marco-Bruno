import java.util.Scanner;

public class Main {

    private static final String NOME_FABRICA = "PCBs BM LTDA";
    private static final String LEMA = "Confiabilidade, qualidade e toda a tecnologia de ponta especialmente para você!";
    private static final String DUPLA = "Marco Correa e Bruno Fiorentino";

    private static final String PLACA_SIMPLES = "Placa Simples";
    private static final String PLACA_DUPLA = "Placa Dupla";
    private static final String PLACA_MULTI = "Placa Multi-camada";

    private static final String LINHA = "==============================================================";
    private static final String DIVISORIA = "--------------------------------------------------------------";

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        MateriaPrima laminado = new MateriaPrima("MP1", "Laminado FR-4 (cobre 35 um)", 200.0, "dm2", 20.0, 5.0);
        MateriaPrima esd      = new MateriaPrima("MP2", "Saco ESD antiestático", 100.0, "un", 10.0, 2.0);
        MateriaPrima cera     = new MateriaPrima("MP3", "Selo de cera", 100.0, "un", 10.0, 1.0);

        Corrosora corrosora = new Corrosora("Corrosora CU-1", 60.0, 10.0, 0.3);
        Embaladora embaladora = new Embaladora("Embaladora ESD-2", 60.0, 8.0, 0.2, 1.0);
        EstacaoInspecao inspecao = new EstacaoInspecao("Bancada de Teste", 60.0, 12.0, 0.1, 0.3, 1.0);

        exibirIntroducao(laminado);
        Cenario cenario = escolherCenario();

        GerenciadorProducao gerenciador = new GerenciadorProducao(
                laminado, esd, cera, corrosora, embaladora, inspecao,
                cenario, new EstrategiaOrdemChegada());

        boolean executando = true;
        while (executando) {
            exibirCabecalho(gerenciador);
            System.out.println("1 - Demandas");
            System.out.println("2 - Fabricação");
            System.out.println("3 - Consultar");
            System.out.println("4 - Comprar matéria-prima");
            System.out.println("5 - Gerenciar estratégia");
            System.out.println("6 - Auditoria e manutenção");
            System.out.println("0 - SAIR");
            int opcao = lerInteiro("ESCOLHA: ");

            switch (opcao) {
                case 1 -> menuDemandas(gerenciador);
                case 2 -> menuFabricacao(gerenciador);
                case 3 -> menuConsultar(gerenciador);
                case 4 -> comprarMateriaPrima(gerenciador);
                case 5 -> menuEstrategia(gerenciador);
                case 6 -> menuAuditoria(gerenciador);
                case 0 -> {
                    executando = false;
                    System.out.println("\nDesligando a linha. Faça bom proveito dos PCB's!");
                }
                default -> System.out.println("\nOpção inexistente. Escolha um número do menu.");
            }
        }

        scanner.close();
    }

    //Introdução e cenário

    private static void exibirIntroducao(MateriaPrima mp) {
        System.out.println("\n" + LINHA);
        System.out.println("| " + NOME_FABRICA);
        System.out.println("| " + LEMA);
        System.out.println(LINHA + "\n");
        System.out.println("Matéria-prima principal: " + mp.getNome());
        System.out.println("Produto fabricado: Placas de circuito impresso (PCB's)");
        System.out.println("Desenvolvido pelos bilionários: " + DUPLA);
        System.out.println("Linha de produção: Corrosora -> Embaladora -> Bancada de Teste");
    }

    private static Cenario escolherCenario() {
        while (true) {
            System.out.println("\n" + DIVISORIA);
            System.out.println("ESCOLHA O CENÁRIO DE OPERAÇÃO");
            System.out.println(DIVISORIA);
            System.out.println("1 - " + Cenario.IDEAL.getNome() + " (budget farto, poucas falhas)");
            System.out.println("2 - " + Cenario.APOCALIPTICO.getNome() + " (budget curto, máquinas quebrando)");
            int opcao = lerInteiro("ESCOLHA: ");
            if (opcao == 1) return Cenario.IDEAL;
            if (opcao == 2) return Cenario.APOCALIPTICO;
            System.out.println("Opção inválida.");
        }
    }

    //Cabeçalho

    private static void exibirCabecalho(GerenciadorProducao g) {
        System.out.println("\n" + LINHA);
        System.out.println("| [" + NOME_FABRICA + "]");
        System.out.println("| ESTRATEGIA ATUAL: [" + g.getEstrategiaAtual().getNomeEstrategia() + "]");
        System.out.println("| CENARIO ATIVO: [" + g.getCenario().getNome() + "]");
        System.out.printf("| BUDGET ATUAL: R$ %.2f%n", g.getBudget());
        System.out.println(LINHA);
    }

    private static void exibirTituloSubmenu(String titulo) {
        System.out.println("\n" + DIVISORIA);
        System.out.println("[" + titulo + "]");
        System.out.println(DIVISORIA);
    }

    //Submenus

    private static void menuDemandas(GerenciadorProducao g) {
        boolean voltar = false;
        while (!voltar) {
            exibirTituloSubmenu("DEMANDAS");
            System.out.println("1 - Atualizar demanda de " + PLACA_SIMPLES);
            System.out.println("2 - Atualizar demanda de " + PLACA_DUPLA);
            System.out.println("3 - Atualizar demanda de " + PLACA_MULTI);
            System.out.println("4 - Listar demandas");
            System.out.println("0 - Voltar");
            int opcao = lerInteiro("ESCOLHA: ");

            switch (opcao) {
                case 1 -> atualizarDemanda(g, PLACA_SIMPLES);
                case 2 -> atualizarDemanda(g, PLACA_DUPLA);
                case 3 -> atualizarDemanda(g, PLACA_MULTI);
                case 4 -> g.listarDemandas();
                case 0 -> voltar = true;
                default -> System.out.println("Opção inexistente.");
            }
        }
    }

    private static void menuFabricacao(GerenciadorProducao g) {
        boolean voltar = false;
        while (!voltar) {
            exibirTituloSubmenu("FABRICAÇÃO");
            System.out.println("1 - Processar próxima demanda (usa a estratégia ativa)");
            System.out.println("2 - Fabricar " + PLACA_SIMPLES);
            System.out.println("3 - Fabricar " + PLACA_DUPLA);
            System.out.println("4 - Fabricar " + PLACA_MULTI);
            System.out.println("0 - Voltar");
            int opcao = lerInteiro("ESCOLHA: ");

            switch (opcao) {
                case 1 -> g.executarProximaProducao();
                case 2 -> g.fabricarDemanda(PLACA_SIMPLES);
                case 3 -> g.fabricarDemanda(PLACA_DUPLA);
                case 4 -> g.fabricarDemanda(PLACA_MULTI);
                case 0 -> voltar = true;
                default -> System.out.println("Opção inexistente.");
            }
        }
    }

    private static void menuConsultar(GerenciadorProducao g) {
        boolean voltar = false;
        while (!voltar) {
            exibirTituloSubmenu("CONSULTAR");
            System.out.println("1 - Ver armazém (produtos acabados)");
            System.out.println("2 - Ver estoque de matéria-prima");
            System.out.println("3 - Ver budget");
            System.out.println("0 - Voltar");
            int opcao = lerInteiro("ESCOLHA: ");

            switch (opcao) {
                case 1 -> g.exibirArmazem();
                case 2 -> g.exibirEstoqueMateriaPrima();
                case 3 -> g.exibirBudget();
                case 0 -> voltar = true;
                default -> System.out.println("Opção inexistente.");
            }
        }
    }

    private static void menuEstrategia(GerenciadorProducao g) {
        boolean voltar = false;
        while (!voltar) {
            exibirTituloSubmenu("GERENCIAR ESTRATÉGIA");
            System.out.println("Ativa agora: " + g.getEstrategiaAtual().getNomeEstrategia());
            System.out.println("1 - Ordem de Chegada");
            System.out.println("2 - Maior Demanda");
            System.out.println("3 - Maximizar Produção");
            System.out.println("0 - Voltar");
            int opcao = lerInteiro("ESCOLHA: ");

            switch (opcao) {
                case 1 -> g.setEstrategia(new EstrategiaOrdemChegada());
                case 2 -> g.setEstrategia(new EstrategiaMaiorDemanda());
                case 3 -> g.setEstrategia(new EstrategiaMaximoProdutos());
                case 0 -> voltar = true;
                default -> System.out.println("Opção inexistente.");
            }
        }
    }

    private static void menuAuditoria(GerenciadorProducao g) {
        boolean voltar = false;
        while (!voltar) {
            exibirTituloSubmenu("AUDITORIA E MANUTENÇÃO");
            System.out.println("1 - Relatório geral (máquinas e produtos)");
            System.out.println("2 - Detalhar máquinas");
            System.out.println("3 - Reparar máquina");
            System.out.println("0 - Voltar");
            int opcao = lerInteiro("ESCOLHA: ");

            switch (opcao) {
                case 1 -> g.gerarAuditoriaGeral();
                case 2 -> g.exibirMaquinas();
                case 3 -> repararMaquina(g);
                case 0 -> voltar = true;
                default -> System.out.println("Opção inexistente.");
            }
        }
    }

    //Ações 

    private static void atualizarDemanda(GerenciadorProducao g, String tipoProduto) {
        int quantidade = lerInteiro("Quantas unidades de " + tipoProduto + "? ");
        if (quantidade <= 0) {
            System.out.println("A quantidade precisa ser maior que zero.");
            return;
        }
        g.registrarDemanda(tipoProduto, quantidade);
    }

    private static void repararMaquina(GerenciadorProducao g) {
        g.exibirMaquinas();
        int numero = lerInteiro("Qual máquina reparar (0 para voltar)? ");
        if (numero == 0) return;
        g.repararMaquina(numero - 1);
    }

    private static void comprarMateriaPrima(GerenciadorProducao g) {
        exibirTituloSubmenu("COMPRA DE MATÉRIA-PRIMA");
        System.out.println("1 - Laminado");
        System.out.println("2 - ESD (embalagem)");
        System.out.println("3 - Cera (selo)");
        System.out.println("0 - Voltar");
        int qual = lerInteiro("Qual comprar? ");
        String nome;
        switch (qual) {
            case 1 -> nome = "laminado";
            case 2 -> nome = "esd";
            case 3 -> nome = "cera";
            case 0 -> {
                return;
            }
            default -> {
                System.out.println("Opção inválida.");
                return;
            }
        }
        double qtd = lerDouble("Quantidade a comprar: ");
        if (qtd <= 0) {
            System.out.println("Informe um valor maior que zero.");
            return;
        }
        g.comprarMateriaPrima(nome, qtd);
    }

    //Leitura 

    private static int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = proximaLinha().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Digite apenas números inteiros.");
            }
        }
    }

    private static double lerDouble(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = proximaLinha().trim().replace(',', '.');
            try {
                return Double.parseDouble(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Digite apenas números (ex.: 12 ou 12.5).");
            }
        }
    }

    private static String proximaLinha() {
        if (!scanner.hasNextLine()) {
            System.out.println("\n\nEntrada encerrada. Desligando a linha com segurança.");
            scanner.close();
            System.exit(0);
        }
        return scanner.nextLine();
    }
}