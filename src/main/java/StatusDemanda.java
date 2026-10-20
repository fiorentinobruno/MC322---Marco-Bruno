/**
 * Estados possíveis do ciclo de vida de uma {@link Demanda}.
 * <p>
 * Transições válidas: {@code PENDENTE} para {@code EM_PRODUCAO} para {@code CONCLUIDA};
 * qualquer estado exceto {@code CONCLUIDA} pode ir para {@code CANCELADA}; uma demanda
 * {@code CONCLUIDA} volta a {@code PENDENTE} se receber novas unidades.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public enum StatusDemanda {
    /** Registrada, ainda sem nenhuma unidade em fabricação. */
    PENDENTE("Aguardando início"),
    /** Pelo menos uma unidade já foi fabricada e ainda restam unidades pendentes. */
    EM_PRODUCAO("Em fabricação"),
    /** Todas as unidades solicitadas foram fabricadas. */
    CONCLUIDA("Finalizada com sucesso"),
    /** Encerrada sem atendimento (por exemplo, por falta de orçamento); não pode ser alterada. */
    CANCELADA("Cancelada");

    private final String descricao;

    StatusDemanda(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna o texto amigável do estado, usado nas listagens.
     *
     * @return a descrição do estado
     */
    public String getDescricao() {
        return descricao;
    }
}
