package br.unicamp.ic.mc322.exception;

/**
 * Falha ao salvar ou carregar o estado da planta em arquivo.
 * <p>
 * Segue a mesma hierarquia da Tarefa 4 (é uma {@link PlantaException}, não verificada), então é
 * exibida pelo {@code Main} como qualquer outro erro previsto. O {@link Motivo} identifica a
 * situação de forma específica, o que permite ao programa (e aos testes) reagir a cada caso sem
 * depender do texto da mensagem.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class PersistenciaException extends PlantaException {

    /**
     * Situações de falha de persistência tratadas pela aplicação.
     */
    public enum Motivo {
        /** O arquivo de persistência não existe. */
        ARQUIVO_INEXISTENTE,
        /** O arquivo existe, mas está vazio. */
        ARQUIVO_VAZIO,
        /** O conteúdo não é um XML bem formado ou não tem a estrutura esperada. */
        FORMATO_INVALIDO,
        /** O XML é válido, mas contém valores incompatíveis com as regras do domínio. */
        DADOS_INVALIDOS,
        /** Erro do sistema ao ler o arquivo. */
        FALHA_LEITURA,
        /** Erro do sistema ao gravar o arquivo. */
        FALHA_ESCRITA
    }

    private final Motivo motivo;

    /**
     * Cria a exceção com o motivo e a mensagem informados.
     *
     * @param motivo   situação que causou a falha
     * @param mensagem descrição do problema, para o usuário
     */
    public PersistenciaException(Motivo motivo, String mensagem) {
        super(mensagem);
        this.motivo = motivo;
    }

    /**
     * Cria a exceção preservando a causa original (por exemplo, a {@code IOException}).
     *
     * @param motivo   situação que causou a falha
     * @param mensagem descrição do problema, para o usuário
     * @param causa    exceção original
     */
    public PersistenciaException(Motivo motivo, String mensagem, Throwable causa) {
        super(mensagem);
        this.motivo = motivo;
        initCause(causa);
    }

    /**
     * Retorna a situação que causou a falha.
     *
     * @return o motivo
     */
    public Motivo getMotivo() {
        return motivo;
    }
}
