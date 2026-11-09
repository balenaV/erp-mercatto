package br.com.mercatto.controller;

import br.com.mercatto.model.*;
import br.com.mercatto.model.dao.DadosAplicacao;
import br.com.mercatto.util.Dialogos;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.*;

/** Abre as telas com FXMLLoader e entrega os mesmos dados a cada controller. */
public final class PrincipalController {

    @FXML
    private BorderPane areaConteudo;

    @FXML
    private Parent conteudoPdv;

    @FXML
    private PdvController conteudoPdvController;

    @FXML
    private Button btnMenuPdv, btnMenuProdutos, btnMenuClientes, btnMenuVendas;

    private DadosAplicacao dados;
    private Dialogos dialogos;
    private Parent telaProdutos, telaClientes, telaVendas;
    private ProdutosConsultaController produtosController;
    private ClientesConsultaController clientesController;
    private VendasConsultaController vendasController;

    public void iniciar(DadosAplicacao dados, Dialogos dialogos) {
        this.dados = dados;
        this.dialogos = dialogos;
        conteudoPdvController.setPrincipal(this);
        abrirPdv();
    }

    public DadosAplicacao getDados() {
        return dados;
    }

    public Dialogos getDialogos() {
        return dialogos;
    }

    @FXML
    public void abrirPdv() {
        executar(() -> {
            conteudoPdvController.atualizar();
            mostrar(conteudoPdv, btnMenuPdv);
        });
    }

    @FXML
    public void abrirProdutos() {
        executar(() -> {
            if (telaProdutos == null) {
                FXMLLoader loader = loader("produtos-consulta");
                telaProdutos = carregar(loader);
                produtosController = loader.getController();
                produtosController.setPrincipal(this);
            }
            produtosController.atualizar();
            mostrar(telaProdutos, btnMenuProdutos);
        });
    }

    @FXML
    public void abrirClientes() {
        executar(() -> {
            if (telaClientes == null) {
                FXMLLoader loader = loader("clientes-consulta");
                telaClientes = carregar(loader);
                clientesController = loader.getController();
                clientesController.setPrincipal(this);
            }
            clientesController.atualizar();
            mostrar(telaClientes, btnMenuClientes);
        });
    }

    @FXML
    public void abrirVendas() {
        executar(() -> {
            if (telaVendas == null) {
                FXMLLoader loader = loader("vendas-consulta");
                telaVendas = carregar(loader);
                vendasController = loader.getController();
                vendasController.setPrincipal(this);
            }
            vendasController.atualizar();
            mostrar(telaVendas, btnMenuVendas);
        });
    }

    private void mostrar(Parent tela, Button ativo) {
        areaConteudo.setCenter(tela);
        for (Button botao : new Button[] { btnMenuPdv, btnMenuProdutos, btnMenuClientes, btnMenuVendas }) {
            botao.getStyleClass().remove("nav-item-active");
        }
        ativo.getStyleClass().add("nav-item-active");
    }

    public void vender(Produto produto) {
        abrirPdv();
        conteudoPdvController.adicionarProduto(produto, 1);
    }

    public void usarCliente(Cliente cliente) {
        abrirPdv();
        conteudoPdvController.selecionarCliente(cliente);
    }

    public void produto(Produto produto, Consumer<Produto> aoSalvar) {
        produto(produto, "", aoSalvar);
    }

    public void produto(Produto produto, String descricaoInicial, Consumer<Produto> aoSalvar) {
        FXMLLoader loader = loader("produto-formulario");
        Parent tela = carregar(loader);
        ProdutoFormularioController controller = loader.getController();
        controller.setPrincipal(this);
        controller.configurar(produto, descricaoInicial, aoSalvar);
        abrirModal(tela, "Produto");
    }

    public void cliente(Cliente cliente, Consumer<Cliente> aoSalvar) {
        FXMLLoader loader = loader("cliente-formulario");
        Parent tela = carregar(loader);
        ClienteFormularioController controller = loader.getController();
        controller.setPrincipal(this);
        controller.configurar(cliente, aoSalvar);
        abrirModal(tela, "Cliente");
    }

    public void pagamento(Runnable aoConcluir) {
        FXMLLoader loader = loader("pagamento");
        Parent tela = carregar(loader);
        PagamentoController controller = loader.getController();
        controller.setPrincipal(this);
        controller.configurar(aoConcluir);
        abrirModal(tela, "Pagamento");
    }

    public void detalhes(Venda venda) {
        FXMLLoader loader = loader("venda-detalhes");
        Parent tela = carregar(loader);
        VendaDetalhesController controller = loader.getController();
        controller.setPrincipal(this);
        controller.configurar(venda);
        abrirModal(tela, "Venda #" + venda.getNumero());
    }

    private FXMLLoader loader(String nome) {
        return new FXMLLoader(getClass().getResource("/br/com/mercatto/view/" + nome + ".fxml"));
    }

    private Parent carregar(FXMLLoader loader) {
        try {
            return loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível carregar o FXML.", e);
        }
    }

    private void abrirModal(Parent tela, String titulo) {
        Window proprietario = areaConteudo.getScene().getWindow();
        Stage janela = new Stage();
        janela.initOwner(proprietario);
        janela.initModality(Modality.WINDOW_MODAL);
        janela.setTitle("Mercatto — " + titulo);
        Scene cena = new Scene(tela);
        janela.setScene(cena);
        cena.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) janela.close();
        });
        var limites = Screen.getScreensForRectangle(
            proprietario.getX(),
            proprietario.getY(),
            proprietario.getWidth(),
            proprietario.getHeight()
        )
            .getFirst()
            .getVisualBounds();
        janela.setWidth(Math.min(tela.prefWidth(-1) + 16, limites.getWidth() - 40));
        janela.setHeight(Math.min(tela.prefHeight(-1) + 40, limites.getHeight() - 30));
        janela.setMinWidth(700);
        janela.setMinHeight(600);
        janela.show();
        janela.setX(proprietario.getX() + (proprietario.getWidth() - janela.getWidth()) / 2);
        janela.setY(
            Math.max(limites.getMinY(), proprietario.getY() + (proprietario.getHeight() - janela.getHeight()) / 2)
        );
    }

    public void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RegraNegocioException e) {
            dialogos.erro(e.getMessage());
        } catch (RuntimeException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Falha na operação", e);
            dialogos.erro("Não foi possível concluir a operação. Consulte o console para detalhes.");
        }
    }
}
