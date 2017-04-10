package org.bank.atm.util;

public enum Banknote {
    FIVE(5), TEN(10),  TWNETY(20), FIFTY(50);
    private final int value;

    Banknote(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
