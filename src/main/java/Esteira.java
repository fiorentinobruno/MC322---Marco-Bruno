/**
 * Esteira que transporta um item por vez entre as etapas da linha, respeitando uma capacidade máxima.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class Esteira {
    private Object item;
    private boolean emMovimento;
    private double capacidadeMaxima;

    /**
     * Cria uma esteira parada e vazia.
     *
     * @param capacidadeMaxima maior quantidade que a esteira consegue transportar de uma vez
     */
    public Esteira(double capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
    }

    /**
     * Coloca a esteira em movimento.
     */
    public void ligar() {
        emMovimento = true;
    }

    /**
     * Para a esteira.
     */
    public void desligar() {
        emMovimento = false;
    }

    /**
     * Coloca um item na esteira.
     *
     * @param novoItem   item a transportar
     * @param quantidade quantidade que o item representa
     * @throws MaquinaIndisponivelException se a esteira está parada, já contém um item ou a quantidade excede a capacidade
     */
    public void adicionarItem(Object novoItem, double quantidade) {
        if (!emMovimento) {
            throw new MaquinaIndisponivelException("A esteira não está em movimento. Item não transportado.");
        }

        if (item != null) {
            throw new MaquinaIndisponivelException("A esteira já contém um item.");
        }

        if (!verificarCapacidade(quantidade)) {
            throw new MaquinaIndisponivelException("Excede a capacidade máxima da esteira (" + capacidadeMaxima + ").");
        }

        item = novoItem;
    }

    /**
     * Retira o item da esteira.
     *
     * @return o item que estava na esteira, ou {@code null} se estava vazia
     */
    public Object removerItem() {
        Object temp = item;
        item = null;
        return temp;
    }

    /**
     * Verifica se uma quantidade cabe na esteira.
     *
     * @param quant quantidade a verificar
     * @return {@code true} se a quantidade não excede a capacidade máxima
     */
    public boolean verificarCapacidade(double quant) {
        return quant <= capacidadeMaxima;
    }
}
