import java.util.Random;

public class Embaladora extends Maquina {

    private double chanceAumentarFalha;
    private double quantidadeEsdBagPorEmbalagem;
    private Random random;

    public Embaladora(String nome, double capacidadeMax, double custoOperacao,
                      double chanceAumentarFalha, double quantidadeEsdBagPorEmbalagem) {
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
        exigirLigada();
        exigirCapacidade(quantidadeEsdBagPorEmbalagem);
        mp.exigirDisponibilidade(quantidadeEsdBagPorEmbalagem);

        mp.consumir(quantidadeEsdBagPorEmbalagem);
        produto.setStatus("embalado");
        System.out.println("Embalando " + produto.getNome() + " (" + produto.getId()
                + ") em saco antiestático...");

        if (random.nextDouble() < chanceAumentarFalha) {
            produto.aumentarProbabilidadeFalha(0.1);
            System.out.println("Atenção: selagem imperfeita em " + produto.getId()
                    + ". Probabilidade de reprovação aumentada em 10%.");
        }
    }

    public double getQuantidadeEsdBagPorEmbalagem() {
        return quantidadeEsdBagPorEmbalagem;
    }
}
