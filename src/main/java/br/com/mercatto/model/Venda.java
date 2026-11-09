package br.com.mercatto.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class Venda {

    private final long numero;
    private final LocalDateTime data;
    private final Long clienteId;
    private final String clienteNome;
    private final String clienteDocumento;
    private final List<ItemVenda> itens;
    private final FormaPagamento formaPagamento;
    private final BigDecimal valorRecebido;
    private final BigDecimal troco;
    private final BigDecimal total;

    public Venda(long numero, LocalDateTime data, Cliente cliente, List<ItemVenda> itens, Pagamento pagamento) {
        Validacao.exigir(itens != null && !itens.isEmpty(), "A venda deve possuir itens.");
        this.numero = numero;
        this.data = Objects.requireNonNull(data);
        this.clienteId = cliente == null ? null : cliente.getId();
        this.clienteNome = cliente == null ? "Consumidor não identificado" : cliente.getNome();
        this.clienteDocumento = cliente == null ? "—" : cliente.getDocumento();
        this.itens = List.copyOf(itens);
        this.total = itens.stream().map(ItemVenda::getSubtotal).reduce(new BigDecimal("0.00"), BigDecimal::add);
        pagamento.validar(total);
        this.formaPagamento = pagamento.getForma();
        this.valorRecebido = pagamento.getValorRecebido();
        this.troco = pagamento.calcularTroco(total);
    }

    public long getNumero() {
        return numero;
    }

    public LocalDateTime getData() {
        return data;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public String getClienteDocumento() {
        return clienteDocumento;
    }

    public List<ItemVenda> getItens() {
        return itens;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public BigDecimal getValorRecebido() {
        return valorRecebido;
    }

    public BigDecimal getTroco() {
        return troco;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
