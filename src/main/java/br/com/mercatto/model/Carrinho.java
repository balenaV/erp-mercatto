package br.com.mercatto.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class Carrinho {

    private final List<ItemCarrinho> itens = new ArrayList<>();
    private Cliente cliente;

    public List<ItemCarrinho> getItens() {
        return List.copyOf(itens);
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        Validacao.exigir(cliente == null || cliente.isAtivo(), "O cliente está inativo.");
        this.cliente = cliente;
    }

    public boolean isVazio() {
        return itens.isEmpty();
    }

    public void adicionar(Produto produto, int quantidade) {
        Validacao.exigir(produto != null, "Selecione um produto.");
        Validacao.exigir(produto.isAtivo(), "O produto está inativo.");
        Validacao.quantidade(quantidade);
        ItemCarrinho atual = itens
            .stream()
            .filter(i -> i.getProduto() == produto)
            .findFirst()
            .orElse(null);
        long novaQuantidade = (atual == null ? 0L : atual.getQuantidade()) + quantidade;
        Validacao.exigir(
            novaQuantidade <= produto.getEstoque(),
            "Estoque insuficiente para " + produto.getNome() + ". Disponível: " + produto.getEstoque() + "."
        );
        if (atual == null) itens.add(new ItemCarrinho(produto, quantidade));
        else atual.setQuantidade((int) novaQuantidade);
    }

    public void alterarQuantidade(ItemCarrinho item, int quantidade) {
        Validacao.exigir(itens.contains(item), "Selecione um item do carrinho.");
        Validacao.quantidade(quantidade);
        Validacao.exigir(item.getProduto().isAtivo(), "O produto está inativo. Remova-o do carrinho.");
        Validacao.exigir(quantidade <= item.getProduto().getEstoque(), "A quantidade excede o estoque disponível.");
        item.setQuantidade(quantidade);
    }

    public void remover(ItemCarrinho item) {
        itens.remove(item);
    }

    public void limparItens() {
        itens.clear();
    }

    public void limpar() {
        itens.clear();
        cliente = null;
    }

    public BigDecimal getTotal() {
        return itens.stream().map(ItemCarrinho::getSubtotal).reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    public long getQuantidadeTotal() {
        return itens.stream().mapToLong(ItemCarrinho::getQuantidade).sum();
    }
}
