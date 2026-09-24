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
    }

    public abstract void processar(MateriaPrima mp, Produto produto);
    public abstract String getTipo();

    public void ligar() {
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

    public double getCustoOperacao(){
        return custoOperacao;
    }

    protected boolean verificarFalha(){
        double sorteado = random.nextDouble();
        return sorteado < calcularProbabilidadeFalhaAtual();
    }

    public double getCapacidadeMax() {
        return capacidadeMax;
    }

    public void aplicarDesgaste(double multiplicadorCenario){
        if (saude <= 0) return;

        double intervalo = desgasteMaximo - desgasteMinimo;
        double perdaAleatoria = desgasteMinimo + (random.nextDouble() * intervalo);

        double desgasteTotal = perdaAleatoria * multiplicadorCenario;

        this.saude -= desgasteTotal;

        if (saude <= 0) {
            this.saude = 0;
            desligar();
            System.out.printf("A máquina " + getNome() + " quebrou totalmente por desgaste. É necessário manutenção.\n");
        }
    }

    public double calcularProbabilidadeFalhaAtual() {
            double desgasteProporcional = (100.0 - saude) / 100.0;
            return probabilidadeFalha + (desgasteProporcional * (1.0 - probabilidadeFalha ));
    }

    public double getSaude(){
        return saude;
    }

    public void reparar(){
            this.saude = 100;
            System.out.printf("A máquina " + getNome() + " foi consertada. Sua saúde agora é 100.\n");
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
