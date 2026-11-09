package br.com.mercatto.controller;

import br.com.mercatto.model.Cliente;
import br.com.mercatto.model.dao.*;

import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public final class ClienteFormularioController {

    private PrincipalController principal;
    private ClienteDAO clienteDAO;

    @FXML
    private Label tituloFormulario;

    @FXML
    private TextField campoNomeCliente, campoDocumentoCliente, campoTelefoneCliente;

    @FXML
    private RadioButton radioAtivo, radioInativo;

    private Cliente cliente;
    private Consumer<Cliente> aoSalvar = c -> {
    };

    public void setPrincipal(PrincipalController principal) {
        this.principal = principal;
        this.clienteDAO = principal.getDados().getClienteDAO();
    }

    public void configurar(Cliente cliente, Consumer<Cliente> aoSalvar) {
        this.cliente = cliente;
        this.aoSalvar = aoSalvar;
        tituloFormulario.setText(cliente == null ? "Novo cliente" : "Editar cliente");
        if (cliente != null) {
            campoNomeCliente.setText(cliente.getNome());
            campoDocumentoCliente.setText(cliente.getDocumento());
            campoTelefoneCliente.setText(cliente.getTelefone());
            (cliente.isAtivo() ? radioAtivo : radioInativo).setSelected(true);
        }
    }

    @FXML
    private void salvar() {
        principal.executar(() -> {
            Cliente salvo = clienteDAO.salvar(
                cliente,
                campoNomeCliente.getText(),
                campoDocumentoCliente.getText(),
                campoTelefoneCliente.getText(),
                radioAtivo.isSelected()
            );
            aoSalvar.accept(salvo);
            fechar();
        });
    }

    @FXML
    private void fechar() {
        ((Stage) campoNomeCliente.getScene().getWindow()).close();
    }
}
