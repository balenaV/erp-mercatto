package br.com.mercatto.controller;

import br.com.mercatto.model.*;
import br.com.mercatto.model.dao.*;
import br.com.mercatto.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public final class VendasConsultaController {

    private PrincipalController principal;
    private VendaDAO vendaDAO;
    private ClienteDAO clienteDAO;

    @FXML
    private DatePicker filtroDataInicial, filtroDataFinal;

    @FXML
    private ComboBox<Cliente> filtroCliente;

    @FXML
    private TableView<Venda> tabelaVendas;

    @FXML
    private TableColumn<Venda, Long> colunaNumero;

    @FXML
    private TableColumn<Venda, LocalDateTime> colunaData;

    @FXML
    private TableColumn<Venda, String> colunaCliente, colunaPagamento;

    @FXML
    private TableColumn<Venda, BigDecimal> colunaTotal;

    @FXML
    private Button btnDetalhesVenda;

    public void setPrincipal(PrincipalController principal) {
        this.principal = principal;
        this.vendaDAO = principal.getDados().getVendaDAO();
        this.clienteDAO = principal.getDados().getClienteDAO();
        atualizar();
    }

    @FXML
    private void initialize() {
        Tabelas.coluna(colunaNumero, Venda::getNumero, n -> "#" + n);
        Tabelas.coluna(colunaData, Venda::getData, Formatos.DATA_HORA::format);
        Tabelas.coluna(colunaCliente, Venda::getClienteNome);
        Tabelas.coluna(colunaPagamento, v -> v.getFormaPagamento().toString());
        Tabelas.coluna(colunaTotal, Venda::getTotal, Formatos::moeda);
        Tabelas.combo(filtroCliente, "Todos os clientes", Cliente::toString);
        btnDetalhesVenda.disableProperty().bind(tabelaVendas.getSelectionModel().selectedItemProperty().isNull());
        Tabelas.duploClique(tabelaVendas, this::detalhes);
        atualizar();
    }

    public void atualizar() {
        if (vendaDAO == null) return;
        Cliente selecionado = filtroCliente.getValue();
        var clientes = new ArrayList<Cliente>();
        clientes.add(null);
        clientes.addAll(clienteDAO.listarTodos());
        filtroCliente.getItems().setAll(clientes);
        filtroCliente.setValue(clientes.contains(selecionado) ? selecionado : null);
        filtrar();
    }

    @FXML
    private void filtrar() {
        principal.executar(() ->
            tabelaVendas
                .getItems()
                .setAll(
                    vendaDAO.consultar(
                        filtroDataInicial.getValue(),
                        filtroDataFinal.getValue(),
                        filtroCliente.getValue()
                    )
                )
        );
    }

    @FXML
    private void limparFiltros() {
        filtroDataInicial.setValue(null);
        filtroDataFinal.setValue(null);
        filtroCliente.setValue(null);
        filtrar();
    }

    @FXML
    private void detalhes() {
        Venda venda = tabelaVendas.getSelectionModel().getSelectedItem();
        if (venda != null) principal.executar(() -> principal.detalhes(venda));
    }
}
