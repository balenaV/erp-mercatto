package br.com.mercatto.model.dao;

import static org.junit.jupiter.api.Assertions.*;

import br.com.mercatto.model.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FluxoVendaTest {

    private DadosAplicacao dados;
    private Carrinho carrinho;
    private ProdutoDAO produtos;
    private ClienteDAO clientes;
    private VendaDAO vendas;

    @BeforeEach
    void preparar() {
        dados = new DadosAplicacao(Clock.fixed(Instant.parse("2026-10-23T15:30:00Z"), ZoneId.of("America/Sao_Paulo")));
        carrinho = dados.getCarrinho();
        produtos = dados.getProdutoDAO();
        clientes = dados.getClienteDAO();
        vendas = dados.getVendaDAO();
    }

    private Produto produto(String codigo) {
        return produtos.salvar(null, codigo, "Produto de teste " + codigo, new BigDecimal("12.50"), 10, true);
    }

    private Cliente cliente() {
        return clientes.salvar(null, "Cliente de teste", "123.456.789-01", "(11) 99999-9999", true);
    }

    private Venda vender(Produto produto) {
        carrinho.adicionar(produto, 2);
        return vendas.finalizar(new PagamentoPix(carrinho.getTotal()));
    }

    @Test
    void cadaExecucaoComecaSemRegistros() {
        assertFalse(dados.temDados());
        assertTrue(carrinho.isVazio());
        assertNull(carrinho.getCliente());
        produto("A");
        assertTrue(dados.temDados());
        assertFalse(new DadosAplicacao().temDados());
    }

    @Test
    void codigoOpcionalGeraCodigoUnicoMesmoComCodigoManualExistente() {
        produto("prod-000001");
        Produto primeiro = produtos.salvar(null, " ", "Novo produto", new BigDecimal("12.50"), 0, true);
        Produto segundo = produtos.salvar(null, null, "Outro produto", BigDecimal.ONE, 0, true);
        assertEquals("PROD-000002", primeiro.getCodigo());
        assertEquals("PROD-000003", segundo.getCodigo());
        assertEquals(0, primeiro.getEstoque());
        assertTrue(primeiro.isAtivo());
    }

    @Test
    void editarSemInformarCodigoMantemIdentidadeECodigoOriginal() {
        Produto p = produto("CODIGO-ORIGINAL");
        assertSame(p, produtos.salvar(p, " ", "Nova descrição", BigDecimal.ONE, 0, true));
        assertEquals("CODIGO-ORIGINAL", p.getCodigo());
        assertEquals("Nova descrição", p.getNome());
    }

    @Test
    void codigoAutomaticoNaoDispensaDescricaoEValorValidos() {
        assertThrows(RegraNegocioException.class, () -> produtos.salvar(null, "", " ", BigDecimal.ONE, 0, true));
        assertThrows(RegraNegocioException.class, () ->
            produtos.salvar(null, "", "Descrição", BigDecimal.ZERO, 0, true)
        );
        assertThrows(RegraNegocioException.class, () -> produtos.salvar(null, "", "Descrição", null, 0, true));
        assertThrows(RegraNegocioException.class, () ->
            produtos.salvar(null, "", "Descrição", BigDecimal.ONE, -1, true)
        );
        assertTrue(dados.getProdutoDAO().listarTodos().isEmpty());
    }

    @Test
    void edicaoPreservaIdentidadeCompartilhada() {
        Produto p = produto("A");
        carrinho.adicionar(p, 1);
        assertSame(p, produtos.salvar(p, "B", "Atualizado", new BigDecimal("13.00"), 8, true));
        assertSame(p, carrinho.getItens().getFirst().getProduto());
        assertEquals("Atualizado", p.getNome());
        assertEquals(new BigDecimal("12.50"), carrinho.getTotal());
    }

    @Test
    void produtoDuplicadoIgnoraMaiusculasENaoMudaOriginal() {
        Produto a = produto("A");
        Produto b = produto("B");
        assertThrows(RegraNegocioException.class, () -> produtos.salvar(b, "a", "Novo", BigDecimal.ONE, 2, true));
        assertEquals("B", b.getCodigo());
        assertEquals(10, b.getEstoque());
        assertEquals(2, dados.getProdutoDAO().listarTodos().size());
        assertSame(a, produtos.consultar("a", true).getFirst());
    }

    @Test
    void edicaoInvalidaNaoAlteraNenhumCampo() {
        Produto p = produto("A");
        assertThrows(RegraNegocioException.class, () -> produtos.salvar(p, "B", "Novo", BigDecimal.ZERO, 1, false));
        assertEquals("A", p.getCodigo());
        assertEquals(10, p.getEstoque());
        assertTrue(p.isAtivo());
        Cliente c = cliente();
        assertThrows(RegraNegocioException.class, () -> clientes.salvar(c, "Outro", "12345678901", "12", false));
        assertEquals("Cliente de teste", c.getNome());
        assertTrue(c.isAtivo());
    }

    @Test
    void documentoDuplicadoConsideraPontuacao() {
        Cliente c = cliente();
        assertThrows(RegraNegocioException.class, () ->
            clientes.salvar(null, "Outro", "12345678901", "11999999999", true)
        );
        assertEquals(List.of(c), dados.getClienteDAO().listarTodos());
    }

    @Test
    void formatoCnpjAceitaLetrasEValidaFinalNumerico() {
        Cliente c = clientes.salvar(null, "Empresa", "AB.CDE.FGH/0001-23", "1133334444", true);
        assertEquals("ABCDEFGH000123", c.getDocumento());
        assertThrows(RegraNegocioException.class, () -> c.setDocumento("ABCDEFGH0001AB"));
    }

    @Test
    void consultarFiltraNomeCodigoDocumentoESituacao() {
        Produto p = produto("XYZ");
        produtos.alterarSituacao(p);
        assertTrue(produtos.consultar("xyz", true).isEmpty());
        assertEquals(List.of(p), produtos.consultar("XYZ", false));
        Cliente c = cliente();
        assertEquals(List.of(c), clientes.consultar("123.456", null));
        clientes.alterarSituacao(c);
        assertTrue(clientes.consultar("teste", true).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, 11, Integer.MAX_VALUE })
    void quantidadeInvalidaNaoEntraNoCarrinho(int quantidade) {
        Produto p = produto("A");
        assertThrows(RegraNegocioException.class, () -> carrinho.adicionar(p, quantidade));
        assertTrue(carrinho.isVazio());
        assertEquals(10, p.getEstoque());
    }

    @Test
    void adicionarAgrupaItensESoBaixaAoConcluir() {
        Produto p = produto("A");
        carrinho.adicionar(p, 2);
        carrinho.adicionar(p, 3);
        assertEquals(1, carrinho.getItens().size());
        assertEquals(5, carrinho.getQuantidadeTotal());
        assertEquals(new BigDecimal("62.50"), carrinho.getTotal());
        assertEquals(10, p.getEstoque());
        assertThrows(RegraNegocioException.class, () -> carrinho.adicionar(p, 6));
        assertEquals(5, carrinho.getQuantidadeTotal());
        carrinho.alterarQuantidade(carrinho.getItens().getFirst(), 1);
        assertEquals(new BigDecimal("12.50"), carrinho.getTotal());
    }

    @Test
    void produtosEClientesInativosSaoRecusados() {
        Produto p = produto("A");
        produtos.alterarSituacao(p);
        assertThrows(RegraNegocioException.class, () -> carrinho.adicionar(p, 1));
        Cliente c = cliente();
        clientes.alterarSituacao(c);
        assertThrows(RegraNegocioException.class, () -> carrinho.setCliente(c));
    }

    @Test
    void carrinhoVazioNaoGeraVenda() {
        assertThrows(RegraNegocioException.class, () -> vendas.finalizar(new PagamentoPix(BigDecimal.ZERO)));
        assertTrue(dados.getVendaDAO().listarTodos().isEmpty());
    }

    @Test
    void dinheiroInsuficienteNaoBaixaEstoqueNemLimpaCarrinho() {
        Produto p = produto("A");
        carrinho.adicionar(p, 2);
        assertThrows(RegraNegocioException.class, () ->
            vendas.finalizar(new PagamentoDinheiro(new BigDecimal("24.99")))
        );
        assertEquals(10, p.getEstoque());
        assertEquals(2, carrinho.getQuantidadeTotal());
        assertTrue(dados.getVendaDAO().listarTodos().isEmpty());
    }

    @Test
    void alteracaoEstoqueEmOutroModuloNaoPermiteVendaParcial() {
        Produto a = produto("A");
        Produto b = produto("B");
        carrinho.adicionar(a, 2);
        carrinho.adicionar(b, 3);
        b.setEstoque(1);
        assertThrows(RegraNegocioException.class, () -> vendas.finalizar(new PagamentoCartao(carrinho.getTotal())));
        assertEquals(10, a.getEstoque());
        assertEquals(1, b.getEstoque());
        assertEquals(2, carrinho.getItens().size());
        assertTrue(dados.getVendaDAO().listarTodos().isEmpty());
    }

    @Test
    void revalidaProdutoEClienteInativadosDepoisDeSelecionados() {
        Produto p = produto("A");
        Cliente c = cliente();
        carrinho.adicionar(p, 1);
        carrinho.setCliente(c);
        clientes.alterarSituacao(c);
        assertThrows(RegraNegocioException.class, () -> vendas.finalizar(new PagamentoPix(carrinho.getTotal())));
        carrinho.setCliente(null);
        produtos.alterarSituacao(p);
        assertThrows(RegraNegocioException.class, () -> vendas.finalizar(new PagamentoPix(carrinho.getTotal())));
        assertEquals(10, p.getEstoque());
        assertTrue(dados.getVendaDAO().listarTodos().isEmpty());
    }

    @Test
    void vendaDinheiroCalculaTrocoBaixaEstoqueELimpaCarrinho() {
        Produto p = produto("A");
        carrinho.adicionar(p, 2);
        carrinho.setCliente(cliente());
        Pagamento pagamento = new PagamentoDinheiro(new BigDecimal("30.00"));
        Venda venda = vendas.finalizar(pagamento);
        assertEquals(new BigDecimal("5.00"), venda.getTroco());
        assertEquals(new BigDecimal("25.00"), venda.getTotal());
        assertEquals(8, p.getEstoque());
        assertTrue(carrinho.isVazio());
        assertNull(carrinho.getCliente());
        assertEquals(1, venda.getNumero());
        assertEquals(LocalDateTime.of(2026, 10, 23, 12, 30), venda.getData());
        assertThrows(RegraNegocioException.class, () -> vendas.finalizar(pagamento));
        assertEquals(1, dados.getVendaDAO().listarTodos().size());
    }

    @Test
    void contratoPolimorficoAceitaTodasAsFormas() {
        BigDecimal total = new BigDecimal("12.50");
        List<Pagamento> pagamentos = List.of(
            new PagamentoDinheiro(new BigDecimal("20.00")),
            new PagamentoCartao(total),
            new PagamentoPix(total)
        );
        for (Pagamento pagamento : pagamentos) {
            Produto p = produto(pagamento.getForma().name());
            carrinho.adicionar(p, 1);
            Venda venda = vendas.finalizar(pagamento);
            assertEquals(pagamento.getForma(), venda.getFormaPagamento());
            assertEquals(pagamento.calcularTroco(total), venda.getTroco());
        }
        assertEquals(3, dados.getVendaDAO().listarTodos().size());
        assertThrows(RegraNegocioException.class, () -> new PagamentoCartao(BigDecimal.ONE).validar(total));
    }

    @Test
    void historicoNaoMudaComEdicaoDosCadastros() {
        Produto p = produto("A");
        Cliente c = cliente();
        carrinho.setCliente(c);
        Venda venda = vender(p);
        produtos.salvar(p, "B", "Novo produto", new BigDecimal("99.99"), 8, true);
        clientes.salvar(c, "Outro nome", "12345678901", "1133334444", true);
        assertEquals("Produto de teste A", venda.getItens().getFirst().getNome());
        assertEquals("A", venda.getItens().getFirst().getCodigo());
        assertEquals("Cliente de teste", venda.getClienteNome());
        assertEquals(new BigDecimal("25.00"), venda.getTotal());
        assertThrows(UnsupportedOperationException.class, () -> venda.getItens().clear());
    }

    @Test
    void exclusaoProtegeHistoricoEItensDoCarrinho() {
        Produto p = produto("A");
        Cliente c = cliente();
        carrinho.setCliente(c);
        carrinho.adicionar(p, 1);
        assertThrows(RegraNegocioException.class, () -> produtos.excluir(p));
        vendas.finalizar(new PagamentoPix(carrinho.getTotal()));
        assertThrows(RegraNegocioException.class, () -> produtos.excluir(p));
        assertThrows(RegraNegocioException.class, () -> clientes.excluir(c));
        Produto novo = produto("B");
        produtos.excluir(novo);
        assertFalse(dados.getProdutoDAO().listarTodos().contains(novo));
    }

    @Test
    void excluirClienteAindaSemVendaRetiraIdentificacaoDoCarrinho() {
        Cliente c = cliente();
        carrinho.setCliente(c);
        clientes.excluir(c);
        assertNull(carrinho.getCliente());
        assertTrue(dados.getClienteDAO().listarTodos().isEmpty());
    }

    @Test
    void filtrosDeVendasIncluemODiaInteiroEClienteInativo() {
        Produto p = produto("A");
        Cliente c = cliente();
        carrinho.setCliente(c);
        Venda primeira = vender(p);
        vender(p);
        clientes.alterarSituacao(c);
        LocalDate dia = LocalDate.of(2026, 10, 23);
        assertEquals(List.of(primeira), vendas.consultar(dia, dia, c));
        assertEquals(2, vendas.consultar(null, null, null).getFirst().getNumero());
        assertTrue(vendas.consultar(dia.plusDays(1), null, null).isEmpty());
        assertThrows(RegraNegocioException.class, () -> vendas.consultar(dia.plusDays(1), dia, null));
    }

    @Test
    void buscaComMilProdutosRetornaEmAteUmSegundo() {
        for (int i = 1; i <= 1000; i++) {
            produtos.salvar(null, "COD" + i, "Produto " + i, new BigDecimal("5.00"), 10, true);
        }
        long inicio = System.nanoTime();
        List<Produto> porNome = produtos.consultar("Produto 999", true);
        List<Produto> porCodigo = produtos.consultar("COD1000", true);
        long duracaoMs = (System.nanoTime() - inicio) / 1_000_000;
        assertEquals(1, porNome.size());
        assertEquals(1, porCodigo.size());
        assertTrue(duracaoMs <= 1000, "Busca levou " + duracaoMs + " ms");
    }
}
