import java.util.Random;

public class Corrosora extends Maquina{
    private double chanceAumentarFalha;
    private Random random;

    public Corrosora(String nome, double capacidadeMax, double custoOperacao, double chanceAumentarFalha){
        super(nome, capacidadeMax, 0.0, custoOperacao);
        this.chanceAumentarFalha = chanceAumentarFalha;
        this.random = new Random();
    }

    @Override 
    public String getTipo() {
        return "Corrosora";
    }

    @Override 
    public void processar(MateriaPrima mp, Produto produto){
        if (!estaLigada()){
            System.out.println("A " + getNome() + " está desligada.");
            return;
        } 

        if (produto.getQuantidadeMateriaPrimaPorUnidade() > getCapacidadeMax()){
            System.out.println("A capacidade máxima da " + getNome() + " foi ultrapassada.");
            return;
        }

        if (!mp.verificarDisponibilidade(produto.getQuantidadeMateriaPrimaPorUnidade())) {
            System.out.println("Não há matéria prima " + mp.getId() + " suficiente");
            return;
        }

        mp.consumir(produto.getQuantidadeMateriaPrimaPorUnidade());
        produto.processar(mp.getId());

        if (random.nextDouble() < chanceAumentarFalha) {
            produto.aumentarProbabilidadeFalha(0.1);
            System.out.println("Problema na corrosão de " + produto.getId() + ". Probabilidade de falha aumentado em 10%.");
        }


    }
}
