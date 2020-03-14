package com.seccoale.caramellabot.game.exception;

public class GameAlreadyStartedException extends Exception {
    public GameAlreadyStartedException(String message) {
        super(message);
    }

    public GameAlreadyStartedException(String message, Throwable cause) {
        super(message, cause);
    }
}
