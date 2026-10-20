/**
 * Pedido de fabricação de um tipo de produto, com quantidade pendente e ciclo de vida definido por
 * {@link StatusDemanda}.
 * <p>
 * Regras de negócio: uma demanda cancelada não pode mais ser alterada nem atendida; uma demanda
 * concluída não pode ser cancelada; uma demanda concluída volta a ficar pendente se receber novas
 * unidades.
 *
 * @author Marco Correa e Bruno Fiorentino
 */
public class Demanda {
    private String tipoProduto;
    private int quantidadeProdutos;
    private StatusDemanda status;
    private final double custoUnitarioEstimado;

    /**
     * Cria uma demanda pendente.
     *
     * @param tipoProduto            tipo de produto solicitado (ex.: {@code "Placa Simples"})
     * @param quantidadeProdutos     unidades solicitadas; deve ser maior que zero
     * @param custoUnitarioEstimado  custo estimado de fabricar uma unidade, em reais
     * @throws EntradaInvalidaException se o tipo estiver vazio ou a quantidade não for positiva
     */
    public Demanda(String tipoProduto, int quantidadeProdutos, double custoUnitarioEstimado) {
        if (tipoProduto == null || tipoProduto.isBlank()) {
            throw new EntradaInvalidaException("O tipo de produto da demanda não pode ser vazio.");
        }
        if (quantidadeProdutos <= 0) {
            throw new EntradaInvalidaException("A quantidade da demanda deve ser maior que zero.");
        }
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = quantidadeProdutos;
        this.custoUnitarioEstimado = custoUnitarioEstimado;
        this.status = StatusDemanda.PENDENTE;
    }

    /**
     * Acrescenta unidades à demanda. Se estava concluída, volta a ficar pendente.
     *
     * @param quantidadeAdicional unidades a acrescentar; deve ser maior que zero
     * @throws EntradaInvalidaException  se a quantidade não for positiva ou o total ultrapassar o limite suportado
     * @throws DemandaInvalidaException  se a demanda foi cancelada
     */
    public void atualizarQuantidade(int quantidadeAdicional) {
        if (status == StatusDemanda.CANCELADA) {
            throw new DemandaInvalidaException("A demanda de " + tipoProduto + " foi cancelada e não pode ser alterada.");
        }
        if (quantidadeAdicional <= 0) {
            throw new EntradaInvalidaException("A quantidade a acrescentar deve ser maior que zero.");
        }
        if (quantidadeAdicional > Integer.MAX_VALUE - quantidadeProdutos) {
            throw new EntradaInvalidaException("A quantidade total da demanda ultrapassaria o limite de "
                    + Integer.MAX_VALUE + " unidades.");
        }
        quantidadeProdutos += quantidadeAdicional;

        if (status == StatusDemanda.CONCLUIDA) {
            status = StatusDemanda.PENDENTE;
        }
    }

    /**
     * Calcula a matéria-prima necessária para atender todas as unidades pendentes.
     *
     * @param demandaMateriaPrimaPorUnidade matéria-prima consumida por unidade
     * @return a matéria-prima total necessária
     */
    public double calcularMateriaPrimaNecessaria(double demandaMateriaPrimaPorUnidade) {
        return this.quantidadeProdutos * demandaMateriaPrimaPorUnidade;
    }

    /**
     * Abate unidades fabricadas da quantidade pendente; ao zerar, a demanda é concluída.
     *
     * @param unidadesFabricadas unidades fabricadas com sucesso; deve ser maior que zero
     * @throws EntradaInvalidaException se a quantidade não for positiva
     * @throws DemandaInvalidaException se a demanda não está pendente nem em produção
     */
    public void registrarProducao(int unidadesFabricadas) {
        if (unidadesFabricadas <= 0) {
            throw new EntradaInvalidaException("O número de unidades fabricadas deve ser maior que zero.");
        }
        if (status != StatusDemanda.PENDENTE && status != StatusDemanda.EM_PRODUCAO) {
            throw new DemandaInvalidaException("A demanda de " + tipoProduto + " está '"
                    + status.getDescricao() + "' e não pode receber produção.");
        }
        quantidadeProdutos -= unidadesFabricadas;
        if (quantidadeProdutos <= 0) {
            quantidadeProdutos = 0;
            atender();
        }
    }

    /**
     * Marca a demanda como concluída.
     *
     * @throws DemandaInvalidaException se a demanda foi cancelada
     */
    public void atender() {
        if (status == StatusDemanda.CANCELADA) {
            throw new DemandaInvalidaException("A demanda de " + tipoProduto + " foi cancelada e não pode ser concluída.");
        }
        status = StatusDemanda.CONCLUIDA;
    }

    /**
     * Marca a demanda como em produção, caso esteja pendente. Nos demais estados não faz nada.
     */
    public void iniciarProducao() {
        if (status == StatusDemanda.PENDENTE) {
            status = StatusDemanda.EM_PRODUCAO;
        }
    }

    /**
     * Cancela a demanda.
     *
     * @throws DemandaInvalidaException se a demanda já foi concluída
     */
    public void cancelar() {
        if (status == StatusDemanda.CONCLUIDA) {
            throw new DemandaInvalidaException("A demanda de " + tipoProduto + " já foi concluída e não pode ser cancelada.");
        }
        status = StatusDemanda.CANCELADA;
    }

    /**
     * Retorna o tipo de produto solicitado.
     *
     * @return o tipo do produto
     */
    public String getTipoProduto() {
        return tipoProduto;
    }

    /**
     * Retorna as unidades ainda pendentes.
     *
     * @return a quantidade pendente
     */
    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    /**
     * Retorna o estado atual da demanda.
     *
     * @return o status
     */
    public StatusDemanda getStatus() {
        return status;
    }

    /**
     * Estima o custo de atender todas as unidades pendentes.
     *
     * @return o custo total estimado, em reais
     */
    public double getCustoTotalEstimado() {
        return quantidadeProdutos * custoUnitarioEstimado;
    }

    /**
     * Indica se uma unidade da demanda cabe no orçamento informado.
     *
     * @param orcamento orçamento disponível, em reais
     * @return {@code true} se o custo unitário é positivo e não excede o orçamento
     */
    public boolean ehViavel(double orcamento) {
        return custoUnitarioEstimado > 0 && custoUnitarioEstimado <= orcamento;
    }

    /**
     * Retorna o custo estimado de uma unidade.
     *
     * @return o custo unitário estimado, em reais
     */
    public double getCustoUnitarioEstimado() {
        return custoUnitarioEstimado;
    }
}
