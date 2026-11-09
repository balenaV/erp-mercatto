package br.com.mercatto.main;

import br.com.mercatto.controller.PrincipalController;
import br.com.mercatto.model.dao.DadosAplicacao;
import br.com.mercatto.util.Dialogos;
import br.com.mercatto.util.DialogosJavaFx;
import java.io.IOException;
import java.util.Locale;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Main extends Application {

    @Override
    public void start(Stage janela) throws IOException {
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));
        DadosAplicacao dados = new DadosAplicacao();
        Dialogos dialogos = new DialogosJavaFx(() -> janela);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/com/mercatto/view/principal.fxml"));
        Parent raiz = loader.load();
        PrincipalController controller = loader.getController();
        controller.iniciar(dados, dialogos);

        janela.setTitle("Mercatto ERP");
        janela.setScene(new Scene(raiz, 1280, 700));
        janela.setMinWidth(1080);
        janela.setMinHeight(730);
        janela.setOnCloseRequest(e -> {
            boolean temDados =
                dados.temDados() || !dados.getCarrinho().isVazio() || dados.getCarrinho().getCliente() != null;
            if (
                temDados &&
                !dialogos.confirmar(
                    "Fechar o Mercatto? Todos os cadastros, vendas e itens do carrinho desta execução serão descartados."
                )
            ) e.consume();
        });
        janela.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
