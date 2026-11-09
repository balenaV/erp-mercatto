package br.com.mercatto.model.dao;

import br.com.mercatto.model.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class VendaDAO {

    private final DadosAplicacao dados;
    private final Clock relogio;
    private final List<Venda> vendas = new ArrayList<>();
    private long proximoNumero = 1;

    public VendaDAO(DadosAplicacao dados, Clock relogio) {
        this.dados = dados;
        this.relogio = relogio;
    }

    public List<Venda> listarTodos() {
        return List.copyOf(vendas);
    }

    public Venda finalizar(Pagamento pagamento) {
        Validacao.exigir(!dados.getCarrinho().isVazio(), "Adicione pelo menos um produto à venda.");
        Validacao.exigir(pagamento != null, "Selecione a forma de pagamento.");
        Cliente cliente = dados.getCarrinho().getCliente();
        Validacao.exigir(
            cliente == null || (dados.getClienteDAO().listarTodos().contains(cliente) && cliente.isAtivo()),
            "O cliente está inativo ou foi excluído. Selecione outro cliente ou consumidor não identificado."
        );
        List<ItemCarrinho> itens = dados.getCarrinho().getItens();
        for (ItemCarrinho item : itens) {
            Produto produto = item.getProduto();
            Validacao.exigir(
                dados.getProdutoDAO().listarTodos().contains(produto) && produto.isAtivo(),
                "Produto indisponível: " + produto.getNome() + ". Remova-o do carrinho."
            );
            Validacao.exigir(
                produto.getEstoque() >= item.getQuantidade(),
                "Estoque insuficiente para " + produto.getNome() + ". Disponível: " + produto.getEstoque() + "."
            );
        }
        // Construção e pagamento podem falhar; nenhuma baixa de estoque ocorreu ainda.
        Venda venda = new Venda(
            proximoNumero,
            LocalDateTime.now(relogio),
            cliente,
            itens
                .stream()
                .map(i ->
                    new ItemVenda(
                        i.getProduto().getId(),
                        i.getProduto().getCodigo(),
                        i.getProduto().getNome(),
                        i.getQuantidade(),
                        i.getPrecoUnitario()
                    )
                )
                .toList(),
            pagamento
        );
        List<Integer> estoques = itens
            .stream()
            .map(i -> i.getProduto().getEstoque())
            .toList();
        try {
            for (ItemCarrinho item : itens) item.getProduto().baixarEstoque(item.getQuantidade());
            vendas.add(venda);
            proximoNumero++;
        } catch (RuntimeException e) {
            for (int i = 0; i < itens.size(); i++) itens.get(i).getProduto().setEstoque(estoques.get(i));
            throw e;
        }
        dados.getCarrinho().limpar();
        return venda;
    }

    public List<Venda> consultar(LocalDate inicio, LocalDate fim, Cliente cliente) {
        Validacao.exigir(
            inicio == null || fim == null || !inicio.isAfter(fim),
            "A data inicial deve ser anterior ou igual à data final."
        );
        return vendas
            .stream()
            .filter(v -> inicio == null || !v.getData().toLocalDate().isBefore(inicio))
            .filter(v -> fim == null || !v.getData().toLocalDate().isAfter(fim))
            .filter(v -> cliente == null || Long.valueOf(cliente.getId()).equals(v.getClienteId()))
            .sorted(Comparator.comparingLong(Venda::getNumero).reversed())
            .toList();
    }
}
