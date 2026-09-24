public  enum StatusDemanda{
    PENDENTE("Aguarndando início"),
    EM_PRODUCAO("Em fabricação"),
    CONCLUIDA("Finalizada com sucesso"),
    CANCELADA("Cancelada");
    
    private final String descricao;
    
    StatusDemanda(String descricao){
        this.descricao = descricao;
    }
    
    public String getDescricao(){
        return descricao;
    }
}