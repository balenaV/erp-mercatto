package br.com.mercatto.controller;

import br.com.mercatto.model.*;
import br.com.mercatto.util.*;
import java.math.BigDecimal;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public final class VendaDetalhesController {

    private PrincipalController principal;

    @FXML
    private Label labelNumeroVenda, labelDataVenda, labelFormaPagamento, labelClienteVenda, labelDocumentoCliente;

    @FXML
    private Label labelValorRecebido, labelTrocoVenda, labelTotalVenda;

    @FXML
    private TableView<ItemVenda> tabelaItensVenda;

    @FXML
    private TableColumn<ItemVenda, String> colunaCodigo, colunaProduto;

    @FXML
    private TableColumn<ItemVenda, Integer> colunaQuantidade;

    @FXML
    private TableColumn<ItemVenda, BigDecimal> colunaPreco, colunaSubtotal;

    public void setPrincipal(PrincipalController principal) {
        this.principal = principal;
    }

    @FXML
    private void initialize() {
        Tabelas.coluna(colunaCodigo, ItemVenda::getCodigo);
        Tabelas.coluna(colunaProduto, ItemVenda::getNome);
        Tabelas.coluna(colunaQuantidade, ItemVenda::getQuantidade);
        Tabelas.coluna(colunaPreco, ItemVenda::getPrecoUnitario, Formatos::moeda);
        Tabelas.coluna(colunaSubtotal, ItemVenda::getSubtotal, Formatos::moeda);
    }

    public void configurar(Venda venda) {
        labelNumeroVenda.setText("#" + venda.getNumero());
        labelDataVenda.setText(Formatos.DATA_HORA.format(venda.getData()));
        labelFormaPagamento.setText(venda.getFormaPagamento().toString());
        labelClienteVenda.setText(venda.getClienteNome());
        labelDocumentoCliente.setText(venda.getClienteDocumento());
        tabelaItensVenda.getItems().setAll(venda.getItens());
        labelValorRecebido.setText(Formatos.moeda(venda.getValorRecebido()));
        labelTrocoVenda.setText(Formatos.moeda(venda.getTroco()));
        labelTotalVenda.setText(Formatos.moeda(venda.getTotal()));
    }

    @FXML
    private void fechar() {
        ((Stage) labelNumeroVenda.getScene().getWindow()).close();
    }
}
