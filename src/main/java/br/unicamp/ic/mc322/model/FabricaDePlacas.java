package br.unicamp.ic.mc322.model;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;

/**
 * Fábrica dos tipos de placa produzidos pela planta. Concentra em um só lugar a correspondência
 * entre o nome do tipo (usado nas demandas, no menu e no arquivo salvo) e a subclasse de
 * {@link Produto}, com o consumo de laminado de cada uma. Assim, o gerenciador de produção e a
 * persistência criam placas da mesma forma, sem duplicar essa regra.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public final class FabricaDePlacas {

    /** Nome do tipo {@link PlacaSimples}. */
    public static final String PLACA_SIMPLES = "Placa Simples";
    /** Nome do tipo {@link PlacaDupla}. */
    public static final String PLACA_DUPLA = "Placa Dupla";
    /** Nome do tipo {@link PlacaMultiCamada}. */
    public static final String PLACA_MULTI = "Placa Multi-camada";

    private static final double LAMINADO_SIMPLES = 4.0;
    private static final double LAMINADO_DUPLA = 12.0;
    private static final double LAMINADO_MULTI = 30.0;

    /**
     * Classe utilitária: não deve ser instanciada.
     */
    private FabricaDePlacas() {
    }

    /**
     * Indica se o tipo é fabricado pela planta (ignora maiúsculas e minúsculas).
     *
     * @param tipo tipo a verificar
     * @return {@code true} se o tipo é conhecido
     */
    public static boolean ehTipoConhecido(String tipo) {
        if (tipo == null) return false;
        switch (tipo.toLowerCase()) {
            case "placa simples":
            case "placa dupla":
            case "placa multi-camada":
            case "placa multicamada":
                return true;
            default:
                return false;
        }
    }

    /**
     * Cria uma placa do tipo pedido com o identificador informado.
     *
     * @param tipo tipo da placa (ex.: {@code "Placa Dupla"})
     * @param id   identificador da placa (ex.: {@code "PCB-3"})
     * @return a nova placa, aguardando processamento
     * @throws DemandaInvalidaException se o tipo é desconhecido
     */
    public static Produto criar(String tipo, String id) {
        if (tipo != null) {
            switch (tipo.toLowerCase()) {
                case "placa simples":
                    return new PlacaSimples(id, PLACA_SIMPLES, LAMINADO_SIMPLES);
                case "placa dupla":
                    return new PlacaDupla(id, PLACA_DUPLA, LAMINADO_DUPLA);
                case "placa multi-camada":
                case "placa multicamada":
                    return new PlacaMultiCamada(id, PLACA_MULTI, LAMINADO_MULTI);
                default:
                    break;
            }
        }
        throw new DemandaInvalidaException("Tipo de produto desconhecido: " + tipo);
    }
}
