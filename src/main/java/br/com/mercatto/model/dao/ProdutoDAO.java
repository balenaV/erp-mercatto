package br.com.mercatto.model.dao;

import br.com.mercatto.model.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProdutoDAO {

    private final DadosAplicacao dados;
    private final List<Produto> produtos = new ArrayList<>();
    private long proximoId = 1;

    public ProdutoDAO(DadosAplicacao dados) {
        this.dados = dados;
    }

    public List<Produto> listarTodos() {
        return List.copyOf(produtos);
    }

    public Produto salvar(Produto original, String codigo, String nome, BigDecimal preco, int estoque, boolean ativo) {
        // Valida uma cópia completa antes de modificar o objeto compartilhado pelas telas.
        String codigoInformado = codigo == null ? "" : codigo.strip();
        String codigoFinal = codigoInformado.isEmpty()
            ? (original == null ? gerarCodigo() : original.getCodigo())
            : codigoInformado;
        Produto candidato = new Produto(
            original == null ? 0 : original.getId(),
            codigoFinal,
            nome,
            preco,
            estoque,
            ativo
        );
        Validacao.exigir(
            produtos.stream().noneMatch(p -> p != original && p.getCodigo().equalsIgnoreCase(candidato.getCodigo())),
            "Já existe um produto com esse código."
        );
        if (original == null) {
            Produto novo = new Produto(
                proximoId++,
                candidato.getCodigo(),
                candidato.getNome(),
                candidato.getPrecoVenda(),
                estoque,
                ativo
            );
            produtos.add(novo);
            return novo;
        }
        exigirCadastrado(original);
        original.setCodigo(candidato.getCodigo());
        original.setNome(candidato.getNome());
        original.setPrecoVenda(candidato.getPrecoVenda());
        original.setEstoque(estoque);
        original.setAtivo(ativo);
        return original;
    }

    private String gerarCodigo() {
        var codigos = produtos
            .stream()
            .map(p -> p.getCodigo().toUpperCase(Locale.ROOT))
            .collect(java.util.stream.Collectors.toSet());
        long sequencia = 1;
        String codigo;
        do {
            codigo = String.format(Locale.ROOT, "PROD-%06d", sequencia++);
        } while (codigos.contains(codigo));
        return codigo;
    }

    public void alterarSituacao(Produto produto) {
        exigirCadastrado(produto);
        produto.setAtivo(!produto.isAtivo());
    }

    public void excluir(Produto produto) {
        exigirCadastrado(produto);
        Validacao.exigir(
            dados
                .getVendaDAO()
                .listarTodos()
                .stream()
                .flatMap(v -> v.getItens().stream())
                .noneMatch(i -> i.getProdutoId() == produto.getId()),
            "O produto já pertence a uma venda. Você pode inativá-lo."
        );
        Validacao.exigir(
            dados
                .getCarrinho()
                .getItens()
                .stream()
                .noneMatch(i -> i.getProduto() == produto),
            "Remova o produto do carrinho antes de excluí-lo."
        );
        produtos.remove(produto);
    }

    public List<Produto> consultar(String busca, Boolean ativo) {
        String termo = busca == null ? "" : busca.strip().toLowerCase(Locale.ROOT);
        return produtos
            .stream()
            .filter(p -> ativo == null || p.isAtivo() == ativo)
            .filter(
                p ->
                    p.getNome().toLowerCase(Locale.ROOT).contains(termo) ||
                    p.getCodigo().toLowerCase(Locale.ROOT).contains(termo)
            )
            .toList();
    }

    public void exigirCadastrado(Produto produto) {
        Validacao.exigir(produto != null && produtos.contains(produto), "O produto não está mais cadastrado.");
    }
}
