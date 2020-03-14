package com.seccoale.caramellabot.config;

public enum LANGUAGE {
    ITA("ITA"), ENG("ENG");
    private String language;

    LANGUAGE(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }
}
