package br.com.mercatto.model;

import java.math.BigDecimal;

public final class PagamentoCartao extends Pagamento {

    public PagamentoCartao(BigDecimal valor) {
        super(valor);
    }

    @Override
    public FormaPagamento getForma() {
        return FormaPagamento.CARTAO;
    }

    @Override
    public void validar(BigDecimal total) {
        validarValorExato(total);
    }
}
