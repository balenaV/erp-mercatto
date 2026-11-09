package br.com.mercatto.util;

import static org.junit.jupiter.api.Assertions.*;

import br.com.mercatto.model.RegraNegocioException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FormatosTest {

    @Test
    void interpretaValoresSemPerderCentavos() {
        assertEquals(new BigDecimal("1234.56"), Formatos.lerMoeda("R$ 1.234,56"));
        assertEquals(new BigDecimal("25.90"), Formatos.lerMoeda("25.90"));
        assertEquals(new BigDecimal("25.90"), Formatos.lerMoeda("25,90"));
        assertEquals(12, Formatos.inteiro("12", "Quantidade"));
        assertTrue(Formatos.moeda(new BigDecimal("1234.56")).contains("1.234,56"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "1,234", "1.234", "-2", "NaN", "2e3", "1.2,34", "1,2,3" })
    void rejeitaValoresAmbiguosOuInvalidos(String texto) {
        assertThrows(RegraNegocioException.class, () -> Formatos.lerMoeda(texto));
    }
}
