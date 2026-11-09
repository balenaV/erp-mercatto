package br.com.mercatto.model;

import java.math.BigDecimal;

public final class Produto {

    private final long id;
    private String codigo;
    private String nome;
    private BigDecimal precoVenda;
    private int estoque;
    private boolean ativo;

    public Produto(long id, String codigo, String nome, BigDecimal precoVenda, int estoque, boolean ativo) {
        this.id = id;
        setCodigo(codigo);
        setNome(nome);
        setPrecoVenda(precoVenda);
        setEstoque(estoque);
        setAtivo(ativo);
    }

    public long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = Validacao.texto(codigo, "o código", 40);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validacao.texto(nome, "a descrição do produto", 120);
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = Validacao.dinheiro(precoVenda, false);
    }

    public int getEstoque() {
        return estoque;
    }

    public void setEstoque(int estoque) {
        Validacao.exigir(estoque >= 0, "O estoque deve ser um número inteiro igual ou maior que zero.");
        this.estoque = estoque;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public void baixarEstoque(int quantidade) {
        Validacao.quantidade(quantidade);
        Validacao.exigir(estoque >= quantidade, "Estoque insuficiente para " + nome + ". Disponível: " + estoque + ".");
        estoque -= quantidade;
    }

    @Override
    public String toString() {
        return codigo + " — " + nome;
    }
}
