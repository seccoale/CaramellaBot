package com.seccoale.caramellabot.config;

import lombok.Getter;

@Getter
public enum COMMAND {

    NEW_GAME("/newgame"),
    NEW_GAME_ITA("/newgame ita"),
    NEW_GAME_ENG("/newgame eng"),
    JOIN_GAME("/join"),
    END_GAME("/endgame"),
    START_GAME("/start");

    private String commandString;

    COMMAND(String commandString) {
        this.commandString = commandString;
    }
}
