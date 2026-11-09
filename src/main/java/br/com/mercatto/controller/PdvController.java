package br.com.mercatto.controller;

import br.com.mercatto.model.*;
import br.com.mercatto.model.dao.*;
import br.com.mercatto.util.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public final class PdvController {

    private PrincipalController principal;
    private ProdutoDAO produtoDAO;
    private ClienteDAO clienteDAO;
    private Carrinho carrinho;

    @FXML
    private TextField campoBuscaProduto, campoQuantidade;

    @FXML
    private TableView<ItemCarrinho> tabelaCarrinho;

    @FXML
    private TableColumn<ItemCarrinho, String> colunaProduto;

    @FXML
    private TableColumn<ItemCarrinho, Integer> colunaQuantidade;

    @FXML
    private TableColumn<ItemCarrinho, BigDecimal> colunaPrecoUnitario, colunaSubtotal;

    @FXML
    private ComboBox<Cliente> seletorCliente;

    @FXML
    private Label labelQuantidadeItens, labelSubtotal, labelTotalVenda;

    @FXML
    private Button btnRemoverItem, btnAlterarQuantidade, btnLimparCarrinho, btnPagamento, btnCancelarVenda;

    @FXML
    private HBox avisoBuscaProduto;

    @FXML
    private Label labelResultadoBusca;

    @FXML
    private Button btnCriarProduto;

    private final ContextMenu sugestoes = new ContextMenu();
    private Produto produtoSelecionado;
    private boolean atualizando;

    public void setPrincipal(PrincipalController principal) {
        this.principal = principal;
        this.produtoDAO = principal.getDados().getProdutoDAO();
        this.clienteDAO = principal.getDados().getClienteDAO();
        this.carrinho = principal.getDados().getCarrinho();
        atualizar();
    }

    @FXML
    private void initialize() {
        Tabelas.coluna(colunaProduto, i -> i.getProduto().getNome());
        Tabelas.coluna(colunaQuantidade, ItemCarrinho::getQuantidade);
        Tabelas.coluna(colunaPrecoUnitario, ItemCarrinho::getPrecoUnitario, Formatos::moeda);
        Tabelas.coluna(colunaSubtotal, ItemCarrinho::getSubtotal, Formatos::moeda);
        var semSelecao = tabelaCarrinho.getSelectionModel().selectedItemProperty().isNull();
        btnRemoverItem.disableProperty().bind(semSelecao);
        btnAlterarQuantidade.disableProperty().bind(semSelecao);
        Tabelas.duploClique(tabelaCarrinho, this::alterarQuantidade);
        Tabelas.combo(seletorCliente, "Consumidor não identificado", Cliente::toString);
        seletorCliente.valueProperty().addListener((o, a, b) -> {
            if (!atualizando) principal.executar(() -> carrinho.setCliente(b));
        });
        campoQuantidade.setText("1");
        campoBuscaProduto.textProperty().addListener((o, a, b) -> {
            produtoSelecionado = null;
            mostrarSugestoes();
        });
        campoBuscaProduto.focusedProperty().addListener((o, a, focado) -> {
            if (focado) mostrarSugestoes();
            else sugestoes.hide();
        });
        atualizar();
    }

    public void atualizar() {
        if (carrinho == null) return;
        ItemCarrinho selecionado = tabelaCarrinho.getSelectionModel().getSelectedItem();
        tabelaCarrinho.getItems().setAll(carrinho.getItens());
        tabelaCarrinho.getSelectionModel().select(selecionado);
        tabelaCarrinho.refresh();
        atualizando = true;
        try {
            List<Cliente> clientes = new ArrayList<>();
            clientes.add(null);
            clientes.addAll(clienteDAO.consultar("", true));
            if (carrinho.getCliente() != null && !clientes.contains(carrinho.getCliente())) clientes.add(
                carrinho.getCliente()
            );
            seletorCliente.getItems().setAll(clientes);
            seletorCliente.setValue(carrinho.getCliente());
        } finally {
            atualizando = false;
        }
        labelQuantidadeItens.setText(Long.toString(carrinho.getQuantidadeTotal()));
        labelSubtotal.setText(Formatos.moeda(carrinho.getTotal()));
        labelTotalVenda.setText(Formatos.moeda(carrinho.getTotal()));
        btnPagamento.setDisable(carrinho.isVazio());
        btnLimparCarrinho.setDisable(carrinho.isVazio());
        btnCancelarVenda.setDisable(carrinho.isVazio() && carrinho.getCliente() == null);
        mostrarSugestoes();
    }

    private void mostrarSugestoes() {
        sugestoes.getItems().clear();
        String termo = campoBuscaProduto.getText().strip();
        boolean semResultados = false;
        boolean podeCriar = false;
        List<Produto> encontrados = List.of();
        if (!termo.isEmpty() && produtoSelecionado == null) {
            encontrados = produtoDAO.consultar(termo, true);
            semResultados = encontrados.isEmpty();
            podeCriar = semResultados && produtoDAO.consultar(termo, null).isEmpty();
        }
        avisoBuscaProduto.setVisible(semResultados);
        avisoBuscaProduto.setManaged(semResultados);
        btnCriarProduto.setVisible(podeCriar);
        btnCriarProduto.setManaged(podeCriar);
        labelResultadoBusca.setText(
            podeCriar ? "Produto não encontrado." : "Há produtos inativos com essa busca. Ative-os em Produtos."
        );
        if (
            termo.isEmpty() ||
            produtoSelecionado != null ||
            !campoBuscaProduto.isFocused() ||
            campoBuscaProduto.getScene() == null
        ) {
            sugestoes.hide();
            return;
        }
        encontrados
            .stream()
            .limit(8)
            .forEach(p -> {
                MenuItem opcao = new MenuItem(
                    p + " · " + Formatos.moeda(p.getPrecoVenda()) + " · Estoque: " + p.getEstoque()
                );
                opcao.setOnAction(e -> {
                    campoBuscaProduto.setText(p.toString());
                    produtoSelecionado = p;
                    mostrarSugestoes();
                    campoQuantidade.requestFocus();
                    campoQuantidade.selectAll();
                });
                sugestoes.getItems().add(opcao);
            });
        if (sugestoes.getItems().isEmpty()) sugestoes.hide();
        else sugestoes.show(campoBuscaProduto, Side.BOTTOM, 0, 0);
    }

    @FXML
    private void adicionar() {
        principal.executar(() -> {
            Produto produto = produtoSelecionado;
            if (produto == null) {
                String busca = campoBuscaProduto.getText().strip();
                Validacao.exigir(!busca.isEmpty(), "Informe o nome ou código de um produto cadastrado.");
                List<Produto> encontrados = produtoDAO.consultar(busca, true);
                if (encontrados.isEmpty()) {
                    mostrarSugestoes();
                    return;
                }
                produto = encontrados
                    .stream()
                    .filter(p -> p.getCodigo().equalsIgnoreCase(busca))
                    .findFirst()
                    .orElse(null);
                if (produto == null && encontrados.size() == 1) produto = encontrados.getFirst();
                Validacao.exigir(produto != null, "Escolha um dos produtos sugeridos pela busca.");
            }
            adicionarProduto(produto, Formatos.inteiro(campoQuantidade.getText(), "Quantidade"));
            campoBuscaProduto.clear();
            produtoSelecionado = null;
            sugestoes.hide();
            campoQuantidade.setText("1");
            campoBuscaProduto.requestFocus();
        });
    }

    @FXML
    private void criarProduto() {
        principal.executar(() -> {
            String descricao = campoBuscaProduto.getText().strip();
            if (descricao.isEmpty() || !produtoDAO.consultar(descricao, null).isEmpty()) {
                mostrarSugestoes();
                return;
            }
            sugestoes.hide();
            principal.produto(null, descricao, produto -> {
                campoBuscaProduto.setText(produto.toString());
                produtoSelecionado = produto;
                atualizar();
                campoQuantidade.requestFocus();
                campoQuantidade.selectAll();
            });
        });
    }

    public void adicionarProduto(Produto produto, int quantidade) {
        produtoDAO.exigirCadastrado(produto);
        carrinho.adicionar(produto, quantidade);
        atualizar();
    }

    public void selecionarCliente(Cliente cliente) {
        clienteDAO.exigirCadastrado(cliente);
        carrinho.setCliente(cliente);
        atualizar();
    }

    @FXML
    private void alterarQuantidade() {
        ItemCarrinho item = tabelaCarrinho.getSelectionModel().getSelectedItem();
        if (item != null) principal
            .getDialogos()
            .solicitarQuantidade(item.getQuantidade())
            .ifPresent(texto ->
                principal.executar(() -> {
                    carrinho.alterarQuantidade(item, Formatos.inteiro(texto, "Quantidade"));
                    atualizar();
                })
            );
    }

    @FXML
    private void remover() {
        carrinho.remover(tabelaCarrinho.getSelectionModel().getSelectedItem());
        atualizar();
    }

    @FXML
    private void limparCarrinho() {
        if (principal.getDialogos().confirmar("Remover todos os itens do carrinho?")) {
            carrinho.limparItens();
            atualizar();
        }
    }

    @FXML
    private void cancelarVenda() {
        if (principal.getDialogos().confirmar("Cancelar a venda em preparação?")) {
            carrinho.limpar();
            campoBuscaProduto.clear();
            campoQuantidade.setText("1");
            atualizar();
        }
    }

    @FXML
    private void pagamento() {
        principal.executar(() -> principal.pagamento(this::atualizar));
    }
}
