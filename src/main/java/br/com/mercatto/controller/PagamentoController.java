package br.com.mercatto.controller;

import br.com.mercatto.model.*;
import br.com.mercatto.model.dao.*;
import br.com.mercatto.util.Formatos;
import java.math.BigDecimal;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public final class PagamentoController {

    private PrincipalController principal;
    private Carrinho carrinho;
    private VendaDAO vendaDAO;

    @FXML
    private ToggleButton opcaoDinheiro, opcaoCartao, opcaoPix;

    @FXML
    private ToggleGroup grupoPagamento;

    @FXML
    private TextField campoValorRecebido;

    @FXML
    private Label labelTroco, labelTotalPagamento, labelClientePagamento, labelItensPagamento, labelOrientacaoPagamento;

    @FXML
    private Button btnConfirmarPagamento;

    private Runnable aoConcluir = () -> {};
    private boolean concluindo;

    public void setPrincipal(PrincipalController principal) {
        this.principal = principal;
        this.carrinho = principal.getDados().getCarrinho();
        this.vendaDAO = principal.getDados().getVendaDAO();
        atualizar();
    }

    @FXML
    private void initialize() {
        grupoPagamento.selectedToggleProperty().addListener((o, anterior, atual) -> {
            if (atual == null && anterior != null) anterior.setSelected(true);
            atualizar();
        });
        campoValorRecebido.textProperty().addListener((o, a, b) -> validarEntrada());
    }

    public void configurar(Runnable aoConcluir) {
        this.aoConcluir = aoConcluir;
        atualizar();
    }

    public void atualizar() {
        if (carrinho == null) return;
        labelTotalPagamento.setText(Formatos.moeda(carrinho.getTotal()));
        labelClientePagamento.setText(
            carrinho.getCliente() == null ? "Consumidor não identificado" : carrinho.getCliente().getNome()
        );
        labelItensPagamento.setText(Long.toString(carrinho.getQuantidadeTotal()));
        boolean dinheiro = opcaoDinheiro.isSelected();
        campoValorRecebido.setDisable(!dinheiro);
        if (!dinheiro) campoValorRecebido.setText(Formatos.decimal(carrinho.getTotal()));
        else campoValorRecebido.clear();
        labelOrientacaoPagamento.setText(
            dinheiro
                ? "Informe o valor recebido. Ele deve cobrir o total da venda."
                : "Confirme após receber o pagamento. Esta etapa apenas registra a forma escolhida."
        );
        validarEntrada();
    }

    private Pagamento criarPagamento() {
        BigDecimal total = carrinho.getTotal();
        if (opcaoCartao.isSelected()) return new PagamentoCartao(total);
        if (opcaoPix.isSelected()) return new PagamentoPix(total);
        return new PagamentoDinheiro(Formatos.lerMoeda(campoValorRecebido.getText()));
    }

    private void validarEntrada() {
        try {
            Pagamento pagamento = criarPagamento();
            pagamento.validar(carrinho.getTotal());
            labelTroco.setText(Formatos.moeda(pagamento.calcularTroco(carrinho.getTotal())));
            btnConfirmarPagamento.setDisable(concluindo || carrinho.isVazio());
        } catch (RegraNegocioException e) {
            labelTroco.setText(Formatos.moeda(BigDecimal.ZERO));
            btnConfirmarPagamento.setDisable(true);
        }
    }

    @FXML
    private void confirmar() {
        if (concluindo) return;
        principal.executar(() -> {
            concluindo = true;
            btnConfirmarPagamento.setDisable(true);
            try {
                Venda venda = vendaDAO.finalizar(criarPagamento());
                aoConcluir.run();
                fechar();
                principal
                    .getDialogos()
                    .informar(
                        "Venda #" + venda.getNumero() + " concluída. Total: " + Formatos.moeda(venda.getTotal()) + "."
                    );
            } finally {
                concluindo = false;
                validarEntrada();
            }
        });
    }

    @FXML
    private void fechar() {
        ((Stage) campoValorRecebido.getScene().getWindow()).close();
    }
}
