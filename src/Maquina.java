import java.util.Random;

public abstract class Maquina implements Auditavel{
    private String nome;
    private boolean ligada;
    private double capacidadeMax;
    private double probabilidadeFalha;
    private double custoOperacao;
    private double saude;
    private double desgasteMinimo;
    private double desgasteMaximo;
    private double custoReparo;
    private double multiplicadorDesgaste;
    private double multiplicadorFalha;  

    private Random random;

    public Maquina(String nome, double capacidadeMax, double probabilidadeFalha, double custoOperacao, double custoReparo) {
        this.nome = nome;
        this.ligada = false;
        this.capacidadeMax = capacidadeMax;
        this.probabilidadeFalha = probabilidadeFalha;
        this.custoOperacao = custoOperacao;
        this.random = new Random();
        this.saude = 100.0;
        this.desgasteMaximo = 3.0;
        this.desgasteMinimo = 0.5;
        this.custoReparo = custoReparo;
        this.multiplicadorDesgaste = 1.0;
        this.multiplicadorFalha = 1.0;
    }

    public abstract void processar(MateriaPrima mp, Produto produto);
    public abstract String getTipo();

    public void aplicarCenario(Cenario c) {
        this.multiplicadorDesgaste = c.getMultiplicadorDesgaste();
        this.multiplicadorFalha = c.getMultiplicadorFalha();
    }

    public void ligar() {
        if (estaQuebrada()) {
            System.out.println("A " + nome + " está quebrada e precisa de reparo.");
            return;
        }
        this.ligada = true;
    }

    public void desligar() {
        this.ligada = false;
    }

    public String getNome() {
        return nome;
    }

    public boolean estaLigada() {
        return this.ligada;
    }

    public boolean estaQuebrada() {
        return saude <= 0;
    }

    public double getCustoOperacao(){
        return custoOperacao;
    }

    protected boolean verificarFalha() {
        return random.nextDouble() < calcularProbabilidadeFalhaAtual();
    }

    public double getCapacidadeMax() {
        return capacidadeMax;
    }

    public void aplicarDesgaste() {
        if (saude <= 0) return;

        double perda = desgasteMinimo + random.nextDouble() * (desgasteMaximo - desgasteMinimo);
        saude -= perda * multiplicadorDesgaste;

        if (saude <= 0) {
            saude = 0;
            desligar();
            System.out.println("A máquina " + nome + " quebrou totalmente por desgaste. É necessária manutenção.");
        }
    }

    public double calcularProbabilidadeFalhaAtual() {
        double base = Math.min(1.0, probabilidadeFalha * multiplicadorFalha);
        double desgasteProporcional = (100.0 - saude) / 100.0;
        return base + desgasteProporcional * (1.0 - base);
    }

    public double getSaude(){
        return saude;
    }

    public void reparar(){
            this.saude = 100;
            System.out.println("A máquina " + nome + " foi consertada. Sua saúde agora é 100.\n");
    }

    public double getCustoReparo() {
        return custoReparo;
    }

    @Override
    public boolean precisaManutencao() {
        return this.saude < 30.0;
    }

    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format(
            "%s (%s) | Saúde: %.1f%% | Probabilidade falha: %.1f%% | Custo reparo: R$%.2f | Status: %s",
            getNome(),
            getTipo(),
            this.saude,
            calcularProbabilidadeFalhaAtual() * 100.0,
            this.custoReparo,
            precisaManutencao() ? "REQUER MANUTENÇÃO" : "OPERACIONAL"
        );
    }
}
