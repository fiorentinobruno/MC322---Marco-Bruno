import java.util.Random;

public class EstacaoInspecao extends Maquina {
    private double fator;
    private double quantidadeSeloPorInspecao;
    private Random random;

    public EstacaoInspecao(String nome, double capacidadeMax, double custoOperacao, double probabilidadeFalha, double fator, double quantidadeSeloPorInspecao) {
        super(nome, capacidadeMax, probabilidadeFalha, custoOperacao);
        this.fator = fator;
        this.quantidadeSeloPorInspecao = quantidadeSeloPorInspecao;
        this.random = new Random();
    }

    @Override
    public String getTipo() {
        return "Estação de Inspeção";
    }

    @Override
    public void processar(MateriaPrima mp, Produto produto) {
        if (!estaLigada()) {
            System.out.println("A " + getNome() + " está desligada.");
            return;
        }

        if (verificarFalha()) {
            desligar();
            System.out.println("Falha técnica na " + getNome() + "! Inspeção de " + produto.getId() + " não pôde ser concluída. Máquina desligada.");
            return;
        }

        if (mp.getQuantidade() < quantidadeSeloPorInspecao) {
            System.out.println("Não há " + mp.getNome() + " suficiente para selar a inspeção.");
            return;
        }

        mp.consumir(quantidadeSeloPorInspecao);

        double chanceRejeicao = (produto.getQualidade() * fator) + produto.getProbabilidadeFalhaAcumulada();

        double sorteado = random.nextDouble();

        if (sorteado < chanceRejeicao) {
            produto.setStatus("defeituoso");
            System.out.println("Produto " + produto.getId() + " rejeitado na inspeção (critério de qualidade).");
        } else {
            produto.aprovar();
            System.out.println("Produto " + produto.getId() + " aprovado na inspeção! Selo de cera aplicado.");
        }
    }
}

//MP CERA