package br.com.mercatto.controller;

import br.com.mercatto.model.Cliente;
import br.com.mercatto.model.dao.*;
import br.com.mercatto.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public final class ClientesConsultaController {

    private PrincipalController principal;
    private ClienteDAO clienteDAO;

    @FXML
    private TextField campoBuscaCliente;

    @FXML
    private ComboBox<String> filtroSituacaoCliente;

    @FXML
    private TableView<Cliente> tabelaClientes;

    @FXML
    private TableColumn<Cliente, String> colunaNome, colunaDocumento, colunaTelefone, colunaSituacao;

    @FXML
    private Button btnEditarCliente, btnSituacaoCliente, btnExcluirCliente, btnUsarCliente;

    public void setPrincipal(PrincipalController principal) {
        this.principal = principal;
        this.clienteDAO = principal.getDados().getClienteDAO();
        atualizar();
    }

    @FXML
    private void initialize() {
        filtroSituacaoCliente.getItems().setAll("Todas as situações", "Ativos", "Inativos");
        filtroSituacaoCliente.setValue("Todas as situações");
        Tabelas.coluna(colunaNome, Cliente::getNome);
        Tabelas.coluna(colunaDocumento, Cliente::getDocumento);
        Tabelas.coluna(colunaTelefone, Cliente::getTelefone);
        Tabelas.coluna(colunaSituacao, c -> c.isAtivo() ? "Ativo" : "Inativo");
        var semSelecao = tabelaClientes.getSelectionModel().selectedItemProperty().isNull();
        btnEditarCliente.disableProperty().bind(semSelecao);
        btnSituacaoCliente.disableProperty().bind(semSelecao);
        btnExcluirCliente.disableProperty().bind(semSelecao);
        tabelaClientes
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((o, a, b) -> btnUsarCliente.setDisable(b == null || !b.isAtivo()));
        Tabelas.duploClique(tabelaClientes, this::editar);
        campoBuscaCliente.textProperty().addListener((o, a, b) -> atualizar());
        filtroSituacaoCliente.valueProperty().addListener((o, a, b) -> atualizar());
        atualizar();
    }

    public void atualizar() {
        if (clienteDAO == null) return;
        Cliente selecionado = tabelaClientes.getSelectionModel().getSelectedItem();
        Boolean ativo = null;
        if ("Ativos".equals(filtroSituacaoCliente.getValue())) ativo = true;
        else if ("Inativos".equals(filtroSituacaoCliente.getValue())) ativo = false;
        tabelaClientes.getItems().setAll(clienteDAO.consultar(campoBuscaCliente.getText(), ativo));
        if (tabelaClientes.getItems().contains(selecionado)) tabelaClientes.getSelectionModel().select(selecionado);
        else tabelaClientes.getSelectionModel().clearSelection();
        tabelaClientes.refresh();
        btnUsarCliente.setDisable(
            selecionado == null || !tabelaClientes.getItems().contains(selecionado) || !selecionado.isAtivo()
        );
    }

    @FXML
    private void buscar() {
        atualizar();
    }

    @FXML
    private void novo() {
        principal.executar(() -> principal.cliente(null, this::selecionarSalvo));
    }

    @FXML
    private void editar() {
        Cliente selecionado = tabelaClientes.getSelectionModel().getSelectedItem();
        if (selecionado != null) principal.executar(() -> principal.cliente(selecionado, this::selecionarSalvo));
    }

    private void selecionarSalvo(Cliente cliente) {
        campoBuscaCliente.clear();
        filtroSituacaoCliente.setValue("Todas as situações");
        atualizar();
        tabelaClientes.getSelectionModel().select(cliente);
        tabelaClientes.scrollTo(cliente);
    }

    @FXML
    private void alterarSituacao() {
        principal.executar(() -> {
            clienteDAO.alterarSituacao(tabelaClientes.getSelectionModel().getSelectedItem());
            atualizar();
        });
    }

    @FXML
    private void excluir() {
        Cliente cliente = tabelaClientes.getSelectionModel().getSelectedItem();
        if (
            cliente != null && principal.getDialogos().confirmar("Excluir o cliente “" + cliente.getNome() + "”?")
        ) principal.executar(() -> {
            clienteDAO.excluir(cliente);
            atualizar();
        });
    }

    @FXML
    private void usarCliente() {
        principal.executar(() -> principal.usarCliente(tabelaClientes.getSelectionModel().getSelectedItem()));
    }
}
