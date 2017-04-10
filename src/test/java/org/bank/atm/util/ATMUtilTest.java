package org.bank.atm.util;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class ATMUtilTest {

    private static final Logger log = Logger.getLogger(ATMUtilTest.class.getName());

    private static final long INVALID_AMOUNT_1 = 123;
    private static final long INVALID_AMOUNT_2 = 255;
    private static final long INVALID_AMOUNT_3 = 10;
    private static final long VALID_AMOUNT_1 = 50;
    private static final long VALID_AMOUNT_2 = 170;
    private static final long VALID_AMOUNT_TOO_MUCH = 240;

    private ATMUtil util;

    @Before
    public void init() {
        util = new ATMUtil();
        log.info("Unit test initialised");
    }

    @Test
    public void testReplenish() {
        log.info("testReplenish started");
        Map<Banknote, Long> initialSum = createBanknotes(1, 1, 1, 1);
        util.replenishATM(initialSum);

        Map<Banknote, Long> expectedBanknotesInitial = util.getBanknoteCount();
        assertThat(initialSum, equalTo(expectedBanknotesInitial));

        Map<Banknote, Long> nextSum = createBanknotes(1, 2, 3, 4);
        util.replenishATM(nextSum);

        Map<Banknote, Long> expectedBanknotesNext = util.getBanknoteCount();
        assertThat(2L, equalTo(expectedBanknotesNext.get(Banknote.FIVE)));
        assertThat(3L, equalTo(expectedBanknotesNext.get(Banknote.TEN)));
        assertThat(4L, equalTo(expectedBanknotesNext.get(Banknote.TWNETY)));
        assertThat(5L, equalTo(expectedBanknotesNext.get(Banknote.FIFTY)));
    }

    @Test
    public void testIsAmountValid() {
        log.info("testIsAmountValid started");
        assertThat(false, equalTo(util.isAmountValid(INVALID_AMOUNT_1)));
        assertThat(false, equalTo(util.isAmountValid(INVALID_AMOUNT_2)));
        assertThat(false, equalTo(util.isAmountValid(INVALID_AMOUNT_3)));
        assertThat(true, equalTo(util.isAmountValid(VALID_AMOUNT_1)));
    }

    @Test
    public void testCheckBalanceForATM() {
        log.info("testCheckBalanceForATM started");
        Map<Banknote, Long> initialSum = createBanknotes(1, 1, 1, 1);
        util.replenishATM(initialSum);
        assertThat(StatusCode.OK, equalTo(util.checkBalanceForATM(VALID_AMOUNT_1)));
        assertThat(StatusCode.ATM_NOT_ENOUGH_MONEY, equalTo(util.checkBalanceForATM(VALID_AMOUNT_TOO_MUCH)));
    }

    @Test
    public void testWithdrawAmount() {
        log.info("testWithdrawAmount started");
        Map<Banknote, Long> initialSum = createBanknotes(5, 5, 5, 5);
        util.replenishATM(initialSum);

        Map<Banknote, Long> expectedBanknoteCountToWithdrawMap1 = new TreeMap<>(new BanknoteComparator());
        expectedBanknoteCountToWithdrawMap1.put(Banknote.FIVE, 2L);
        expectedBanknoteCountToWithdrawMap1.put(Banknote.TWNETY, 2L);

        assertThat(expectedBanknoteCountToWithdrawMap1,
                equalTo(util.withdrawAmount(VALID_AMOUNT_1)));

        Map<Banknote, Long> expectedBanknoteCountToWithdrawMap2 = new TreeMap<>(new BanknoteComparator());
        expectedBanknoteCountToWithdrawMap2.put(Banknote.FIVE, 2L);
        expectedBanknoteCountToWithdrawMap2.put(Banknote.TEN, 1L);
        expectedBanknoteCountToWithdrawMap2.put(Banknote.FIFTY, 3L);

        assertThat(expectedBanknoteCountToWithdrawMap2,
                equalTo(util.withdrawAmount(VALID_AMOUNT_2)));
    }

    @Test
    public void testWithdrawAmountNoMoreFives() {
        log.info("testWithdrawAmountNoMoreFives started");
        Map<Banknote, Long> initialSum = createBanknotes(0, 5, 5, 5);
        util.replenishATM(initialSum);

        Map<Banknote, Long> expectedBanknoteCountToWithdrawMap1 = new TreeMap<>(new BanknoteComparator());
        expectedBanknoteCountToWithdrawMap1.put(Banknote.FIFTY, 1L);

        assertThat(expectedBanknoteCountToWithdrawMap1,
                equalTo(util.withdrawAmount(VALID_AMOUNT_1)));
    }

    private Map<Banknote, Long> createBanknotes(long five, long ten, long twenty, long fifty) {
        Map<Banknote, Long> banknoteCountMap = new TreeMap<>(new BanknoteComparator());
        banknoteCountMap.put(Banknote.FIVE, five);
        banknoteCountMap.put(Banknote.TEN, ten);
        banknoteCountMap.put(Banknote.TWNETY, twenty);
        banknoteCountMap.put(Banknote.FIFTY, fifty);
        log.info("Initialised banknotes");

        return banknoteCountMap;
    }
}
