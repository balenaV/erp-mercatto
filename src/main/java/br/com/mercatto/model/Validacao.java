package br.com.mercatto.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Validacao {

    private Validacao() {}

    public static void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new RegraNegocioException(mensagem);
    }

    public static String texto(String valor, String campo, int limite) {
        String texto = valor == null ? "" : valor.strip();
        exigir(!texto.isBlank(), "Informe " + campo + ".");
        exigir(texto.length() <= limite, campo + ": use até " + limite + " caracteres.");
        return texto;
    }

    public static BigDecimal dinheiro(BigDecimal valor, boolean aceitarZero) {
        exigir(valor != null, "Informe um valor monetário.");
        exigir(
            aceitarZero ? valor.signum() >= 0 : valor.signum() > 0,
            aceitarZero ? "O valor não pode ser negativo." : "O preço deve ser maior que zero."
        );
        try {
            return valor.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new RegraNegocioException("Use no máximo duas casas decimais.");
        }
    }

    public static int quantidade(int valor) {
        exigir(valor > 0, "A quantidade deve ser um número inteiro maior que zero.");
        return valor;
    }
}
