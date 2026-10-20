/**
 * Placa de circuito impresso de duas camadas, com exigência de qualidade intermediária (0.7).
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class PlacaDupla extends Produto {

    /**
     * Cria uma placa dupla.
     *
     * @param id                              identificador único da placa
     * @param nome                            nome de exibição
     * @param quantidadeMateriaPrimaNecessaria laminado consumido na corrosão
     */
    public PlacaDupla(String id, String nome, double quantidadeMateriaPrimaNecessaria) {
        super(id, nome, quantidadeMateriaPrimaNecessaria, 0.7);
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
     * Retorna o tempo de produção de uma placa dupla.
     *
     * @return 30.0 minutos
     */
    @Override
    public double calcularTempoProducao() {
        return 30.0;
    }

    /**
     * Retorna o nome do tipo.
     *
     * @return {@code "Placa Dupla"}
     */
    @Override
    public String getTipo() {
        return "Placa Dupla";
    }
}
