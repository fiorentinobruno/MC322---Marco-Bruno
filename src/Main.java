import java.util.Scanner;

public class Main {

    private static final String NOME_FABRICA = "PCBs BM LTDA";
    private static final String LEMA = "Confiabilidade, qualidade e toda a tecnologia de ponta especialmente para você!";
    private static final String DUPLA = "Marco Correa e Bruno Fiorentino";

    private static final String PLACA_SIMPLES = "Placa Simples";
    private static final String PLACA_DUPLA = "Placa Dupla";
    private static final String PLACA_MULTI = "Placa Multi-camada";

    private static final double DEMANDA_SIMPLES = 4.0;
    private static final double DEMANDA_DUPLA = 12.0;
    private static final double DEMANDA_MULTI = 30.0;

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        MateriaPrima laminado = new MateriaPrima("MP1", "Laminado FR-4 (cobre 35 um)", 200.0, "dm2", 20.0, 5.0);
        MateriaPrima esd      = new MateriaPrima("MP2", "Saco ESD antiestático", 100.0, "un", 10.0, 2.0);
        MateriaPrima cera     = new MateriaPrima("MP3", "Selo de cera", 100.0, "un", 10.0, 1.0);

        Corrosora corrosora = new Corrosora("Corrosora CU-1", 60.0, 10.0, 0.3);

        Embaladora embaladora = new Embaladora("Embaladora ESD-2", 60.0, 8.0, 0.2, 1.0);

        EstacaoInspecao inspecao = new EstacaoInspecao("Bancada de Teste", 60.0, 12.0, 0.1, 0.3, 1.0);

        GerenciadorProducao gerenciador = new GerenciadorProducao(
                laminado, esd, cera, corrosora, embaladora, inspecao, 1000.0);

        exibirIntroducao(laminado, gerenciador);

        boolean executando = true;
        while (executando) {
            exibirMenu(gerenciador.getBudget());
            int opcao = lerInteiro("ESCOLHA: ");

            switch (opcao) {
                case 1 -> atualizarDemanda(gerenciador, PLACA_SIMPLES);
                case 2 -> atualizarDemanda(gerenciador, PLACA_DUPLA);
                case 3 -> atualizarDemanda(gerenciador, PLACA_MULTI);
                case 4 -> gerenciador.fabricarDemanda(PLACA_SIMPLES);
                case 5 -> gerenciador.fabricarDemanda(PLACA_DUPLA);
                case 6 -> gerenciador.fabricarDemanda(PLACA_MULTI);
                case 7 -> gerenciador.exibirArmazem();
                case 8 -> gerenciador.exibirEstoqueMateriaPrima();
                case 9 -> comprarMateriaPrima(gerenciador);
                case 0 -> {
                    executando = false;
                    System.out.println("\nDesligando a linha. Faça bom proveito dos PCB's!");
                }
                default -> System.out.println("\nOpção inexistente. Escolha um número do menu.");
            }
        }

        scanner.close();
    }

    private static void atualizarDemanda(GerenciadorProducao gerenciador, String tipoProduto) {
        int quantidade = lerInteiro("Quantas unidades de " + tipoProduto + "? ");
        if (quantidade <= 0) {
            System.out.println("A quantidade precisa ser maior que zero.");
            return;
        }
        gerenciador.registrarDemanda(tipoProduto, quantidade);
    }

    private static void comprarMateriaPrima(GerenciadorProducao gerenciador) {
        System.out.println("\n*** COMPRA DE MATÉRIA-PRIMA ***");
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
        gerenciador.comprarMateriaPrima(nome, qtd);
    }

    private static void exibirIntroducao(MateriaPrima mp, GerenciadorProducao gerenciador) {
        System.out.println("\n*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
        System.out.println(NOME_FABRICA);
        System.out.println(LEMA);
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*\n");
        System.out.println("Matéria-prima principal: " + mp.getNome());
        System.out.println("Produto fabricado: Placas de circuito impresso (PCB's)");
        System.out.println("Desenvolvido pelos bilionários: " + DUPLA);
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*\n");
        System.out.println("Linha de produção: Corrosora -> Embaladora -> Bancada de Teste");
        System.out.println("\nCatálogo (custo de operação por placa: R$" + String.format("%.2f", gerenciador.consultarCustoOperacao()) + "):");
        System.out.println("  " + PLACA_SIMPLES + " - qualidade 0.5 | consome " + DEMANDA_SIMPLES + " dm2 de laminado");
        System.out.println("  " + PLACA_DUPLA + " - qualidade 0.7 | consome " + DEMANDA_DUPLA + " dm2 de laminado");
        System.out.println("  " + PLACA_MULTI + " - qualidade 0.9 | consome " + DEMANDA_MULTI + " dm2 de laminado");
        System.out.println("\nAtenção: quanto maior a qualidade, mais rigorosa é a inspeção!");
    }

    private static void exibirMenu(double budget) {
        System.out.println("\n*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
        System.out.println(NOME_FABRICA);
        System.out.printf("BUDGET ATUAL: R$%.2f%n", budget);
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*\n");
        System.out.println("ATUALIZAR DEMANDAS");
        System.out.println("1 - Atualizar demanda de " + PLACA_SIMPLES);
        System.out.println("2 - Atualizar demanda de " + PLACA_DUPLA);
        System.out.println("3 - Atualizar demanda de " + PLACA_MULTI);
        System.out.println("\nFABRICAR");
        System.out.println("4 - Fabricar " + PLACA_SIMPLES);
        System.out.println("5 - Fabricar " + PLACA_DUPLA);
        System.out.println("6 - Fabricar " + PLACA_MULTI);
        System.out.println("\nCONSULTAR");
        System.out.println("7 - Ver armazém");
        System.out.println("8 - Ver estoque de matéria-prima");
        System.out.println("\nCOMPRAR MATÉRIA-PRIMA");
        System.out.println("9 - Comprar matéria-prima");
        System.out.println("\n0 - SAIR");
    }

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