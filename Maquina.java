
public class Maquina {
    private String nome;
    private boolean ligada;
    private double capacidadeMax;

    public Maquina(String nome, boolean ligada, double capacidadeMax) {
        this.nome = nome;
        this.ligada = ligada;
        this.capacidadeMax = capacidadeMax;
    }

    public void ligar() {
        this.ligada = true;
    }

    public void desligar() {
        this.ligada = false;
    }

    public void processar(MateriaPrima mp, Produto produto) {
        double demanda = produto.getDemandaMateriaPrima();
        if (this.ligada) {
            if (this.capacidadeMax < demanda) {
                System.out.println("A demanda excede a capacidade máxima da máquina");
            } else {
                if (mp.verificarDisponibilidade(demanda)) {
                    mp.consumir(demanda);
                    produto.processar(mp.getId());
                    System.out.println("Produzindo " + produto.getNome() + "...");
                } else {
                    System.out.println("Estoque insuficiente de " + mp.getNome());
                }
            }
        } else {
            System.out.println("Máquina desligada");
        }
    }

    public String getNome() {
        return nome;
    }

    public boolean estaLigada() {
        return this.ligada;
    }

}
