package com.seccoale.caramellabot.game.exception;

public class GameNotCreatableException extends Exception{
    public GameNotCreatableException(String reason) {
        super(reason);
    }
    public GameNotCreatableException(String reason, Throwable e) {
        super(reason, e);
    }
}
