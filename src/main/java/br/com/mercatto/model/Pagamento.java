package br.com.mercatto.model;

import java.math.BigDecimal;

/** Contrato comum usado pela venda, independentemente da forma de pagamento. */
public abstract class Pagamento {

    private final BigDecimal valorRecebido;

    protected Pagamento(BigDecimal valorRecebido) {
        this.valorRecebido = Validacao.dinheiro(valorRecebido, true);
    }

    public BigDecimal getValorRecebido() {
        return valorRecebido;
    }

    public abstract FormaPagamento getForma();

    public abstract void validar(BigDecimal total);

    public BigDecimal calcularTroco(BigDecimal total) {
        return new BigDecimal("0.00");
    }

    protected final void validarValorExato(BigDecimal total) {
        Validacao.exigir(valorRecebido.compareTo(total) == 0, "O pagamento deve corresponder ao total da venda.");
    }
}
