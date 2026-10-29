/**
 * Indica que o orçamento (budget) atual da planta não cobre o custo da operação solicitada
 * (produção, compra de matéria-prima ou reparo de máquina).
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class OrcamentoInsuficienteException extends PlantaException {

    /**
     * Cria a exceção de orçamento insuficiente.
     *
     * @param mensagem custo necessário e orçamento disponível
     */
    public OrcamentoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
