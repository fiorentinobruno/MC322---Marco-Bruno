public class PlacaSimples extends Produto {
    public PlacaSimples(String id, String nome, double quantidadeMateriaPrimaNecessaria) {
        super(id, nome, quantidadeMateriaPrimaNecessaria, 0.5);
    }

    @Override 
    public void processar(String idMateriaPrima) {
        setStatus("processado");
        setMateriaPrimaUsada(idMateriaPrima);
    }

    @Override 
    public double calcularTempoProducao(){
        return 15.0;
    }

    @Override 
    public String getTipo(){
        return "Placa Simples";
    }
}
