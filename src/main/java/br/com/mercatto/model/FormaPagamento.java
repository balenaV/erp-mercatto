package br.com.mercatto.model;

public enum FormaPagamento {
    DINHEIRO("Dinheiro"),
    CARTAO("Cartão"),
    PIX("PIX");

    private final String descricao;

    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
