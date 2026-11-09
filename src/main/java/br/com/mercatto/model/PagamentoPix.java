package br.com.mercatto.model;

import java.math.BigDecimal;

public final class PagamentoPix extends Pagamento {

    public PagamentoPix(BigDecimal valor) {
        super(valor);
    }

    @Override
    public FormaPagamento getForma() {
        return FormaPagamento.PIX;
    }

    @Override
    public void validar(BigDecimal total) {
        validarValorExato(total);
    }
}
