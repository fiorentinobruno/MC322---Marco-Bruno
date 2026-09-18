public class PlacaDupla extends Produto {
    public PlacaDupla(String id, String nome, double quantidadeMateriaPrimaNecessaria) {
        super(id, nome, quantidadeMateriaPrimaNecessaria, 0.7);
    }

    @Override 
    public void processar(String idMateriaPrima) {
        setStatus("processado");
        setMateriaPrimaUsada(idMateriaPrima);
    }

    @Override 
    public double calcularTempoProducao(){
        return 30.0;
    }

    @Override 
    public String getTipo(){
        return "Placa Dupla";
    }
}
