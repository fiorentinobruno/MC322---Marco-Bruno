import java.util.Scanner;

public class Main {

    
    private static final String NOME_FABRICA = " PCBs BM LTDA";
    private static final String LEMA = "Confiabilidade, qualidade e toda a tecnologia de ponta especialmente para você!";
    private static final String DUPLA = "Marco Correa e Bruno Fiorentino";

    private static final Scanner scanner = new Scanner(System.in);

    private static int contadorPlacas = 0;

    public static void main(String[] args) {
        
        MateriaPrima laminado = new MateriaPrima(
                "MP1", "Laminado FR-4 (cobre 35 um)", 200.0, "dm2", 20.0);

         Produto[] catalogo = {
            new Produto("PCB01", "Placa single-face p/ sensor", 4.0),
            new Produto("PCB02", "Placa dupla-face p/ controlador", 12.0),
            new Produto("PCB03", "Painel de potência", 30.0)
        };

        Maquina corrosora = new Maquina("Corrosora CU-1", false, 35.0);
        Esteira esteira = new Esteira(60.0);
        EstacaoInspecao bancadaTeste = new EstacaoInspecao();

        exibirIntroducao(laminado, catalogo);

        boolean executando = true;
        while (executando) {
            exibirMenu();
            int opcao = lerInteiro("Escolha: ");

            switch (opcao) {
                case 1 -> iniciarProducao(laminado, catalogo, corrosora, esteira, bancadaTeste);
                case 2 -> consultarEstoque(laminado);
                case 3 -> reporEstoque(laminado);
                case 4 -> System.out.println("\nPlacas aprovadas na Estação de inspeção: "
                        + bancadaTeste.getTotalInspecionados());
                case 5 -> {
                    executando = false;
                    System.out.println("\nDesligando a linha. Faça bom proveito dos PCB's!!!'");
                }
                default -> System.out.println("\n Opção inexistente. Escolha um número de 1 a 5.");
            }
        }

        scanner.close();
    }

     

    private static void exibirIntroducao(MateriaPrima mp, Produto[] catalogo) {
        System.out.println("\n*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
        System.out.println(NOME_FABRICA);
        System.out.println(LEMA);
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");

        System.out.println();
        System.out.println("Matéria-prima principal: " + mp.getNome());
        System.out.println("Produto fabricado: placas de circuito impresso (PCB's)");
        System.out.println("Desenvolvido pelos bilionários: " + DUPLA);
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*\n");

        System.out.println("Estoque inicial: " + fmt(mp.getQuantidade()) + " " + mp.getUnidade()
                + "  (" + mp.getId() + ")");
        System.out.println("\nNossos produtos e placas:");
        listarProdutos(catalogo, mp.getUnidade());
    }

    private static void listarProdutos(Produto[] catalogo, String unidade) {
        for (int i = 0; i < catalogo.length; i++) {
            System.out.println("  " + (i + 1) + " - " + catalogo[i].getNome()
                    + " (área padrão: " + fmt(catalogo[i].getDemandaMateriaPrima())
                    + " " + unidade + ")");
        }
    }

    private static void exibirMenu() {
        System.out.println("\n*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
        System.out.println("MENU MB");
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*\n");
        System.out.println("1 - Iniciar produção de uma placa");
        System.out.println("2 - Consultar estoque de laminado");
        System.out.println("3 - Repor estoque de laminado");
        System.out.println("4 - Relatório da Estação de inspeção");
        System.out.println("5 - Sair");
    }
 
    private static void iniciarProducao(MateriaPrima mp, Produto[] catalogo,
                                        Maquina corrosora, Esteira esteira,
                                        EstacaoInspecao bancada) {
        String un = mp.getUnidade();

        System.out.println("\n*** NOVA ORDEM DE PRODUÇÃO ***");
        listarProdutos(catalogo, un);

        int escolha = lerInteiro("Selecione a placa (1-" + catalogo.length + "): ");
        if (escolha < 1 || escolha > catalogo.length) {
            System.out.println("Essa placa não existe no catálogo.");
            return;
        }

        Produto modelo = catalogo[escolha - 1];
        double demanda = lerDouble("Área de laminado a ser usada (" + un + "): ");
        if (demanda <= 0) {
            System.out.println("A área precisa ser maior que zero.");
            return;
        }

      
        contadorPlacas++;
        Produto placa = new Produto(modelo.getId() + "/" + contadorPlacas,
                                    modelo.getNome(), modelo.getDemandaMateriaPrima());
         placa.definirDemandaMateriaPrima(demanda);

        System.out.println("\nConferindo o estoque de laminado...");
        if (!mp.verificarDisponibilidade(demanda)) {
            if (mp.getQuantidade() < demanda) {
                System.out.println("Laminado insuficiente para essa ordem. Essa quantidade está disponível: "
                        + fmt(mp.getQuantidade()) + " " + un);
            } else {
                 System.out.println("Estoque na reserva mínima ("
                        + fmt(mp.getQuantidade()) + " " + un + "). Reponha antes de produzir.");
            }
            return;
        }
        System.out.println("[OK] " + fmt(demanda) + " " + un + " de laminado reservados.");

        esteira.ligar();
        System.out.println("[OK] Esteira em movimento.");
        corrosora.ligar();
        System.out.println("[OK] " + corrosora.getNome() + " ligada.");

      
        if (!esteira.adicionarItem(mp, demanda)) {
            System.out.println("O laminado não coube na esteira. Ordem cancelada.");
            desligarLinha(corrosora, esteira, bancada);
            return;
        }
        System.out.println("[OK] Laminado " + mp.getId() + " colocado na esteira.");
        esteira.removerItem();
        System.out.println("[OK] Laminado transportado até a corrosora.");

        
        corrosora.processar(mp, placa);
        if (!placa.getStatus().equals("processado")) {
            System.out.println(" A corrosão não foi concluída. Ordem cancelada.");
            desligarLinha(corrosora, esteira, bancada);
            return;
        }
        System.out.println("[OK] Trilhas de cobre reveladas. Placa " + placa.getId() + " pronta.");

        
        if (!esteira.adicionarItem(placa, demanda)) {
            System.out.println(" A placa não pôde seguir para a Estação de inspeção.");
            desligarLinha(corrosora, esteira, bancada);
            return;
        }
        esteira.removerItem();
        System.out.println("[OK] Placa " + placa.getId() + " levada até a Estação de inspeção.");

        bancada.ativar();
        System.out.println("[OK] Estação de inspeção ativada.");
        bancada.inspecionar(placa);

        desligarLinha(corrosora, esteira, bancada);

        System.out.println();
        if (placa.getStatus().equals("inspecionado")) {
            System.out.println("PLACA APROVADA E LIBERADA PARA EXPEDIÇÃO");
        } else {
            System.out.println("ORDEM ENCERRADA COM PENDÊNCIAS");
        }
        System.out.println();
        System.out.println("Placa: " + placa.getId() + " | status: " + placa.getStatus());
        System.out.println("Laminado de origem: " + placa.getMateriaPrimaUsada());
        System.out.println("Estoque restante: " + fmt(mp.getQuantidade()) + " " + un);
    }

    private static void desligarLinha(Maquina corrosora, Esteira esteira, EstacaoInspecao bancada) {
        corrosora.desligar();
        esteira.desligar();
        bancada.desativar();
    }

    private static void consultarEstoque(MateriaPrima mp) {
        System.out.println("\n*** ESTOQUE ***");
        System.out.println(mp.getId() + " - " + mp.getNome() + ": "
                + fmt(mp.getQuantidade()) + " " + mp.getUnidade());
    }

    private static void reporEstoque(MateriaPrima mp) {
        double quantidade = lerDouble("Quanto laminado deseja receber (" + mp.getUnidade() + ")? ");
        if (quantidade <= 0) {
            System.out.println("Informe um valor maior que zero.");
            return;
        }
        mp.adicionarEstoque(quantidade);
        System.out.println("[OK] Estoque atualizado: " + fmt(mp.getQuantidade()) + " " + mp.getUnidade());
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

    private static String fmt(double valor) {
        return String.format("%.1f", valor);
    }
}