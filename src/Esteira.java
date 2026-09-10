public class Esteira {
    private Object item;
    private boolean emMovimento;
    private double capacidadeMaxima;

    public Esteira(double capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public void ligar() {
        emMovimento = true;
    }

    public void desligar() {
        emMovimento = false;
    }

    public boolean adicionarItem(Object novoItem, double quantidade) {
        if (!emMovimento) {
            System.out.println("A esteira não está em movimento. Item não transportado.");
            return false;
        }

        if (item != null) {
            System.out.println("A esteira já contém um item.");
            return false;
        }

        if (!verificarCapacidade(quantidade)) {
            System.out.println("Excede a capacidade máxima da esteira");
            return false;
        }

        item = novoItem;
        return true;
    }

    public Object removerItem() {
        Object temp = item;
        item = null;
        return temp;
    }

    public boolean verificarCapacidade(double quant) {
        return quant <= capacidadeMaxima;
    }
}