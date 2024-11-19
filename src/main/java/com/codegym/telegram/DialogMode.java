package com.codegym.telegram;

public enum DialogMode {
    MAIN("main"),
    PROFILE("profile"),
    OPENER("opener"),
    MESSAGE("message"),
    DATE("date"),
    GPT("gpt"),
    ;

    private final String fileKey;

    DialogMode(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getFileKey() {
        return fileKey;
    }
}
