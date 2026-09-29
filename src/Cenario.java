public enum Cenario {
    IDEAL("Cenário Ideal", 6000.0, 1.0, 1.0),
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

    public String getNome() {
        return nome;
    }

    public double getBudgetInicial() {
        return budgetInicial;
    }

    public double getMultiplicadorDesgaste() {
        return multiplicadorDesgaste;
    }

    public double getMultiplicadorFalha() {
        return multiplicadorFalha;
    }
}