/**
 * Indica que um valor informado pelo usuário é inválido (quantidade não positiva, índice fora do
 * intervalo, nome de matéria-prima desconhecido etc.).
 * <p>
 * É a categoria dos <b>erros de entrada</b>: nada no estado da planta está errado, apenas o dado
 * recebido não pode ser aceito.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class EntradaInvalidaException extends PlantaException {

    /**
     * Cria a exceção de entrada inválida.
     *
     * @param mensagem descrição do valor recusado e do que é esperado
     */
    public EntradaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
