import java.util.Random;

/**
 * Etapa final da linha: bancada de teste que aprova ou rejeita a placa e, ao aprovar, aplica um
 * selo de cera.
 * <p>
 * A chance de rejeição é {@code qualidade * fator + risco acumulado}; portanto placas de maior
 * qualidade exigida passam por uma inspeção mais rigorosa.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class EstacaoInspecao extends Maquina {
    private double fator;
    private double quantidadeSeloPorInspecao;
    private Random random;

    /**
     * Cria uma estação de inspeção (reparo de R$ 125).
     *
     * @param nome                     nome de exibição
     * @param capacidadeMax            capacidade máxima por inspeção
     * @param custoOperacao            custo, em reais, de cada inspeção
     * @param probabilidadeFalha       probabilidade-base de falha técnica (0 a 1)
     * @param fator                    peso da qualidade da placa na chance de rejeição
     * @param quantidadeSeloPorInspecao selos de cera consumidos em cada inspeção
     */
    public EstacaoInspecao(String nome, double capacidadeMax, double custoOperacao, double probabilidadeFalha, double fator, double quantidadeSeloPorInspecao) {
        super(nome, capacidadeMax, probabilidadeFalha, custoOperacao, 125);
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
     * Inspeciona a placa: consome um selo, aplica desgaste e marca a placa como aprovada
     * ({@code "inspecionado"}) ou rejeitada ({@code "defeituoso"}). Uma rejeição é um resultado
     * normal da inspeção, não uma exceção.
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

        aplicarDesgaste();

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
