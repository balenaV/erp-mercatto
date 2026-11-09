package br.com.mercatto.model.dao;

import br.com.mercatto.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ClienteDAO {

    private final DadosAplicacao dados;
    private final List<Cliente> clientes = new ArrayList<>();
    private long proximoId = 1;

    public ClienteDAO(DadosAplicacao dados) {
        this.dados = dados;
    }

    public List<Cliente> listarTodos() {
        return List.copyOf(clientes);
    }

    public Cliente salvar(Cliente original, String nome, String documento, String telefone, boolean ativo) {
        Cliente candidato = new Cliente(original == null ? 0 : original.getId(), nome, documento, telefone, ativo);
        Validacao.exigir(
            clientes.stream().noneMatch(c -> c != original && c.getDocumento().equals(candidato.getDocumento())),
            "Já existe um cliente com esse CPF/CNPJ."
        );
        if (original == null) {
            Cliente novo = new Cliente(
                proximoId++,
                candidato.getNome(),
                candidato.getDocumento(),
                candidato.getTelefone(),
                ativo
            );
            clientes.add(novo);
            return novo;
        }
        exigirCadastrado(original);
        original.setNome(candidato.getNome());
        original.setDocumento(candidato.getDocumento());
        original.setTelefone(candidato.getTelefone());
        original.setAtivo(ativo);
        return original;
    }

    public void alterarSituacao(Cliente cliente) {
        exigirCadastrado(cliente);
        cliente.setAtivo(!cliente.isAtivo());
    }

    public void excluir(Cliente cliente) {
        exigirCadastrado(cliente);
        Validacao.exigir(
            dados
                .getVendaDAO()
                .listarTodos()
                .stream()
                .noneMatch(v -> Long.valueOf(cliente.getId()).equals(v.getClienteId())),
            "O cliente já pertence a uma venda. Você pode inativá-lo."
        );
        if (dados.getCarrinho().getCliente() == cliente) dados.getCarrinho().setCliente(null);
        clientes.remove(cliente);
    }

    public List<Cliente> consultar(String busca, Boolean ativo) {
        String termo = busca == null ? "" : busca.strip().toLowerCase(Locale.ROOT);
        String documento = Cliente.normalizarDocumento(busca);
        return clientes
            .stream()
            .filter(c -> ativo == null || c.isAtivo() == ativo)
            .filter(
                c ->
                    c.getNome().toLowerCase(Locale.ROOT).contains(termo) ||
                    (!documento.isEmpty() && c.getDocumento().contains(documento))
            )
            .toList();
    }

    public void exigirCadastrado(Cliente cliente) {
        Validacao.exigir(cliente != null && clientes.contains(cliente), "O cliente não está mais cadastrado.");
    }
}
