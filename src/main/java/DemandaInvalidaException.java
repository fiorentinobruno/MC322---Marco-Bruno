/**
 * Indica que uma operação não pode ser aplicada a uma demanda: ela não existe, não tem unidades
 * pendentes, está cancelada/concluída ou o tipo de produto é desconhecido.
 * <p>
 * É um <b>erro de regra de negócio</b>: a entrada é bem formada, mas incompatível com o estado
 * atual da demanda.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class DemandaInvalidaException extends PlantaException {

    /**
     * Cria a exceção de demanda inválida.
     *
     * @param mensagem motivo pelo qual a demanda não pode ser processada
     */
    public DemandaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
