import java.util.List;

/**
 * Estratégia (padrão Strategy) que decide qual demanda será atendida na próxima produção.
 * O {@link GerenciadorProducao} conhece apenas esta interface e pode trocar de estratégia em
 * tempo de execução.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public interface EstrategiaProducao {

    /**
     * Escolhe a próxima demanda a produzir entre as elegíveis.
     *
     * @param demandas            demandas registradas na planta
     * @param orcamentoDisponivel orçamento atual, em reais
     * @return a demanda escolhida, ou {@code null} se nenhuma for elegível
     */
    Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel);

    /**
     * Retorna o nome de exibição da estratégia.
     *
     * @return o nome da estratégia
     */
    String getNomeEstrategia();

    /**
     * Filtro comum a todas as estratégias: a demanda deve estar pendente ou em produção, ter
     * unidades restantes e ser viável dentro do orçamento.
     *
     * @param d         demanda a avaliar
     * @param orcamento orçamento atual, em reais
     * @return {@code true} se a demanda pode ser produzida agora
     */
    default boolean ehElegivel(Demanda d, double orcamento) {
        StatusDemanda s = d.getStatus();
        return (s == StatusDemanda.PENDENTE || s == StatusDemanda.EM_PRODUCAO)
                && d.getQuantidadeProdutos() > 0
                && d.ehViavel(orcamento);
    }
}
