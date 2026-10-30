import java.util.Scanner;

/**
 * Ponto de entrada e camada de interação com o usuário (menus no terminal) da planta de PCBs.
 * <p>
 * Aqui são criadas as matérias-primas, as máquinas e a {@link LinhaDeProducao} que as agrupa. As
 * máquinas são criadas fora da linha (agregação) e recebem apenas configurações primitivas: cada
 * máquina instancia suas próprias partes internas (composição).
 * <p>
 * Esta classe é a responsável por <b>capturar as exceções</b> lançadas pelo domínio: cada ação
 * do menu é executada por {@link #executar(Runnable)}, que exibe uma mensagem de erro clara e
 * devolve o usuário ao menu em que estava, sem encerrar o programa.
 * <ul>
 *   <li>{@link PlantaException}: erro previsto (entrada, regra de negócio, recurso, máquina);</li>
 *   <li>qualquer outra {@link RuntimeException}: falha inesperada, relatada e isolada para que o
 *       programa continue.</li>
 * </ul>
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class Main {

    private static final String NOME_FABRICA = "PCBs BM LTDA";
    private static final String LEMA = "Confiabilidade, qualidade e toda a tecnologia de ponta especialmente para você!";
    private static final String DUPLA = "Marco Correa e Bruno Fiorentino";

    private static final String PLACA_SIMPLES = "Placa Simples";
    private static final String PLACA_DUPLA = "Placa Dupla";
    private static final String PLACA_MULTI = "Placa Multi-camada";

    private static final String LINHA = "==============================================================";
    private static final String DIVISORIA = "--------------------------------------------------------------";
    private static final int LARGURA_ERRO = 60;

    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Classe de partida: não deve ser instanciada.
     */
    private Main() {
    }

    /**
     * Inicia o programa: monta a linha de produção, escolhe o cenário e executa o menu principal
     * até o usuário sair.
     *
     * @param args argumentos de linha de comando (não utilizados)
     */
    public static void main(String[] args) {

        MateriaPrima laminado = new MateriaPrima("MP1", "Laminado FR-4 (cobre 35 um)", 200.0, "dm2", 20.0, 5.0);
        MateriaPrima esd      = new MateriaPrima("MP2", "Saco ESD antiestático", 100.0, "un", 10.0, 2.0);
        MateriaPrima cera     = new MateriaPrima("MP3", "Selo de cera", 100.0, "un", 10.0, 1.0);

        // O último argumento de cada máquina é o número de ventoinhas (relação 1:N com a máquina).
        Corrosora corrosora = new Corrosora("Corrosora CU-1", 60.0, 10.0, 0.3, 2);
        Embaladora embaladora = new Embaladora("Embaladora ESD-2", 60.0, 8.0, 0.2, 1.0, 2);
        EstacaoInspecao inspecao = new EstacaoInspecao("Bancada de Teste", 60.0, 12.0, 0.1, 0.3, 1.0, 1);

        // Agrupador: a linha recebe máquinas que já existem (agregação), na ordem das etapas.
        LinhaDeProducao linhaPrincipal = new LinhaDeProducao("Linha de Produção PCB-1");
        linhaPrincipal.adicionarMaquina(corrosora);
        linhaPrincipal.adicionarMaquina(embaladora);
        linhaPrincipal.adicionarMaquina(inspecao);

        exibirIntroducao(laminado);
        Cenario cenario = escolherCenario();

        GerenciadorProducao gerenciador = new GerenciadorProducao(
                laminado, esd, cera, linhaPrincipal,
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
            System.out.println("7 - Linha de produção");
            System.out.println("0 - SAIR");
            int opcao = lerInteiro("ESCOLHA: ");

            switch (opcao) {
                case 1 -> menuDemandas(gerenciador);
                case 2 -> menuFabricacao(gerenciador);
                case 3 -> menuConsultar(gerenciador);
                case 4 -> executar(() -> comprarMateriaPrima(gerenciador));
                case 5 -> menuEstrategia(gerenciador);
                case 6 -> menuAuditoria(gerenciador);
                case 7 -> menuLinhaProducao(gerenciador);
                case 0 -> {
                    executando = false;
                    System.out.println("\nDesligando a linha. Faça bom proveito dos PCB's!");
                }
                default -> opcaoInexistente();
            }
        }

        scanner.close();
    }

    // Tratamento de erros

    /**
     * Executa uma ação do menu tratando seus erros: o usuário é informado e volta ao menu.
     * <p>
     * A captura de {@link RuntimeException} é feita apenas aqui, na fronteira com o usuário, para
     * que um defeito inesperado não derrube a planta; ele é sempre exibido, nunca ignorado.
     *
     * @param acao ação a executar
     */
    private static void executar(Runnable acao) {
        try {
            acao.run();
        } catch (PlantaException e) {
            exibirErro("ERRO", e.getMessage(), e.getObservacao());
        } catch (RuntimeException e) {
            exibirErro("ERRO INESPERADO",
                    "Ocorreu uma falha inesperada (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ").",
                    "O programa continua em execução. Se o problema persistir, reinicie a aplicação.");
        }
    }

    /**
     * Exibe uma caixa de erro e aguarda o usuário pressionar ENTER.
     *
     * @param titulo     título da caixa (ex.: {@code "ERRO"})
     * @param mensagem   explicação do que aconteceu
     * @param observacao texto complementar sobre o estado do sistema; ignorado se vazio
     */
    private static void exibirErro(String titulo, String mensagem, String observacao) {
        String borda = "=".repeat(LARGURA_ERRO);
        System.out.println();
        System.out.println(borda);
        System.out.println(titulo);
        System.out.println(borda);
        System.out.println(quebrarLinhas(mensagem, LARGURA_ERRO));
        if (observacao != null && !observacao.isBlank()) {
            System.out.println();
            System.out.println(quebrarLinhas(observacao, LARGURA_ERRO));
        }
        System.out.println();
        System.out.print("Pressione ENTER para continuar...");
        proximaLinha();
    }

    /**
     * Quebra um texto em linhas de no máximo a largura informada, sem partir palavras.
     *
     * @param texto   texto a quebrar
     * @param largura número máximo de caracteres por linha
     * @return o texto com quebras de linha
     */
    private static String quebrarLinhas(String texto, int largura) {
        StringBuilder resultado = new StringBuilder();
        StringBuilder linha = new StringBuilder();
        for (String palavra : texto.split("\\s+")) {
            if (linha.length() > 0 && linha.length() + 1 + palavra.length() > largura) {
                resultado.append(linha).append('\n');
                linha.setLength(0);
            }
            if (linha.length() > 0) linha.append(' ');
            linha.append(palavra);
        }
        return resultado.append(linha).toString();
    }

    /**
     * Informa que a opção digitada não existe no menu.
     */
    private static void opcaoInexistente() {
        System.out.println("\nOpção inexistente. Escolha um número do menu.");
    }

    // Introdução e cenário

    /**
     * Exibe a apresentação da fábrica.
     *
     * @param mp matéria-prima principal, citada na apresentação
     */
    private static void exibirIntroducao(MateriaPrima mp) {
        System.out.println("\n" + LINHA);
        System.out.println("| " + NOME_FABRICA);
        System.out.println("| " + LEMA);
        System.out.println(LINHA + "\n");
        System.out.println("Matéria-prima principal: " + mp.getNome());
        System.out.println("Produto fabricado: Placas de circuito impresso (PCB's)");
        System.out.println("Desenvolvido pelos bilionários: " + DUPLA);
        System.out.println("Linha de produção: Corrosora -> Embaladora -> Bancada de Teste");
        System.out.println("Cada máquina é composta por motor, sensor de temperatura e ventoinhas de resfriamento.");
    }

    /**
     * Pergunta o cenário de operação, repetindo enquanto a opção for inválida.
     *
     * @return o cenário escolhido
     */
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
            System.out.println("Opção inválida. Digite 1 ou 2.");
        }
    }

    // Cabeçalho

    /**
     * Exibe o cabeçalho com linha, estratégia, cenário e orçamento atuais.
     *
     * @param g gerenciador da planta
     */
    private static void exibirCabecalho(GerenciadorProducao g) {
        System.out.println("\n" + LINHA);
        System.out.println("| [" + NOME_FABRICA + "]");
        System.out.println("| LINHA ATIVA: [" + g.getNomeLinha() + "]");
        System.out.println("| ESTRATEGIA ATUAL: [" + g.getEstrategiaAtual().getNomeEstrategia() + "]");
        System.out.println("| CENARIO ATIVO: [" + g.getCenario().getNome() + "]");
        System.out.printf("| BUDGET ATUAL: R$ %.2f%n", g.getBudget());
        System.out.println(LINHA);
    }

    /**
     * Exibe o título de um submenu.
     *
     * @param titulo texto do título
     */
    private static void exibirTituloSubmenu(String titulo) {
        System.out.println("\n" + DIVISORIA);
        System.out.println("[" + titulo + "]");
        System.out.println(DIVISORIA);
    }

    // Submenus

    /**
     * Submenu de demandas.
     *
     * @param g gerenciador da planta
     */
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
                case 1 -> executar(() -> atualizarDemanda(g, PLACA_SIMPLES));
                case 2 -> executar(() -> atualizarDemanda(g, PLACA_DUPLA));
                case 3 -> executar(() -> atualizarDemanda(g, PLACA_MULTI));
                case 4 -> executar(g::listarDemandas);
                case 0 -> voltar = true;
                default -> opcaoInexistente();
            }
        }
    }

    /**
     * Submenu de fabricação.
     *
     * @param g gerenciador da planta
     */
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
                case 1 -> executar(g::executarProximaProducao);
                case 2 -> executar(() -> g.fabricarDemanda(PLACA_SIMPLES));
                case 3 -> executar(() -> g.fabricarDemanda(PLACA_DUPLA));
                case 4 -> executar(() -> g.fabricarDemanda(PLACA_MULTI));
                case 0 -> voltar = true;
                default -> opcaoInexistente();
            }
        }
    }

    /**
     * Submenu de consultas (armazém, estoque e orçamento).
     *
     * @param g gerenciador da planta
     */
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
                case 1 -> executar(g::exibirArmazem);
                case 2 -> executar(g::exibirEstoqueMateriaPrima);
                case 3 -> executar(g::exibirBudget);
                case 0 -> voltar = true;
                default -> opcaoInexistente();
            }
        }
    }

    /**
     * Submenu de escolha da estratégia de produção.
     *
     * @param g gerenciador da planta
     */
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
                case 1 -> executar(() -> g.setEstrategia(new EstrategiaOrdemChegada()));
                case 2 -> executar(() -> g.setEstrategia(new EstrategiaMaiorDemanda()));
                case 3 -> executar(() -> g.setEstrategia(new EstrategiaMaximoProdutos()));
                case 0 -> voltar = true;
                default -> opcaoInexistente();
            }
        }
    }

    /**
     * Submenu de auditoria e manutenção.
     *
     * @param g gerenciador da planta
     */
    private static void menuAuditoria(GerenciadorProducao g) {
        boolean voltar = false;
        while (!voltar) {
            exibirTituloSubmenu("AUDITORIA E MANUTENÇÃO");
            System.out.println("1 - Relatório geral (linha, máquinas, componentes e produtos)");
            System.out.println("2 - Auditoria estrutural em cascata (linha > máquinas > componentes)");
            System.out.println("3 - Detalhar máquinas");
            System.out.println("4 - Reparar máquina");
            System.out.println("0 - Voltar");
            int opcao = lerInteiro("ESCOLHA: ");

            switch (opcao) {
                case 1 -> executar(g::gerarAuditoriaGeral);
                case 2 -> executar(g::gerarAuditoriaEstrutural);
                case 3 -> executar(g::exibirMaquinas);
                case 4 -> executar(() -> repararMaquina(g));
                case 0 -> voltar = true;
                default -> opcaoInexistente();
            }
        }
    }

    /**
     * Submenu da linha de produção (o agrupador das máquinas).
     *
     * @param g gerenciador da planta
     */
    private static void menuLinhaProducao(GerenciadorProducao g) {
        boolean voltar = false;
        while (!voltar) {
            exibirTituloSubmenu("LINHA DE PRODUÇÃO");
            System.out.println("1 - Ver estrutura da linha (máquinas e componentes)");
            System.out.println("2 - Ver leituras de temperatura, ventilação e motor");
            System.out.println("3 - Auditoria estrutural em cascata");
            System.out.println("0 - Voltar");
            int opcao = lerInteiro("ESCOLHA: ");

            switch (opcao) {
                case 1 -> executar(g::exibirEstruturaLinha);
                case 2 -> executar(g::exibirLeiturasSensores);
                case 3 -> executar(g::gerarAuditoriaEstrutural);
                case 0 -> voltar = true;
                default -> opcaoInexistente();
            }
        }
    }

    // Ações

    /**
     * Pergunta a quantidade e registra/atualiza a demanda do tipo informado.
     *
     * @param g           gerenciador da planta
     * @param tipoProduto tipo de produto da demanda
     */
    private static void atualizarDemanda(GerenciadorProducao g, String tipoProduto) {
        int quantidade = lerInteiro("Quantas unidades de " + tipoProduto + "? ");
        g.registrarDemanda(tipoProduto, quantidade);
    }

    /**
     * Mostra as máquinas, pergunta qual reparar e executa o reparo.
     *
     * @param g gerenciador da planta
     * @throws EntradaInvalidaException       se o número da máquina é inválido
     * @throws OrcamentoInsuficienteException se o orçamento não cobre o reparo
     */
    private static void repararMaquina(GerenciadorProducao g) {
        g.exibirMaquinas();
        int numero = lerInteiro("Qual máquina reparar (0 para voltar)? ");
        if (numero == 0) return;
        g.repararMaquina(numero - 1);
    }

    /**
     * Mostra as matérias-primas, pergunta qual e quanto comprar e executa a compra.
     *
     * @param g gerenciador da planta
     * @throws EntradaInvalidaException       se a quantidade é inválida
     * @throws OrcamentoInsuficienteException se o orçamento não cobre a compra
     */
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
                opcaoInexistente();
                return;
            }
        }
        double qtd = lerDouble("Quantidade a comprar: ");
        g.comprarMateriaPrima(nome, qtd);
    }

    // Leitura

    /**
     * Lê um número inteiro do teclado, repetindo a pergunta enquanto a entrada for inválida.
     *
     * @param mensagem texto exibido antes da leitura
     * @return o inteiro digitado
     */
    private static int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = proximaLinha().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite apenas números inteiros.");
            }
        }
    }

    /**
     * Lê um número decimal finito do teclado (aceita vírgula ou ponto), repetindo a pergunta
     * enquanto a entrada for inválida.
     *
     * @param mensagem texto exibido antes da leitura
     * @return o número digitado
     */
    private static double lerDouble(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = proximaLinha().trim().replace(',', '.');
            try {
                double valor = Double.parseDouble(entrada);
                if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                    System.out.println("Entrada inválida. Informe um número finito (ex.: 12 ou 12.5).");
                } else {
                    return valor;
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite apenas números (ex.: 12 ou 12.5).");
            }
        }
    }

    /**
     * Lê a próxima linha do teclado. Se a entrada acabar (EOF), encerra o programa com segurança.
     *
     * @return a linha digitada
     */
    private static String proximaLinha() {
        if (!scanner.hasNextLine()) {
            System.out.println("\n\nEntrada encerrada. Desligando a linha com segurança.");
            scanner.close();
            System.exit(0);
        }
        return scanner.nextLine();
    }
}
