package org.bank.atm.util;

public enum StatusCode {
    OK(""),
    INVALID_AMOUNT("Please enter a valid amount (between " + ATMUtil.MIN_WITHDRAW_AMOUNT + " and " + ATMUtil.MAX_WITHDRAW_AMOUNT + " and multiple of 5)"),
    ATM_NOT_ENOUGH_MONEY("Not enough money in the ATM"),
    ACCOUNT_NOT_ENOUGH_MONEY("Not enough money in your account");

    private final String value;

    StatusCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
