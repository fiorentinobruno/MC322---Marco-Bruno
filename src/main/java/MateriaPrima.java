/**
 * Matéria-prima mantida em estoque pela planta (laminado, saco antiestático, selo de cera).
 * <p>
 * Regra de negócio: uma operação só pode consumir o material se o estoque cobrir a quantidade
 * pedida <b>e</b> estiver no mínimo operacional ({@code quantidadeMinima}).
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class MateriaPrima {
    private String id;
    private String nome;
    private double quantidade;
    private String unidade;
    private double quantidadeMinima;
    private double custoPorUnidade;

    /**
     * Cria uma matéria-prima com estoque inicial.
     *
     * @param id               identificador curto (ex.: {@code MP1})
     * @param nome             nome de exibição
     * @param quantidade       estoque inicial
     * @param unidade          unidade de medida (ex.: {@code dm2}, {@code un})
     * @param quantidadeMinima estoque mínimo exigido para a operação poder consumir o material
     * @param custoPorUnidade  custo de compra de cada unidade, em reais
     */
    public MateriaPrima(String id, String nome, double quantidade, String unidade, double quantidadeMinima, double custoPorUnidade) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.quantidadeMinima = quantidadeMinima;
        this.custoPorUnidade = custoPorUnidade;
    }

    /**
     * Verifica, sem alterar o estoque, se é possível consumir a quantidade informada.
     *
     * @param consumido quantidade que se pretende consumir
     * @return {@code true} se o estoque cobre a quantidade e está no mínimo operacional
     */
    public boolean verificarDisponibilidade(double consumido) {
        return quantidade >= consumido && quantidade >= quantidadeMinima;
    }

    /**
     * Exige que a quantidade esteja disponível, explicando o motivo caso não esteja.
     * Não altera o estoque.
     *
     * @param necessario quantidade que será consumida
     * @throws RecursoInsuficienteException se o estoque não cobre a quantidade ou está abaixo do mínimo operacional
     */
    public void exigirDisponibilidade(double necessario) {
        if (quantidade < necessario) {
            throw new RecursoInsuficienteException(String.format(
                    "Matéria-prima insuficiente: %s precisa de %.1f %s, mas há apenas %.1f %s em estoque. "
                            + "Compre mais no menu 'Comprar matéria-prima'.",
                    nome, necessario, unidade, quantidade, unidade));
        }
        if (quantidade < quantidadeMinima) {
            throw new RecursoInsuficienteException(String.format(
                    "Estoque de %s (%.1f %s) abaixo do mínimo operacional de %.1f %s. "
                            + "Compre mais no menu 'Comprar matéria-prima'.",
                    nome, quantidade, unidade, quantidadeMinima, unidade));
        }
    }

    /**
     * Acrescenta unidades ao estoque.
     *
     * @param adicionado quantidade a acrescentar; deve ser um número positivo
     * @throws EntradaInvalidaException se a quantidade não for um número positivo e finito
     */
    public void adicionarEstoque(double adicionado) {
        if (!(adicionado > 0) || Double.isInfinite(adicionado)) {
            throw new EntradaInvalidaException("A quantidade a adicionar ao estoque deve ser um número maior que zero.");
        }
        quantidade += adicionado;
    }

    /**
     * Retira unidades do estoque.
     *
     * @param consumido quantidade a retirar; não pode ser negativa
     * @throws EntradaInvalidaException     se a quantidade for negativa ou inválida
     * @throws RecursoInsuficienteException se o estoque não cobre a quantidade
     */
    public void consumir(double consumido) {
        if (!(consumido >= 0)) {
            throw new EntradaInvalidaException("A quantidade a consumir não pode ser negativa.");
        }
        exigirDisponibilidade(consumido);
        quantidade -= consumido;
    }

    /**
     * Retorna o identificador da matéria-prima.
     *
     * @return o id (ex.: {@code MP1})
     */
    public String getId() {
        return id;
    }

    /**
     * Retorna o nome de exibição.
     *
     * @return o nome da matéria-prima
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o estoque atual.
     *
     * @return a quantidade em estoque, na unidade da matéria-prima
     */
    public double getQuantidade() {
        return quantidade;
    }

    /**
     * Retorna a unidade de medida.
     *
     * @return a unidade (ex.: {@code dm2}, {@code un})
     */
    public String getUnidade() {
        return unidade;
    }

    /**
     * Retorna o custo de compra de uma unidade.
     *
     * @return o custo por unidade, em reais
     */
    public double getCustoPorUnidade() {
        return custoPorUnidade;
    }
}
