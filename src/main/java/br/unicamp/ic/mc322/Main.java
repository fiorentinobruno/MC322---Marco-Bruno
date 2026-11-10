package br.unicamp.ic.mc322;

import java.nio.file.Path;
import java.util.Scanner;
import br.unicamp.ic.mc322.persistence.PersistenciaPlanta;
import br.unicamp.ic.mc322.model.*;
import br.unicamp.ic.mc322.service.*;
import br.unicamp.ic.mc322.strategy.*;
import br.unicamp.ic.mc322.exception.*;

/**
 * Ponto de entrada e camada de interação com o usuário (menus no terminal) da planta de PCBs.
 * <p>
 * A planta padrão (matérias-primas, máquinas e a {@link LinhaDeProducao} que as agrupa) é montada
 * por {@link PlantaPadrao}; o estado salvo é lido e gravado por {@link PersistenciaPlanta}. Esta
 * classe só decide <i>quando</i> salvar ou carregar e substitui a planta em uso pela carregada.
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

    /** Arquivo onde o estado da planta é salvo, relativo à pasta em que a aplicação é executada. */
    private static final Path ARQUIVO_ESTADO = Path.of("dados", "planta.xml");

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
        PersistenciaPlanta persistencia = new PersistenciaPlanta(ARQUIVO_ESTADO);

        exibirIntroducao();
        GerenciadorProducao gerenciador = inicializarPlanta(persistencia);

        boolean executando = true;
        while (executando) {
            // Cópia local: os lambdas abaixo precisam de uma referência efetivamente final, e a
            // planta inteira pode ser substituída pela opção "Carregar estado".
            GerenciadorProducao g = gerenciador;
            exibirCabecalho(g);
            System.out.println("1 - Demandas");
            System.out.println("2 - Fabricação");
            System.out.println("3 - Consultar");
            System.out.println("4 - Comprar matéria-prima");
            System.out.println("5 - Gerenciar estratégia");
            System.out.println("6 - Auditoria e manutenção");
            System.out.println("7 - Linha de produção");
            System.out.println("8 - Salvar estado da planta");
            System.out.println("9 - Carregar estado da planta");
            System.out.println("0 - SAIR");
            int opcao = lerInteiro("ESCOLHA: ");

            switch (opcao) {
                case 1 -> menuDemandas(g);
                case 2 -> menuFabricacao(g);
                case 3 -> menuConsultar(g);
                case 4 -> executar(() -> comprarMateriaPrima(g));
                case 5 -> menuEstrategia(g);
                case 6 -> menuAuditoria(g);
                case 7 -> menuLinhaProducao(g);
                case 8 -> executar(() -> salvarEstado(persistencia, g));
                case 9 -> gerenciador = carregarEstado(persistencia, g);
                case 0 -> {
                    executando = false;
                    oferecerSalvarAoSair(persistencia, g);
                    System.out.println("\nDesligando a linha. Faça bom proveito dos PCB's!");
                }
                default -> opcaoInexistente();
            }
        }

        scanner.close();
    }

    // Persistência

    /**
     * Define a planta da execução. Se existir um estado salvo, pergunta se o usuário quer
     * continuá-lo; se não existir (primeira execução), monta a planta padrão sem tratar isso como
     * erro. Se o arquivo salvo estiver corrompido ou inválido, o erro é exibido, a planta padrão é
     * usada e o arquivo <b>não</b> é sobrescrito, a menos que o usuário salve depois.
     *
     * @param persistencia módulo de persistência
     * @return a planta a ser usada
     */
    private static GerenciadorProducao inicializarPlanta(PersistenciaPlanta persistencia) {
        if (!persistencia.existeEstadoSalvo()) {
            System.out.println("\nNenhum estado salvo encontrado (" + persistencia.getArquivo()
                    + "). Iniciando uma planta nova.");
            return PlantaPadrao.criar(escolherCenario());
        }
        System.out.println("\nFoi encontrado um estado salvo em " + persistencia.getArquivo() + ".");
        System.out.println("1 - Continuar de onde parei (carregar estado salvo)");
        System.out.println("2 - Começar uma planta nova (o arquivo salvo só muda se você salvar)");
        while (true) {
            int opcao = lerInteiro("ESCOLHA: ");
            if (opcao == 1) {
                try {
                    GerenciadorProducao carregada = persistencia.carregar();
                    System.out.println("[OK] Estado da planta carregado com sucesso.");
                    return carregada;
                } catch (PersistenciaException e) {
                    exibirErro("ERRO AO CARREGAR", e.getMessage(),
                            "Uma planta nova será iniciada. O arquivo salvo não foi alterado.");
                    return PlantaPadrao.criar(escolherCenario());
                }
            }
            if (opcao == 2) return PlantaPadrao.criar(escolherCenario());
            System.out.println("Opção inválida. Digite 1 ou 2.");
        }
    }

    /**
     * Salva (ou atualiza) o estado da planta no arquivo.
     *
     * @param persistencia módulo de persistência
     * @param g            planta a salvar
     * @throws PersistenciaException se não for possível gravar o arquivo
     */
    private static void salvarEstado(PersistenciaPlanta persistencia, GerenciadorProducao g) {
        persistencia.salvar(g);
        System.out.println("[OK] Estado da planta salvo em " + persistencia.getArquivo() + ".");
    }

    /**
     * Carrega o estado salvo. Se der certo, a planta carregada substitui a atual por completo; se
     * falhar, o erro é exibido e a planta atual continua exatamente como estava.
     *
     * @param persistencia módulo de persistência
     * @param atual        planta em uso
     * @return a planta carregada, ou a atual se o carregamento falhar
     */
    private static GerenciadorProducao carregarEstado(PersistenciaPlanta persistencia, GerenciadorProducao atual) {
        try {
            GerenciadorProducao carregada = persistencia.carregar();
            System.out.println("[OK] Estado carregado de " + persistencia.getArquivo()
                    + ". A planta atual foi substituída pela salva.");
            return carregada;
        } catch (PersistenciaException e) {
            exibirErro("ERRO AO CARREGAR", e.getMessage(), "A planta atual foi mantida sem alterações.");
            return atual;
        }
    }

    /**
     * Pergunta se o usuário quer salvar antes de sair. Recusar não apaga o estado salvo antes.
     *
     * @param persistencia módulo de persistência
     * @param g            planta em uso
     */
    private static void oferecerSalvarAoSair(PersistenciaPlanta persistencia, GerenciadorProducao g) {
        int opcao = lerInteiro("Salvar o estado da planta antes de sair? (1 - Sim / 0 - Não): ");
        if (opcao == 1) {
            executar(() -> salvarEstado(persistencia, g));
        }
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
     */
    private static void exibirIntroducao() {
        System.out.println("\n" + LINHA);
        System.out.println("| " + NOME_FABRICA);
        System.out.println("| " + LEMA);
        System.out.println(LINHA + "\n");
        System.out.println("Matéria-prima principal: Laminado FR-4 (cobre 35 um)");
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
