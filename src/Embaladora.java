import java.util.Random;

public class Embaladora extends Maquina {
    private double chanceAumentarFalha;
    private double quantidadeEsdBagPorEmbalagem;
    private Random random;

    public Embaladora(String nome, double capacidadeMax, double custoOperacao, double chanceAumentarFalha, double quantidadeEsdBagPorEmbalagem) {
        super(nome, capacidadeMax, 0.0, custoOperacao); 
        this.chanceAumentarFalha = chanceAumentarFalha;
        this.quantidadeEsdBagPorEmbalagem = quantidadeEsdBagPorEmbalagem;
        this.random = new Random();
    }

    @Override
    public String getTipo() {
        return "Embaladora";
    }

    @Override
    public void processar(MateriaPrima mp, Produto produto) {
        if (!estaLigada()) {
            System.out.println("A " + getNome() + " está desligada.");
            return;
        }
        if (quantidadeEsdBagPorEmbalagem > getCapacidadeMax()) {
            System.out.println("A capacidade máxima da " + getNome() + " foi ultrapassada.");
            return;
        }
        if (!mp.verificarDisponibilidade(quantidadeEsdBagPorEmbalagem)) {
            System.out.println("Não há " + mp.getNome() + " suficiente para embalar.");
            return;
        }

        mp.consumir(quantidadeEsdBagPorEmbalagem);
        produto.processar(mp.getId());
        System.out.println("Embalando " + produto.getNome() + " (" + produto.getId() + ") em saco antiestático...");

        if (random.nextDouble() < chanceAumentarFalha) {
            produto.aumentarProbabilidadeFalha(0.1);
            System.out.println("Atenção: possível imperfeição na embalagem de " + produto.getId() + ".");
        }
    }
}

//MP será ssaquinho ECD antiestatico
