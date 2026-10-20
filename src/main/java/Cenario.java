/**
 * Cenários de operação da planta. Cada cenário define o orçamento inicial e o quão severos são o
 * desgaste e a probabilidade de falha das máquinas.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public enum Cenario {
    /** Orçamento farto e poucas falhas. */
    IDEAL("Cenário Ideal", 6000.0, 1.0, 1.0),
    /** Orçamento curto, máquinas se desgastando mais rápido e falhando com mais frequência. */
    APOCALIPTICO("Cenário Apocalíptico", 2000.0, 1.5, 1.4);

    private final String nome;
    private final double budgetInicial;
    private final double multiplicadorDesgaste;
    private final double multiplicadorFalha;

    Cenario(String nome, double budgetInicial, double multiplicadorDesgaste, double multiplicadorFalha) {
        this.nome = nome;
        this.budgetInicial = budgetInicial;
        this.multiplicadorDesgaste = multiplicadorDesgaste;
        this.multiplicadorFalha = multiplicadorFalha;
    }

    /**
     * Retorna o nome de exibição do cenário.
     *
     * @return o nome do cenário
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o orçamento com que a planta começa neste cenário.
     *
     * @return o budget inicial, em reais
     */
    public double getBudgetInicial() {
        return budgetInicial;
    }

    /**
     * Retorna o fator aplicado à perda de saúde das máquinas a cada uso.
     *
     * @return o multiplicador de desgaste (1.0 significa desgaste normal)
     */
    public double getMultiplicadorDesgaste() {
        return multiplicadorDesgaste;
    }

    /**
     * Retorna o fator aplicado à probabilidade-base de falha das máquinas.
     *
     * @return o multiplicador de falha (1.0 significa probabilidade normal)
     */
    public double getMultiplicadorFalha() {
        return multiplicadorFalha;
    }
}
