package br.com.mercatto.model;

import java.math.BigDecimal;

public final class ItemCarrinho {

    private final Produto produto;
    private final BigDecimal precoUnitario;
    private int quantidade;

    ItemCarrinho(Produto produto, int quantidade) {
        this.produto = produto;
        this.precoUnitario = produto.getPrecoVenda();
        setQuantidade(quantidade);
    }

    public Produto getProduto() {
        return produto;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public int getQuantidade() {
        return quantidade;
    }

    void setQuantidade(int quantidade) {
        this.quantidade = Validacao.quantidade(quantidade);
    }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
