package br.com.mercatto.util;

import java.util.Optional;
import java.util.function.Supplier;
import javafx.scene.control.*;
import javafx.stage.Window;

public final class DialogosJavaFx implements Dialogos {

    private final Supplier<Window> janela;

    public DialogosJavaFx(Supplier<Window> janela) {
        this.janela = janela;
    }

    private Alert alerta(Alert.AlertType tipo, String mensagem) {
        Alert alerta = new Alert(tipo);
        if (janela.get() != null) alerta.initOwner(janela.get());
        alerta.setTitle("Mercatto");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        return alerta;
    }

    @Override
    public void erro(String mensagem) {
        alerta(Alert.AlertType.WARNING, mensagem).showAndWait();
    }

    @Override
    public void informar(String mensagem) {
        alerta(Alert.AlertType.INFORMATION, mensagem).showAndWait();
    }

    @Override
    public boolean confirmar(String mensagem) {
        Alert alerta = alerta(Alert.AlertType.CONFIRMATION, mensagem);
        ButtonType confirmar = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alerta.getButtonTypes().setAll(confirmar, cancelar);
        return alerta.showAndWait().orElse(cancelar) == confirmar;
    }

    @Override
    public Optional<String> solicitarQuantidade(int atual) {
        TextInputDialog dialogo = new TextInputDialog(Integer.toString(atual));
        if (janela.get() != null) dialogo.initOwner(janela.get());
        dialogo.setTitle("Quantidade");
        dialogo.setHeaderText("Alterar quantidade do item");
        dialogo.setContentText("Quantidade:");
        return dialogo.showAndWait();
    }
}
