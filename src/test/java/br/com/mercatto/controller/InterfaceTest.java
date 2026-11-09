package br.com.mercatto.controller;

import static org.junit.jupiter.api.Assertions.*;

import br.com.mercatto.model.*;
import br.com.mercatto.model.dao.DadosAplicacao;
import br.com.mercatto.util.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.*;

/** Exercita FXML, injeção, eventos e passagem de objetos usando o toolkit JavaFX real. */
class InterfaceTest {

    private static class DialogosTeste implements Dialogos {

        final List<String> erros = new ArrayList<>();
        final List<String> avisos = new ArrayList<>();

        @Override
        public void erro(String mensagem) {
            erros.add(mensagem);
        }

        @Override
        public void informar(String mensagem) {
            avisos.add(mensagem);
        }

        @Override
        public boolean confirmar(String mensagem) {
            return true;
        }

        @Override
        public Optional<String> solicitarQuantidade(int atual) {
            return Optional.of("3");
        }
    }

    @BeforeAll
    static void iniciarJavaFx() {
        Platform.startup(() -> {});
        Platform.setImplicitExit(false);
    }

    @AfterAll
    static void encerrarJavaFx() throws Exception {
        noJavaFx(() -> new ArrayList<>(Window.getWindows()).forEach(Window::hide));
        Platform.exit();
    }

    private static void noJavaFx(Runnable tarefa) throws Exception {
        FutureTask<Void> futuro = new FutureTask<>(tarefa, null);
        Platform.runLater(futuro);
        futuro.get(40, TimeUnit.SECONDS);
    }

    private static <T> T controle(Parent raiz, String id, Class<T> tipo) {
        Object controle = raiz.lookup("#" + id);
        assertNotNull(controle, id);
        return tipo.cast(controle);
    }

    private static void texto(Parent raiz, String id, String valor) {
        controle(raiz, id, TextField.class).setText(valor);
    }

    private static void clicar(Parent raiz, String id) {
        Button botao = controle(raiz, id, Button.class);
        assertFalse(botao.isDisabled(), id);
        botao.fire();
    }

    private static Parent abrirPrincipal(DadosAplicacao dados, Dialogos dialogos) {
        FXMLLoader loader = new FXMLLoader(InterfaceTest.class.getResource("/br/com/mercatto/view/principal.fxml"));
        try {
            Parent raiz = loader.load();
            PrincipalController controller = loader.getController();
            controller.iniciar(dados, dialogos);
            return raiz;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static Parent modal(String campo) {
        return Window.getWindows()
            .stream()
            .filter(Window::isShowing)
            .map(Window::getScene)
            .filter(s -> s != null)
            .map(Scene::getRoot)
            .filter(r -> r.lookup("#" + campo) != null)
            .findFirst()
            .orElseThrow();
    }

    @Test
    void buscaSemResultadoOfereceMesmoModalECadastroExigeSoDescricaoEValor() throws Exception {
        noJavaFx(() -> {
            DialogosTeste dialogos = new DialogosTeste();
            DadosAplicacao dados = new DadosAplicacao();
            Produto anterior = dados
                .getProdutoDAO()
                .salvar(null, "EXISTENTE", "Produto já no carrinho", BigDecimal.TEN, 5, true);
            dados.getCarrinho().adicionar(anterior, 1);
            Parent raiz = abrirPrincipal(dados, dialogos);
            Stage janela = new Stage();
            janela.setScene(new Scene(raiz, 1280, 700));
            janela.show();
            raiz.applyCss();
            raiz.layout();
            try {
                texto(raiz, "campoBuscaProduto", "Descrição pesquisada");
                texto(raiz, "campoQuantidade", "2");
                assertTrue(controle(raiz, "avisoBuscaProduto", HBox.class).isVisible());
                assertTrue(controle(raiz, "btnCriarProduto", Button.class).isVisible());
                clicar(raiz, "btnAdicionarItem");
                assertTrue(dialogos.erros.isEmpty());
                assertEquals(1, dados.getProdutoDAO().listarTodos().size());
                clicar(raiz, "btnCriarProduto");
                Parent formulario = modal("campoNomeProduto");
                formulario.applyCss();
                formulario.layout();
                assertEquals("produtoFormularioRoot", formulario.getId());
                assertEquals(
                    "Descrição pesquisada",
                    controle(formulario, "campoNomeProduto", TextField.class).getText()
                );
                clicar(formulario, "btnCancelar");
                assertEquals(1, dados.getProdutoDAO().listarTodos().size());
                assertEquals("Descrição pesquisada", controle(raiz, "campoBuscaProduto", TextField.class).getText());
                assertEquals("2", controle(raiz, "campoQuantidade", TextField.class).getText());
                assertSame(anterior, dados.getCarrinho().getItens().getFirst().getProduto());

                clicar(raiz, "btnCriarProduto");
                formulario = modal("campoNomeProduto");
                assertTrue(controle(formulario, "campoCodigoProduto", TextField.class).getText().isEmpty());
                assertTrue(controle(formulario, "campoEstoqueProduto", TextField.class).getText().isEmpty());
                clicar(formulario, "btnSalvar");
                assertEquals(1, dialogos.erros.size());
                texto(formulario, "campoNomeProduto", " ");
                texto(formulario, "campoPrecoProduto", "8,90");
                clicar(formulario, "btnSalvar");
                assertEquals(2, dialogos.erros.size());
                assertEquals(1, dados.getProdutoDAO().listarTodos().size());
                texto(formulario, "campoNomeProduto", "Descrição pesquisada");
                clicar(formulario, "btnSalvar");
                Produto criado = dados.getProdutoDAO().listarTodos().getLast();
                assertEquals("Descrição pesquisada", criado.getNome());
                assertEquals(new BigDecimal("8.90"), criado.getPrecoVenda());
                assertFalse(criado.getCodigo().isBlank());
                assertEquals(0, criado.getEstoque());
                assertTrue(criado.isAtivo());
                assertEquals(criado.toString(), controle(raiz, "campoBuscaProduto", TextField.class).getText());
                assertFalse(controle(raiz, "avisoBuscaProduto", HBox.class).isVisible());
                assertEquals(1, dados.getCarrinho().getQuantidadeTotal());
                clicar(raiz, "btnAdicionarItem");
                assertEquals(3, dialogos.erros.size());
                assertTrue(dialogos.erros.getLast().contains("Estoque insuficiente"));

                clicar(raiz, "btnMenuProdutos");
                clicar(raiz, "btnNovoProduto");
                Parent gerenciador = modal("campoNomeProduto");
                assertEquals(formulario.getId(), gerenciador.getId());
                texto(gerenciador, "campoNomeProduto", "Criado pelo gerenciador");
                texto(gerenciador, "campoPrecoProduto", "15,20");
                clicar(gerenciador, "btnSalvar");
                Produto outro = dados.getProdutoDAO().listarTodos().getLast();
                assertEquals("Criado pelo gerenciador", outro.getNome());
                assertEquals(0, outro.getEstoque());
                assertNotEquals(criado.getCodigo(), outro.getCodigo());
                assertEquals(3, dados.getProdutoDAO().listarTodos().size());
            } finally {
                new ArrayList<>(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    @Test
    void buscaExistenteOuVaziaNaoOfereceCriacaoEProdutoInativoTemOrientacao() throws Exception {
        noJavaFx(() -> {
            DialogosTeste dialogos = new DialogosTeste();
            DadosAplicacao dados = new DadosAplicacao();
            dados.getProdutoDAO().salvar(null, "ATIVO", "Descrição ativa", BigDecimal.ONE, 1, true);
            dados.getProdutoDAO().salvar(null, "INATIVO", "Arquivado", BigDecimal.ONE, 0, false);
            Parent raiz = abrirPrincipal(dados, dialogos);
            Stage janela = new Stage();
            janela.setScene(new Scene(raiz, 1280, 700));
            janela.show();
            raiz.applyCss();
            raiz.layout();
            try {
                assertFalse(controle(raiz, "avisoBuscaProduto", HBox.class).isVisible());
                texto(raiz, "campoBuscaProduto", "Descrição ativa");
                assertFalse(controle(raiz, "avisoBuscaProduto", HBox.class).isVisible());
                texto(raiz, "campoBuscaProduto", "Arquivado");
                assertTrue(controle(raiz, "avisoBuscaProduto", HBox.class).isVisible());
                assertFalse(controle(raiz, "btnCriarProduto", Button.class).isVisible());
                assertTrue(controle(raiz, "labelResultadoBusca", Label.class).getText().contains("inativos"));
                texto(raiz, "campoBuscaProduto", " ");
                assertFalse(controle(raiz, "avisoBuscaProduto", HBox.class).isVisible());
                assertTrue(dialogos.erros.isEmpty());
            } finally {
                new ArrayList<>(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    void cadastroConsultaPdvPagamentoHistoricoEEstoqueFuncionamIntegrados() throws Exception {
        noJavaFx(() -> {
            DialogosTeste dialogos = new DialogosTeste();
            DadosAplicacao dados = new DadosAplicacao();
            Parent raiz = abrirPrincipal(dados, dialogos);
            Stage janela = new Stage();
            janela.setScene(new Scene(raiz, 1280, 700));
            janela.show();
            raiz.applyCss();
            raiz.layout();
            try {
                assertTrue(dados.getProdutoDAO().listarTodos().isEmpty());
                clicar(raiz, "btnMenuProdutos");
                clicar(raiz, "btnNovoProduto");
                Parent formulario = modal("campoNomeProduto");
                formulario.applyCss();
                formulario.layout();
                texto(formulario, "campoNomeProduto", "Produto usado apenas no teste");
                texto(formulario, "campoCodigoProduto", "TESTE-01");
                texto(formulario, "campoPrecoProduto", "12,50");
                texto(formulario, "campoEstoqueProduto", "10");
                clicar(formulario, "btnSalvar");
                assertEquals(1, dados.getProdutoDAO().listarTodos().size());
                Produto produto = dados.getProdutoDAO().listarTodos().getFirst();
                TableView<Produto> produtos = controle(raiz, "tabelaProdutos", TableView.class);
                assertSame(produto, produtos.getSelectionModel().getSelectedItem());
                clicar(raiz, "btnVenderProduto");
                assertSame(produto, dados.getCarrinho().getItens().getFirst().getProduto());
                TableView<ItemCarrinho> carrinho = controle(raiz, "tabelaCarrinho", TableView.class);
                carrinho.getSelectionModel().selectFirst();
                clicar(raiz, "btnAlterarQuantidade");
                assertEquals(3, dados.getCarrinho().getQuantidadeTotal());

                clicar(raiz, "btnMenuClientes");
                clicar(raiz, "btnNovoCliente");
                Parent formCliente = modal("campoNomeCliente");
                formCliente.applyCss();
                formCliente.layout();
                texto(formCliente, "campoNomeCliente", "Cliente usado apenas no teste");
                texto(formCliente, "campoDocumentoCliente", "12345678901");
                texto(formCliente, "campoTelefoneCliente", "11999999999");
                clicar(formCliente, "btnSalvar");
                clicar(raiz, "btnUsarCliente");
                assertSame(dados.getClienteDAO().listarTodos().getFirst(), dados.getCarrinho().getCliente());
                assertEquals(3, carrinho.getItems().getFirst().getQuantidade());
                clicar(raiz, "btnPagamento");
                Parent pagamento = modal("campoValorRecebido");
                pagamento.applyCss();
                pagamento.layout();
                assertTrue(controle(pagamento, "btnConfirmarPagamento", Button.class).isDisabled());
                texto(pagamento, "campoValorRecebido", "20,00");
                assertTrue(controle(pagamento, "btnConfirmarPagamento", Button.class).isDisabled());
                texto(pagamento, "campoValorRecebido", "40,00");
                assertTrue(controle(pagamento, "labelTroco", Label.class).getText().contains("2,50"));
                clicar(pagamento, "btnConfirmarPagamento");
                assertEquals(7, produto.getEstoque());
                assertTrue(dados.getCarrinho().isVazio());
                assertEquals(1, dados.getVendaDAO().listarTodos().size());
                assertEquals(1, dialogos.avisos.size());
                clicar(raiz, "btnMenuVendas");
                TableView<Venda> vendas = controle(raiz, "tabelaVendas", TableView.class);
                assertEquals(1, vendas.getItems().size());
                vendas.getSelectionModel().selectFirst();
                clicar(raiz, "btnDetalhesVenda");
                Parent detalhe = modal("labelNumeroVenda");
                detalhe.applyCss();
                detalhe.layout();
                assertEquals("#1", controle(detalhe, "labelNumeroVenda", Label.class).getText());
                assertTrue(controle(detalhe, "labelTotalVenda", Label.class).getText().contains("37,50"));
                clicar(detalhe, "btnFecharDetalhes");
                clicar(raiz, "btnMenuProdutos");
                assertSame(produto, produtos.getItems().getFirst());
                assertEquals(7, produtos.getItems().getFirst().getEstoque());
                produtos.getSelectionModel().selectFirst();
                clicar(raiz, "btnExcluirProduto");
                assertEquals(1, dialogos.erros.size());
                assertTrue(dialogos.erros.getFirst().contains("venda"));
                assertEquals(1, dados.getProdutoDAO().listarTodos().size());
                texto(raiz, "campoBuscaProduto", "Não existe este produto");
                assertTrue(produtos.getItems().isEmpty());
                assertTrue(controle(raiz, "btnEditarProduto", Button.class).isDisabled());
                texto(raiz, "campoBuscaProduto", "");
                clicar(raiz, "btnMenuPdv");
                texto(raiz, "campoBuscaProduto", "TESTE-01");
                texto(raiz, "campoQuantidade", "2");
                clicar(raiz, "btnAdicionarItem");
                assertEquals(2, dados.getCarrinho().getQuantidadeTotal());
                clicar(raiz, "btnPagamento");
                Parent segundoPagamento = modal("campoValorRecebido");
                controle(segundoPagamento, "opcaoCartao", ToggleButton.class).fire();
                assertTrue(controle(segundoPagamento, "campoValorRecebido", TextField.class).isDisabled());
                controle(segundoPagamento, "opcaoCartao", ToggleButton.class).fire();
                assertTrue(controle(segundoPagamento, "opcaoCartao", ToggleButton.class).isSelected());
                controle(segundoPagamento, "opcaoPix", ToggleButton.class).fire();
                clicar(segundoPagamento, "btnVoltarPdv");
                assertEquals(2, dados.getCarrinho().getQuantidadeTotal());
                assertEquals(7, produto.getEstoque());
                clicar(raiz, "btnPagamento");
                Parent terceiroPagamento = modal("campoValorRecebido");
                controle(terceiroPagamento, "opcaoPix", ToggleButton.class).fire();
                clicar(terceiroPagamento, "btnConfirmarPagamento");
                assertEquals(5, produto.getEstoque());
                assertEquals(FormaPagamento.PIX, dados.getVendaDAO().listarTodos().getLast().getFormaPagamento());
                assertEquals(1, dialogos.erros.size());
            } finally {
                new ArrayList<>(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    @Test
    void todasAsTelasCarregamComFXMLLoaderPadraoSemFabricaDeControllers() throws Exception {
        noJavaFx(() -> {
            for (String nome : List.of(
                "principal",
                "pdv",
                "produtos-consulta",
                "produto-formulario",
                "clientes-consulta",
                "cliente-formulario",
                "pagamento",
                "vendas-consulta",
                "venda-detalhes"
            )) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/com/mercatto/view/" + nome + ".fxml"));
                try {
                    Parent raiz = loader.load();
                    Object controller = loader.getController();
                    assertNotNull(controller);
                    assertEquals(0, controller.getClass().getConstructors()[0].getParameterCount());
                    assertFalse(raiz.getStylesheets().isEmpty());
                    Scene cena = new Scene(raiz, raiz.prefWidth(-1), raiz.prefHeight(-1));
                    raiz.applyCss();
                    raiz.layout();
                    assertSame(raiz, cena.getRoot());
                } catch (IOException e) {
                    throw new AssertionError("Falha no FXML " + nome, e);
                }
            }
        });
    }
}
