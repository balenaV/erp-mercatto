package br.com.mercatto.model;

import java.util.Locale;

public final class Cliente {

    private final long id;
    private String nome;
    private String documento;
    private String telefone;
    private boolean ativo;

    public Cliente(long id, String nome, String documento, String telefone, boolean ativo) {
        this.id = id;
        setNome(nome);
        setDocumento(documento);
        setTelefone(telefone);
        setAtivo(ativo);
    }

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validacao.texto(nome, "o nome do cliente", 120);
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        String normalizado = normalizarDocumento(documento);
        Validacao.exigir(
            normalizado.matches("[0-9]{11}|[A-Z0-9]{12}[0-9]{2}"),
            "Informe um CPF com 11 dígitos ou CNPJ com 14 caracteres (os dois últimos numéricos)."
        );
        this.documento = normalizado;
    }

    public static String normalizarDocumento(String documento) {
        return documento == null ? "" : documento.replaceAll("[.\\-/\\s]", "").toUpperCase(Locale.ROOT);
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        String normalizado = telefone == null ? "" : telefone.replaceAll("[()\\-\\s+]", "");
        Validacao.exigir(normalizado.matches("[0-9]{10,11}"), "Informe um telefone com DDD e 10 ou 11 dígitos.");
        this.telefone = normalizado;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return nome + " — " + documento;
    }
}
