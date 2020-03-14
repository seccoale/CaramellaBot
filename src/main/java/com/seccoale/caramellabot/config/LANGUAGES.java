package com.seccoale.caramellabot.config;

public enum LANGUAGES {
    ITA("ITA"), ENG("ENG");
    private String language;

    LANGUAGES(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }
}
