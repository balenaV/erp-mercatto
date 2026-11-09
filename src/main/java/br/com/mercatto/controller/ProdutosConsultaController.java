package br.com.mercatto.controller;

import br.com.mercatto.model.Produto;
import br.com.mercatto.model.dao.*;
import br.com.mercatto.util.*;
import java.math.BigDecimal;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public final class ProdutosConsultaController {

    private PrincipalController principal;
    private ProdutoDAO produtoDAO;

    @FXML
    private TextField campoBuscaProduto;

    @FXML
    private ComboBox<String> filtroSituacaoProduto;

    @FXML
    private TableView<Produto> tabelaProdutos;

    @FXML
    private TableColumn<Produto, String> colunaCodigo, colunaNome, colunaSituacao;

    @FXML
    private TableColumn<Produto, BigDecimal> colunaPreco;

    @FXML
    private TableColumn<Produto, Integer> colunaEstoque;

    @FXML
    private Button btnEditarProduto, btnSituacaoProduto, btnExcluirProduto, btnVenderProduto;

    public void setPrincipal(PrincipalController principal) {
        this.principal = principal;
        this.produtoDAO = principal.getDados().getProdutoDAO();
        atualizar();
    }

    @FXML
    private void initialize() {
        filtroSituacaoProduto.getItems().setAll("Todas as situações", "Ativos", "Inativos");
        filtroSituacaoProduto.setValue("Todas as situações");
        Tabelas.coluna(colunaCodigo, Produto::getCodigo);
        Tabelas.coluna(colunaNome, Produto::getNome);
        Tabelas.coluna(colunaPreco, Produto::getPrecoVenda, Formatos::moeda);
        Tabelas.coluna(colunaEstoque, Produto::getEstoque);
        Tabelas.coluna(colunaSituacao, p -> p.isAtivo() ? "Ativo" : "Inativo");
        var semSelecao = tabelaProdutos.getSelectionModel().selectedItemProperty().isNull();
        btnEditarProduto.disableProperty().bind(semSelecao);
        btnSituacaoProduto.disableProperty().bind(semSelecao);
        btnExcluirProduto.disableProperty().bind(semSelecao);
        tabelaProdutos
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((o, a, b) -> btnVenderProduto.setDisable(b == null || !b.isAtivo() || b.getEstoque() == 0));
        Tabelas.duploClique(tabelaProdutos, this::editar);
        campoBuscaProduto.textProperty().addListener((o, a, b) -> atualizar());
        filtroSituacaoProduto.valueProperty().addListener((o, a, b) -> atualizar());
        atualizar();
    }

    public void atualizar() {
        if (produtoDAO == null) return;
        Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();
        Boolean ativo = null;
        if ("Ativos".equals(filtroSituacaoProduto.getValue())) ativo = true;
        else if ("Inativos".equals(filtroSituacaoProduto.getValue())) ativo = false;
        tabelaProdutos.getItems().setAll(produtoDAO.consultar(campoBuscaProduto.getText(), ativo));
        if (tabelaProdutos.getItems().contains(selecionado)) tabelaProdutos.getSelectionModel().select(selecionado);
        else tabelaProdutos.getSelectionModel().clearSelection();
        tabelaProdutos.refresh();
        btnVenderProduto.setDisable(
            selecionado == null ||
                !tabelaProdutos.getItems().contains(selecionado) ||
                !selecionado.isAtivo() ||
                selecionado.getEstoque() == 0
        );
    }

    @FXML
    private void buscar() {
        atualizar();
    }

    @FXML
    private void novo() {
        principal.executar(() -> principal.produto(null, this::selecionarSalvo));
    }

    @FXML
    private void editar() {
        Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (selecionado != null) principal.executar(() -> principal.produto(selecionado, this::selecionarSalvo));
    }

    private void selecionarSalvo(Produto produto) {
        campoBuscaProduto.clear();
        filtroSituacaoProduto.setValue("Todas as situações");
        atualizar();
        tabelaProdutos.getSelectionModel().select(produto);
        tabelaProdutos.scrollTo(produto);
    }

    @FXML
    private void alterarSituacao() {
        principal.executar(() -> {
            produtoDAO.alterarSituacao(tabelaProdutos.getSelectionModel().getSelectedItem());
            atualizar();
        });
    }

    @FXML
    private void excluir() {
        Produto produto = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (
            produto != null && principal.getDialogos().confirmar("Excluir o produto “" + produto.getNome() + "”?")
        ) principal.executar(() -> {
            produtoDAO.excluir(produto);
            atualizar();
        });
    }

    @FXML
    private void vender() {
        principal.executar(() -> principal.vender(tabelaProdutos.getSelectionModel().getSelectedItem()));
    }
}
