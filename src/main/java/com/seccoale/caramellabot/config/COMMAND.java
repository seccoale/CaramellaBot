package com.seccoale.caramellabot.config;

import lombok.Getter;

@Getter
public enum COMMAND {

    NEW_GAME("/newgame", "Starts a new game"),
    JOIN_GAME("/join", "Joins a starting game"),
    END_GAME("/endgame", "Ends a game"),
    START_GAME("/start", "Starts a game");

    private String commandString;
    private String help;

    COMMAND(String commandString, String help) {
        this.commandString = commandString;
        this.help = help;
    }
}
