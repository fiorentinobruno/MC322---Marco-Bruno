/**
 * Raiz da hierarquia de exceções de domínio da planta industrial.
 * <p>
 * Representa qualquer situação anormal <b>previsível</b> pelas regras de negócio (entrada inválida,
 * falta de recurso, máquina indisponível etc.). A camada de interação com o usuário ({@link Main})
 * captura esta exceção, exibe a mensagem e devolve o usuário ao menu, sem encerrar o programa.
 * <p>
 * É uma exceção não verificada ({@link RuntimeException}) para não poluir a assinatura de todos os
 * métodos do domínio; os métodos que a lançam a documentam com {@code @throws}.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class PlantaException extends RuntimeException {

    /** Texto complementar sobre o estado do sistema após o erro; vazio quando não há. */
    private String observacao = "";

    /**
     * Cria a exceção com uma mensagem destinada ao usuário final.
     *
     * @param mensagem explicação clara do que aconteceu e, quando possível, como corrigir
     */
    public PlantaException(String mensagem) {
        super(mensagem);
    }

    /**
     * Anexa uma observação sobre o estado do sistema após o erro (por exemplo, que nada foi
     * alterado). A observação é exibida junto da mensagem pela interface.
     *
     * @param observacao texto complementar para o usuário
     * @return esta própria exceção, para permitir {@code throw e.comObservacao(...)}
     */
    public PlantaException comObservacao(String observacao) {
        this.observacao = observacao;
        return this;
    }

    /**
     * Retorna a observação complementar associada ao erro.
     *
     * @return a observação, ou uma string vazia quando não há nenhuma
     */
    public String getObservacao() {
        return observacao;
    }
}
