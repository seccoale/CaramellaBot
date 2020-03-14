package com.seccoale.caramellabot.game.exception;

public class GameNotFoundException extends Exception {
    public GameNotFoundException(String s) {
        super(s);
    }
    public GameNotFoundException(String reason, Throwable e) {
        super(reason, e);
    }
}
