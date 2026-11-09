package br.com.mercatto.model;

import java.math.BigDecimal;

public final class PagamentoDinheiro extends Pagamento {

    public PagamentoDinheiro(BigDecimal valorRecebido) {
        super(valorRecebido);
    }

    @Override
    public FormaPagamento getForma() {
        return FormaPagamento.DINHEIRO;
    }

    @Override
    public void validar(BigDecimal total) {
        Validacao.exigir(getValorRecebido().compareTo(total) >= 0, "O valor recebido é menor que o total da venda.");
    }

    @Override
    public BigDecimal calcularTroco(BigDecimal total) {
        validar(total);
        return getValorRecebido().subtract(total);
    }
}
