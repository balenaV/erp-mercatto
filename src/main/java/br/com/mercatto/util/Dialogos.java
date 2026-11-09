package br.com.mercatto.util;

import java.util.Optional;

public interface Dialogos {
    void erro(String mensagem);
    void informar(String mensagem);
    boolean confirmar(String mensagem);
    Optional<String> solicitarQuantidade(int atual);
}
