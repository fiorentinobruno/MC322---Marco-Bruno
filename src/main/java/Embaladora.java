import java.util.Random;

/**
 * Segunda etapa da linha: sela a placa corroída em um saco antiestático (ESD).
 * Consome uma quantidade fixa de sacos por embalagem e pode introduzir uma imperfeição.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class Embaladora extends Maquina {
    private double chanceAumentarFalha;
    private double quantidadeEsdBagPorEmbalagem;
    private Random random;

    /**
     * Cria uma embaladora (probabilidade-base de falha 2% e reparo de R$ 80).
     *
     * @param nome                         nome de exibição
     * @param capacidadeMax                maior quantidade de sacos aceita por embalagem
     * @param custoOperacao                custo, em reais, de cada embalagem
     * @param chanceAumentarFalha          chance (0 a 1) de a embalagem introduzir 10% de risco de defeito
     * @param quantidadeEsdBagPorEmbalagem sacos antiestáticos consumidos em cada embalagem
     */
    public Embaladora(String nome, double capacidadeMax, double custoOperacao, double chanceAumentarFalha, double quantidadeEsdBagPorEmbalagem) {
        super(nome, capacidadeMax, 0.02, custoOperacao, 80);
        this.chanceAumentarFalha = chanceAumentarFalha;
        this.quantidadeEsdBagPorEmbalagem = quantidadeEsdBagPorEmbalagem;
        this.random = new Random();
    }

    /**
     * Retorna o tipo da máquina.
     *
     * @return {@code "Embaladora"}
     */
    @Override
    public String getTipo() {
        return "Embaladora";
    }

    /**
     * Verifica se a embalagem cabe na máquina e se há sacos antiestáticos suficientes. Não altera nada.
     *
     * @param mp      sacos antiestáticos a serem consumidos
     * @param produto placa a ser embalada
     * @throws MaquinaIndisponivelException se a quantidade de sacos excede a capacidade da embaladora
     * @throws RecursoInsuficienteException se o estoque de sacos é insuficiente
     */
    @Override
    public void verificarRecursos(MateriaPrima mp, Produto produto) {
        exigirCapacidade(quantidadeEsdBagPorEmbalagem);
        mp.exigirDisponibilidade(quantidadeEsdBagPorEmbalagem);
    }

    /**
     * Embala a placa em saco antiestático, consumindo o material e aplicando desgaste.
     * Todas as verificações ocorrem antes de qualquer consumo.
     *
     * @param mp      sacos antiestáticos a serem consumidos
     * @param produto placa a ser embalada
     * @throws MaquinaIndisponivelException se a embaladora está desligada, sofre falha técnica ou a capacidade é excedida
     * @throws RecursoInsuficienteException se o estoque de sacos é insuficiente
     */
    @Override
    public void processar(MateriaPrima mp, Produto produto) {
        exigirLigada();
        exigirSemFalhaTecnica(produto, "A embalagem");
        verificarRecursos(mp, produto);

        aplicarDesgaste();

        mp.consumir(quantidadeEsdBagPorEmbalagem);
        produto.setStatus("embalado");
        System.out.println("Embalando " + produto.getNome() + " (" + produto.getId() + ") em saco antiestático...");

        if (random.nextDouble() < chanceAumentarFalha) {
            produto.aumentarProbabilidadeFalha(0.1);
            System.out.println("Atenção: possível imperfeição na embalagem de " + produto.getId() + ".");
        }
    }
}
