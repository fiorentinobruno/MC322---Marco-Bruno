/**
 * Indica que uma máquina não consegue executar sua etapa: está quebrada, desligada, sofreu uma
 * falha técnica durante o processamento ou o lote excede sua capacidade máxima.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class MaquinaIndisponivelException extends PlantaException {

    /**
     * Cria a exceção de máquina indisponível.
     *
     * @param mensagem qual máquina falhou e por quê
     */
    public MaquinaIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
