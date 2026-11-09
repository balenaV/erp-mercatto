package br.com.mercatto.model.dao;

import br.com.mercatto.model.Carrinho;
import java.time.Clock;

/** Uma instância por execução, compartilhada explicitamente entre as telas. */
public final class DadosAplicacao {

    private final Carrinho carrinho;
    private final ProdutoDAO produtoDAO;
    private final ClienteDAO clienteDAO;
    private final VendaDAO vendaDAO;

    public DadosAplicacao() {
        this(Clock.systemDefaultZone());
    }

    public DadosAplicacao(Clock relogio) {
        carrinho = new Carrinho();
        produtoDAO = new ProdutoDAO(this);
        clienteDAO = new ClienteDAO(this);
        vendaDAO = new VendaDAO(this, relogio);
    }

    public Carrinho getCarrinho() {
        return carrinho;
    }

    public ProdutoDAO getProdutoDAO() {
        return produtoDAO;
    }

    public ClienteDAO getClienteDAO() {
        return clienteDAO;
    }

    public VendaDAO getVendaDAO() {
        return vendaDAO;
    }

    public boolean temDados() {
        return !produtoDAO.listarTodos().isEmpty()
            || !clienteDAO.listarTodos().isEmpty()
            || !vendaDAO.listarTodos().isEmpty();
    }
}
