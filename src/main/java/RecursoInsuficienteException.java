/**
 * Indica falta de matéria-prima em estoque para realizar uma operação.
 * <p>
 * Quando lançada durante uma produção, nenhum estoque, orçamento ou demanda foi alterado.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class RecursoInsuficienteException extends PlantaException {

    /**
     * Cria a exceção de recurso insuficiente.
     *
     * @param mensagem qual matéria-prima falta, quanto é necessário e quanto há disponível
     */
    public RecursoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
