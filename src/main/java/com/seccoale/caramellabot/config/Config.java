package com.seccoale.caramellabot.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
public class Config {

    private BotConfig bot;
    private Map<String, String[]> sentences;

    @Getter @Setter
    public final static class BotConfig {
        private String token;
        private String username;
    }
}
