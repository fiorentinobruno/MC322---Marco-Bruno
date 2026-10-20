import java.util.Random;

public class EstacaoInspecao extends Maquina {

    private double fator;
    private double quantidadeSeloPorInspecao;
    private Random random;

    public EstacaoInspecao(String nome, double capacidadeMax, double custoOperacao,
                           double probabilidadeFalha, double fator, double quantidadeSeloPorInspecao) {
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
        exigirLigada();

        if (verificarFalha()) {
            desligar();
            throw new MaquinaIndisponivelException(
                    "Falha técnica na " + getNome() + " durante a inspeção de "
                    + produto.getId() + ". A bancada foi desligada por segurança e a placa "
                    + "ficou sem selo. Tente fabricar novamente.");
        }

        exigirCapacidade(quantidadeSeloPorInspecao);
        mp.exigirDisponibilidade(quantidadeSeloPorInspecao);

        mp.consumir(quantidadeSeloPorInspecao);

        double chanceRejeicao = (produto.getQualidade() * fator) + produto.getProbabilidadeFalhaAcumulada();

        if (random.nextDouble() < chanceRejeicao) {
            produto.setStatus("defeituoso");
            System.out.println("Produto " + produto.getId()
                    + " rejeitado na inspeção (critério de qualidade).");
        } else {
            produto.aprovar();
            System.out.println("Produto " + produto.getId()
                    + " aprovado na inspeção! Selo de cera aplicado.");
        }
    }

    public double getQuantidadeSeloPorInspecao() {
        return quantidadeSeloPorInspecao;
    }
}
