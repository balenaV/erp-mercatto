package br.com.mercatto.controller;

import br.com.mercatto.model.Produto;
import br.com.mercatto.model.dao.*;
import br.com.mercatto.util.Formatos;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public final class ProdutoFormularioController {

    private PrincipalController principal;
    private ProdutoDAO produtoDAO;

    @FXML
    private Label tituloFormulario;

    @FXML
    private TextField campoNomeProduto, campoCodigoProduto, campoPrecoProduto, campoEstoqueProduto;

    @FXML
    private RadioButton radioAtivo, radioInativo;

    private Produto produto;
    private Consumer<Produto> aoSalvar = p -> {};

    public void setPrincipal(PrincipalController principal) {
        this.principal = principal;
        this.produtoDAO = principal.getDados().getProdutoDAO();
    }

    public void configurar(Produto produto, Consumer<Produto> aoSalvar) {
        configurar(produto, "", aoSalvar);
    }

    public void configurar(Produto produto, String descricaoInicial, Consumer<Produto> aoSalvar) {
        this.produto = produto;
        this.aoSalvar = aoSalvar;
        tituloFormulario.setText(produto == null ? "Novo produto" : "Editar produto");
        if (produto == null) {
            campoNomeProduto.setText(descricaoInicial == null ? "" : descricaoInicial);
        } else {
            campoNomeProduto.setText(produto.getNome());
            campoCodigoProduto.setText(produto.getCodigo());
            campoPrecoProduto.setText(Formatos.decimal(produto.getPrecoVenda()));
            campoEstoqueProduto.setText(Integer.toString(produto.getEstoque()));
            (produto.isAtivo() ? radioAtivo : radioInativo).setSelected(true);
        }
    }

    @FXML
    private void salvar() {
        principal.executar(() -> {
            Produto salvo = produtoDAO.salvar(
                produto,
                campoCodigoProduto.getText(),
                campoNomeProduto.getText(),
                Formatos.lerMoeda(campoPrecoProduto.getText()),
                campoEstoqueProduto.getText().isBlank()
                    ? 0
                    : Formatos.inteiro(campoEstoqueProduto.getText(), "Estoque"),
                radioAtivo.isSelected()
            );
            aoSalvar.accept(salvo);
            fechar();
        });
    }

    @FXML
    private void fechar() {
        ((Stage) campoNomeProduto.getScene().getWindow()).close();
    }
}
