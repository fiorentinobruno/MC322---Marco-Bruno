import java.util.Scanner;

public class Main {

    private static final String NOME_FABRICA = "PCBs BM LTDA";
    private static final String LEMA = "Confiabilidade, qualidade e toda a tecnologia de ponta especialmente para voce!";
    private static final String DUPLA = "Marco Correa e Bruno Fiorentino";

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        MateriaPrima laminado = new MateriaPrima("MP1", "Laminado FR-4 (cobre 35 um)", 200.0, "dm2", 20.0, 5.0);
        MateriaPrima esd      = new MateriaPrima("MP2", "Saco ESD antiestatico", 100.0, "un", 10.0, 2.0);
        MateriaPrima cera     = new MateriaPrima("MP3", "Selo de cera", 100.0, "un", 10.0, 1.0);

        Corrosora corrosora = new Corrosora("Corrosora CU-1", 60.0, 10.0, 0.3);

        Embaladora embaladora = new Embaladora("Embaladora ESD-2", 60.0, 8.0, 0.2, 1.0);

        EstacaoInspecao inspecao = new EstacaoInspecao("Bancada de Teste", 60.0, 12.0, 0.1, 0.3, 1.0);

        GerenciadorProducao gerenciador = new GerenciadorProducao(
                laminado, esd, cera, corrosora, embaladora, inspecao, 1000.0);

        exibirIntroducao(laminado);

        boolean executando = true;
        while (executando) {
            exibirMenu(gerenciador.getBudget());
            int opcao = lerInteiro("ESCOLHA: ");

            switch (opcao) {
                case 1 -> gerenciador.atualizarDemanda("placa simples", lerInteiro("Quantas placas simples? "));
                case 2 -> gerenciador.atualizarDemanda("placa dupla", lerInteiro("Quantas placas duplas? "));
                case 3 -> gerenciador.atualizarDemanda("placa multi-camada", lerInteiro("Quantas placas multi-camada? "));
                case 4 -> gerenciador.fabricarDemanda("placa simples");
                case 5 -> gerenciador.fabricarDemanda("placa dupla");
                case 6 -> gerenciador.fabricarDemanda("placa multi-camada");
                case 7 -> gerenciador.exibirArmazem();
                case 8 -> gerenciador.exibirEstoqueMateriaPrima();
                case 9 -> comprarMateriaPrima(gerenciador);
                case 0 -> {
                    executando = false;
                    System.out.println("\nDesligando a linha. Faca bom proveito dos PCB's!");
                }
                default -> System.out.println("\nOpcao inexistente. Escolha um numero do menu.");
            }
        }

        scanner.close();
    }

    private static void comprarMateriaPrima(GerenciadorProducao gerenciador) {
        System.out.println("\nMaterias-primas: laminado | esd | cera");
        System.out.println("1 - Laminado");
        System.out.println("2 - ESD (embalagem)");
        System.out.println("3 - Cera (selo)");
        int qual = lerInteiro("Qual comprar? ");
        String nome;
        switch (qual) {
            case 1 -> nome = "laminado";
            case 2 -> nome = "esd";
            case 3 -> nome = "cera";
            default -> {
                System.out.println("Opcao invalida.");
                return;
            }
        }
        double qtd = lerDouble("Quantidade a comprar: ");
        gerenciador.comprarMateriaPrima(nome, qtd);
    }

    private static void exibirIntroducao(MateriaPrima mp) {
        System.out.println("\n*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
        System.out.println(NOME_FABRICA);
        System.out.println(LEMA);
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*\n");
        System.out.println("Materia-prima principal: " + mp.getNome());
        System.out.println("Produto fabricado: placas de circuito impresso (PCB's)");
        System.out.println("Desenvolvido pelos bilionarios: " + DUPLA);
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
    }

    private static void exibirMenu(double budget) {
        System.out.println("\n*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
        System.out.println(NOME_FABRICA);
        System.out.printf("BUDGET ATUAL: R$%.2f%n", budget);
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*\n");
        System.out.println("ATUALIZAR DEMANDAS");
        System.out.println("1 - Atualizar demanda de Placa Simples");
        System.out.println("2 - Atualizar demanda de Placa Dupla");
        System.out.println("3 - Atualizar demanda de Placa Multi-camada");
        System.out.println("\nFABRICAR");
        System.out.println("4 - Fabricar Placa Simples");
        System.out.println("5 - Fabricar Placa Dupla");
        System.out.println("6 - Fabricar Placa Multi-camada");
        System.out.println("\nCONSULTAR");
        System.out.println("7 - Ver armazem");
        System.out.println("8 - Ver estoque de materia-prima");
        System.out.println("\nCOMPRAR MATERIA-PRIMA");
        System.out.println("9 - Comprar materia-prima");
        System.out.println("\n0 - SAIR");
    }

    private static int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = proximaLinha().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Digite apenas numeros inteiros.");
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
                System.out.println("Digite apenas numeros (ex.: 12 ou 12.5).");
            }
        }
    }

    private static String proximaLinha() {
        if (!scanner.hasNextLine()) {
            System.out.println("\n\nEntrada encerrada. Desligando a linha com seguranca.");
            scanner.close();
            System.exit(0);
        }
        return scanner.nextLine();
    }
}