package com.seccoale.caramellabot.game.exception;

public class PlayerAlreadyInGameException extends Exception {
    public PlayerAlreadyInGameException(String message) {
        super(message);
    }

    public PlayerAlreadyInGameException(String message, Throwable cause) {
        super(message, cause);
    }
}
