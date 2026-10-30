import java.util.Random;

/**
 * Etapa final da linha: bancada de teste que aprova ou rejeita a placa e, ao aprovar, aplica um
 * selo de cera.
 * <p>
 * A chance de rejeição é {@code qualidade * fator + risco acumulado}; portanto placas de maior
 * qualidade exigida passam por uma inspeção mais rigorosa. Problemas nas partes de qualquer
 * máquina da linha (como temperatura em alerta) aumentam o risco acumulado e, com ele, a chance
 * de rejeição.
 * <p>
 * Tem um motor de 0,75 kW e, por lidar com equipamentos de medição, suporta até 60 °C.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class EstacaoInspecao extends Maquina {
    private static final double POTENCIA_MOTOR = 0.75;
    private static final double TEMPERATURA_MAXIMA = 60.0;

    private double fator;
    private double quantidadeSeloPorInspecao;
    private Random random;

    /**
     * Cria uma estação de inspeção (reparo de R$ 125, motor de 0,75 kW e temperatura máxima de 60 °C).
     *
     * @param nome                      nome de exibição
     * @param capacidadeMax             capacidade máxima por inspeção
     * @param custoOperacao             custo, em reais, de cada inspeção
     * @param probabilidadeFalha        probabilidade-base de falha técnica (0 a 1)
     * @param fator                     peso da qualidade da placa na chance de rejeição
     * @param quantidadeSeloPorInspecao selos de cera consumidos em cada inspeção
     * @param quantidadeVentoinhas      número de ventoinhas de resfriamento; deve ser pelo menos 1
     * @throws EntradaInvalidaException se a quantidade de ventoinhas for menor que 1
     */
    public EstacaoInspecao(String nome, double capacidadeMax, double custoOperacao, double probabilidadeFalha,
                           double fator, double quantidadeSeloPorInspecao, int quantidadeVentoinhas) {
        super(nome, capacidadeMax, probabilidadeFalha, custoOperacao, 125, POTENCIA_MOTOR, TEMPERATURA_MAXIMA,
                quantidadeVentoinhas);
        this.fator = fator;
        this.quantidadeSeloPorInspecao = quantidadeSeloPorInspecao;
        this.random = new Random();
    }

    /**
     * Retorna o tipo da máquina.
     *
     * @return {@code "Estação de Inspeção"}
     */
    @Override
    public String getTipo() {
        return "Estação de Inspeção";
    }

    /**
     * Verifica se há selos de cera suficientes para inspecionar. Não altera nada.
     *
     * @param mp      selos de cera a serem consumidos
     * @param produto placa a ser inspecionada
     * @throws RecursoInsuficienteException se o estoque de selos é insuficiente
     */
    @Override
    public void verificarRecursos(MateriaPrima mp, Produto produto) {
        mp.exigirDisponibilidade(quantidadeSeloPorInspecao);
    }

    /**
     * Inspeciona a placa: consome um selo, executa o ciclo de operação (desgaste e temperatura) e
     * marca a placa como aprovada ({@code "inspecionado"}) ou rejeitada ({@code "defeituoso"}).
     * Uma rejeição é um resultado normal da inspeção, não uma exceção.
     *
     * @param mp      selos de cera a serem consumidos
     * @param produto placa a ser inspecionada
     * @throws MaquinaIndisponivelException se a estação está desligada ou sofre falha técnica
     * @throws RecursoInsuficienteException se o estoque de selos é insuficiente
     */
    @Override
    public void processar(MateriaPrima mp, Produto produto) {
        exigirLigada();
        exigirSemFalhaTecnica(produto, "A inspeção");
        verificarRecursos(mp, produto);

        executarCicloOperacao(produto);

        mp.consumir(quantidadeSeloPorInspecao);

        double chanceRejeicao = (produto.getQualidade() * fator) + produto.getProbabilidadeFalhaAcumulada();

        double sorteado = random.nextDouble();

        if (sorteado < chanceRejeicao) {
            produto.setStatus("defeituoso");
            System.out.println("Produto " + produto.getId() + " rejeitado na inspeção (critério de qualidade).");
        } else {
            produto.aprovar();
            System.out.println("Produto " + produto.getId() + " aprovado na inspeção! Selo de cera aplicado.");
        }
    }
}
