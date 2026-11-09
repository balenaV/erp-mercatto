package br.com.mercatto.util;

import br.com.mercatto.model.RegraNegocioException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Formatos {

    private Formatos() {}

    public static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }

    public static String decimal(BigDecimal valor) {
        return valor.toPlainString().replace('.', ',');
    }

    public static BigDecimal lerMoeda(String texto) {
        String limpo = texto == null ? "" : texto.strip().replaceFirst("^R\\$\\s*", "");
        if (limpo.matches("[0-9]{1,3}(\\.[0-9]{3})+,[0-9]{1,2}")) limpo = limpo.replace(".", "");
        if (!limpo.matches("[0-9]+([,.][0-9]{1,2})?")) throw new RegraNegocioException(
            "Informe um valor válido com até duas casas decimais, como 25,90."
        );
        return new BigDecimal(limpo.replace(',', '.'));
    }

    public static int inteiro(String texto, String campo) {
        try {
            if (texto == null || !texto.strip().matches("[0-9]+")) throw new NumberFormatException();
            return Integer.parseInt(texto.strip());
        } catch (NumberFormatException e) {
            throw new RegraNegocioException(campo + ": informe um número inteiro válido.");
        }
    }
}
