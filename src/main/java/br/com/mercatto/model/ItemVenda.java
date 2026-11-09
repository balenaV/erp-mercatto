package br.com.mercatto.model;

import java.math.BigDecimal;

/** Guarda os dados do item no momento da venda, sem alterações posteriores. */
public final class ItemVenda {

    private final long produtoId;
    private final String codigo;
    private final String nome;
    private final int quantidade;
    private final BigDecimal precoUnitario;

    public ItemVenda(long produtoId, String codigo, String nome, int quantidade, BigDecimal precoUnitario) {
        this.produtoId = produtoId;
        this.codigo = Validacao.texto(codigo, "o código", 40);
        this.nome = Validacao.texto(nome, "a descrição do produto", 120);
        this.quantidade = Validacao.quantidade(quantidade);
        this.precoUnitario = Validacao.dinheiro(precoUnitario, false);
    }

    public long getProdutoId() {
        return produtoId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
