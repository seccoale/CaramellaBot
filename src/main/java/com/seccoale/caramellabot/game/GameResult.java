package com.seccoale.caramellabot.game;

import lombok.Getter;

@Getter
public class GameResult {
    private String story = "";

    public void attachResult(String result){
        story = story + "\n"+result;
    }

}
