import java.util.Random;

public abstract class Maquina {
    private String nome;
    private boolean ligada;
    private double capacidadeMax;
    private double probabilidadeFalha;
    private double custoOperacao;

    private Random random;

    public Maquina(String nome, double capacidadeMax, double probabilidadeFalha, double custoOperacao) {
        this.nome = nome;
        this.ligada = false;
        this.capacidadeMax = capacidadeMax;
        this.probabilidadeFalha = probabilidadeFalha;
        this.custoOperacao = custoOperacao;
        this.random = new Random();
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
        return sorteado < probabilidadeFalha;
    }


    public double getCapacidadeMax() {
        return capacidadeMax;
    }
}
