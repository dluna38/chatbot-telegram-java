package com.codegym.telegram;

public enum StarDate {
    DATE_GOSLING("Ryan Gosling"),
    DATE_GRANDE("Ariana Grande"),
    DATE_HARDY("Tom Hardy"),
    DATE_ROBBIE("Margot Robbie"),
    DATE_ZENDAYA("Zendaya Maree");
    private final String simpleName;
    StarDate(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getSimpleName() {
        return simpleName;
    }
    public String getName() {
        return this.name();
    }
    public String g() {
        return this.name();
    }
    public String getFileKey() {
        return this.name().toLowerCase();
    }
}
