/**
 * Placa de circuito impresso de uma camada: o produto mais barato e com a menor exigência de
 * qualidade (0.5).
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class PlacaSimples extends Produto {

    /**
     * Cria uma placa simples.
     *
     * @param id                              identificador único da placa
     * @param nome                            nome de exibição
     * @param quantidadeMateriaPrimaNecessaria laminado consumido na corrosão
     */
    public PlacaSimples(String id, String nome, double quantidadeMateriaPrimaNecessaria) {
        super(id, nome, quantidadeMateriaPrimaNecessaria, 0.5);
    }

    /**
     * Marca a placa como processada e registra a matéria-prima usada.
     *
     * @param idMateriaPrima identificador da matéria-prima utilizada
     */
    @Override
    public void processar(String idMateriaPrima) {
        setStatus("processado");
        setMateriaPrimaUsada(idMateriaPrima);
    }

    /**
     * Retorna o tempo de produção de uma placa simples.
     *
     * @return 15.0 minutos
     */
    @Override
    public double calcularTempoProducao() {
        return 15.0;
    }

    /**
     * Retorna o nome do tipo.
     *
     * @return {@code "Placa Simples"}
     */
    @Override
    public String getTipo() {
        return "Placa Simples";
    }
}
