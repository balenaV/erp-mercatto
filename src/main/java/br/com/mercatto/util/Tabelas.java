package br.com.mercatto.util;

import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.*;

public final class Tabelas {

    private Tabelas() {}

    public static <T, V> void coluna(TableColumn<T, V> coluna, Function<T, V> extrair) {
        coluna.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(extrair.apply(c.getValue())));
    }

    public static <T, V> void coluna(TableColumn<T, V> coluna, Function<T, V> extrair, Function<V, String> formatar) {
        coluna(coluna, extrair);
        coluna.setCellFactory(c ->
            new TableCell<>() {
                @Override
                protected void updateItem(V valor, boolean vazia) {
                    super.updateItem(valor, vazia);
                    setText(vazia || valor == null ? null : formatar.apply(valor));
                }
            }
        );
    }

    public static <T> void duploClique(TableView<T> tabela, Runnable acao) {
        tabela.setRowFactory(t -> {
            TableRow<T> linha = new TableRow<>();
            linha.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !linha.isEmpty()) acao.run();
            });
            return linha;
        });
    }

    public static <T> void combo(ComboBox<T> combo, String semValor, Function<T, String> texto) {
        combo.setCellFactory(c -> celula(semValor, texto));
        combo.setButtonCell(celula(semValor, texto));
    }

    private static <T> ListCell<T> celula(String semValor, Function<T, String> texto) {
        return new ListCell<>() {
            @Override
            protected void updateItem(T valor, boolean vazia) {
                super.updateItem(valor, vazia);
                setText(valor == null ? semValor : texto.apply(valor));
            }
        };
    }
}
