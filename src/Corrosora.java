import java.util.Random;

public class Corrosora extends Maquina {

    private double chanceAumentarFalha;
    private Random random;

    public Corrosora(String nome, double capacidadeMax, double custoOperacao, double chanceAumentarFalha) {
        super(nome, capacidadeMax, 0.0, custoOperacao);
        this.chanceAumentarFalha = chanceAumentarFalha;
        this.random = new Random();
    }

    @Override
    public String getTipo() {
        return "Corrosora";
    }

    @Override
    public void processar(MateriaPrima mp, Produto produto) {
        double area = produto.getQuantidadeMateriaPrimaPorUnidade();

        exigirLigada();
        exigirCapacidade(area);
        mp.exigirDisponibilidade(area);

        mp.consumir(area);
        produto.processar(mp.getId());
        System.out.println("Corroendo " + produto.getNome() + " (" + produto.getId()
                + "). Trilhas de cobre reveladas.");

        if (random.nextDouble() < chanceAumentarFalha) {
            produto.aumentarProbabilidadeFalha(0.1);
            System.out.println("Atenção: ataque irregular do banho em " + produto.getId()
                    + ". Probabilidade de reprovação aumentada em 10%.");
        }
    }
}
