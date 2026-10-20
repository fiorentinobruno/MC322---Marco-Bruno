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

    protected void exigirLigada() {
        if (!ligada) {
            throw new MaquinaIndisponivelException(
                    "A " + nome + " está desligada e não pode processar a placa.");
        }
    }

    protected void exigirCapacidade(double quantidade) {
        if (quantidade > capacidadeMax) {
            throw new MaquinaIndisponivelException(
                    "A " + nome + " comporta no máximo " + String.format("%.1f", capacidadeMax)
                    + " por ciclo, mas a operação exige " + String.format("%.1f", quantidade)
                    + ". Escolha uma placa menor ou divida o lote.");
        }
    }

    public String getNome() {
        return nome;
    }

    public boolean estaLigada() {
        return this.ligada;
    }

    public double getCustoOperacao() {
        return custoOperacao;
    }

    protected boolean verificarFalha() {
        double sorteado = random.nextDouble();
        return sorteado < probabilidadeFalha;
    }

    public double getCapacidadeMax() {
        return capacidadeMax;
    }
}
