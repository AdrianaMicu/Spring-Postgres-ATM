package org.bank.atm.util;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ATMUtil {

    static Logger log = Logger.getLogger(ATMUtil.class.getName());

    public static final long MIN_WITHDRAW_AMOUNT = 20;
    public static final long MAX_WITHDRAW_AMOUNT = 250;

    public ATMUtil() {
    }

    private Map<Banknote, Long> banknoteCountMap = initBanknoteCountMap();

    public void replenishATM(Map<Banknote, Long> banknoteCountReceivedMap) {
        banknoteCountMap.put(Banknote.FIVE, banknoteCountMap.get(Banknote.FIVE) + banknoteCountReceivedMap.get(Banknote.FIVE));
        banknoteCountMap.put(Banknote.TEN, banknoteCountMap.get(Banknote.TEN) + banknoteCountReceivedMap.get(Banknote.TEN));
        banknoteCountMap.put(Banknote.TWNETY, banknoteCountMap.get(Banknote.TWNETY) + banknoteCountReceivedMap.get(Banknote.TWNETY));
        banknoteCountMap.put(Banknote.FIFTY, banknoteCountMap.get(Banknote.FIFTY) + banknoteCountReceivedMap.get(Banknote.FIFTY));

        log.info("ATM replenished");
    }

    public boolean isAmountValid(long amount) {
        if (amount >= MIN_WITHDRAW_AMOUNT
                && amount <= MAX_WITHDRAW_AMOUNT
                && (amount % 5 == 0)) {
            return true;
        }
        return false;
    }

    public StatusCode checkBalanceForATM(long amount) {
        long totalBalance = banknoteCountMap.entrySet().stream().map(banknoteCountEntry ->
                banknoteCountEntry.getKey().getValue() * banknoteCountEntry.getValue())
                .reduce(0L, Long::sum);

        if (totalBalance >= amount) {
            return StatusCode.OK;
        }
        return StatusCode.ATM_NOT_ENOUGH_MONEY;
    }

    public Map<Banknote, Long> withdrawAmount(long amount) {
        Map<Banknote, Long> banknoteCountToWithdrawMap = new TreeMap<>(new BanknoteComparator());

        if (banknoteCountMap.get(Banknote.FIVE) > 0) {
            banknoteCountToWithdrawMap.put(Banknote.FIVE, 1L);
            banknoteCountMap.put(Banknote.FIVE, banknoteCountMap.get(Banknote.FIVE) - 1);

            log.info("ATM will give at least 1 five banknote");

            return calculateSmallestNumberOfBanknotes(banknoteCountToWithdrawMap, amount - 5);
        } else {

            log.info("ATM cannot give any five banknotes");

            return calculateSmallestNumberOfBanknotes(banknoteCountToWithdrawMap, amount);
        }
    }

    private Map<Banknote, Long> calculateSmallestNumberOfBanknotes(Map<Banknote, Long> banknoteCountToWithdrawMap, long amount) {
        log.info("Calculating optimal number of banknotes");

        Map<Banknote, Long> banknoteCountMapCopy = new TreeMap<>(new BanknoteComparator());
        banknoteCountMapCopy.putAll(banknoteCountMap);
        for (Map.Entry<Banknote, Long> banknoteCountEntry : banknoteCountMapCopy.entrySet()) {
            long banknoteCount = banknoteCountEntry.getValue();
            while (amount >= banknoteCountEntry.getKey().getValue() && banknoteCount > 0) {
                long currentBanknoteCount = banknoteCountToWithdrawMap.get(banknoteCountEntry.getKey()) == null ? 0 : banknoteCountToWithdrawMap.get(banknoteCountEntry.getKey());
                banknoteCountToWithdrawMap.put(banknoteCountEntry.getKey(), currentBanknoteCount + 1);
                amount -= banknoteCountEntry.getKey().getValue();
                banknoteCount--;
            }
            banknoteCountMap.put(banknoteCountEntry.getKey(), banknoteCount);
        }
        return banknoteCountToWithdrawMap;
    }

    public Map<Banknote, Long> getBanknoteCount() {
        return banknoteCountMap;
    }

    private Map<Banknote, Long> initBanknoteCountMap() {
        Map<Banknote, Long> banknoteCountMap = new TreeMap<>(new BanknoteComparator());
        banknoteCountMap.put(Banknote.FIVE, 0L);
        banknoteCountMap.put(Banknote.TEN, 0L);
        banknoteCountMap.put(Banknote.TWNETY, 0L);
        banknoteCountMap.put(Banknote.FIFTY, 0L);

        return banknoteCountMap;
    }
}
