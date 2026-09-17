public class PlacaMultiCamada extends Produto {
    public PlacaMultiCamada(String id, String nome, double quantidadeMateriaPrimaNecessaria) {
        super(id, nome, quantidadeMateriaPrimaNecessaria, 0.9);
    }

    @Override 
    public void processar(String idMateriaPrima) {
        setStatus("processado");
        setMateriaPrimaUsada(idMateriaPrima);
    }

    @Override 
    public double calcularTempoProducao(){
        return 50.0;
    }

    @Override 
    public String getTipo(){
        return "Placa Multi-camada";
    }
}
