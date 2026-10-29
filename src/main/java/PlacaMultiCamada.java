/**
 * Placa de circuito impresso com várias camadas: o produto mais complexo, com a maior exigência
 * de qualidade (0.9) e, portanto, a inspeção mais rigorosa.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class PlacaMultiCamada extends Produto {

    /**
     * Cria uma placa multi-camada.
     *
     * @param id                              identificador único da placa
     * @param nome                            nome de exibição
     * @param quantidadeMateriaPrimaNecessaria laminado consumido na corrosão
     */
    public PlacaMultiCamada(String id, String nome, double quantidadeMateriaPrimaNecessaria) {
        super(id, nome, quantidadeMateriaPrimaNecessaria, 0.9);
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
     * Retorna o tempo de produção de uma placa multi-camada.
     *
     * @return 50.0 minutos
     */
    @Override
    public double calcularTempoProducao() {
        return 50.0;
    }

    /**
     * Retorna o nome do tipo.
     *
     * @return {@code "Placa Multi-camada"}
     */
    @Override
    public String getTipo() {
        return "Placa Multi-camada";
    }
}
