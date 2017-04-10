package org.bank.atm.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.bank.atm.model.Account;
import org.bank.atm.service.AccountRepository;
import org.bank.atm.util.ATMUtil;
import org.bank.atm.util.Banknote;
import org.bank.atm.util.BanknoteComparator;
import org.bank.atm.util.StatusCode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ATMControllerTest {

    private static final Logger log = Logger.getLogger(ATMControllerTest.class.getName());

    private static final String ACCOUNT_NUMBER_1 = "1";
    private static final String ACCOUNT_NUMBER_2 = "2";

    private static final long INVALID_AMOUNT_1 = 123;
    private static final long INVALID_AMOUNT_2 = 255;
    private static final long INVALID_AMOUNT_3 = 10;

    private static final long VALID_AMOUNT = 50;

    @Mock
    AccountRepository accountRepository;

    @Mock
    ATMUtil atmUtil;

    @InjectMocks
    private ATMController atmController;

    @Test
    public void testReplenish() {
        log.info("testReplenish started");
        atmController.replenishATM(1, 1, 1, 1);

        verify(atmUtil).replenishATM(Matchers.any());
        verifyNoMoreInteractions(atmUtil);
    }

    @Test
    public void testCheckBalanceForAccount() {
        log.info("testCheckBalanceForAccount started");
        Account account1 = new Account(ACCOUNT_NUMBER_1, 0);
        Account account2 = new Account(ACCOUNT_NUMBER_2, 0);

        when(accountRepository.checkBalanceForAccount(ACCOUNT_NUMBER_1)).thenReturn(account1);
        when(accountRepository.checkBalanceForAccount(ACCOUNT_NUMBER_2)).thenReturn(account2);

        String result1 = atmController.checkBalanceForAccount(ACCOUNT_NUMBER_1);
        String result2 = atmController.checkBalanceForAccount(ACCOUNT_NUMBER_2);

        assertThat(account1.toString(), is(equalTo(result1)));
        assertThat(account2.toString(), is(equalTo(result2)));
    }

    @Test
    public void testWithdrawAmountFromAccountInvalidAmount() {
        log.info("testWithdrawAmountFromAccountInvalidAmount started");
        when(atmUtil.isAmountValid(INVALID_AMOUNT_1)).thenReturn(false);
        when(atmUtil.isAmountValid(INVALID_AMOUNT_2)).thenReturn(false);
        when(atmUtil.isAmountValid(INVALID_AMOUNT_3)).thenReturn(false);

        String result1 = atmController.withdrawAmountFromAccount(ACCOUNT_NUMBER_1, INVALID_AMOUNT_1);
        String result2 = atmController.withdrawAmountFromAccount(ACCOUNT_NUMBER_1, INVALID_AMOUNT_2);
        String result3 = atmController.withdrawAmountFromAccount(ACCOUNT_NUMBER_1, INVALID_AMOUNT_3);

        assertThat(StatusCode.INVALID_AMOUNT.getValue(), is(equalTo(result1)));
        assertThat(StatusCode.INVALID_AMOUNT.getValue(), is(equalTo(result2)));
        assertThat(StatusCode.INVALID_AMOUNT.getValue(), is(equalTo(result3)));
    }

    @Test
    public void testWithdrawAmountFromAccountNotEnoughMoneyInAccount() {
        log.info("testWithdrawAmountFromAccountNotEnoughMoneyInAccount started");
        when(atmUtil.isAmountValid(VALID_AMOUNT)).thenReturn(true);
        when(accountRepository.checkBalanceForAccount(ACCOUNT_NUMBER_1)).thenReturn(new Account(ACCOUNT_NUMBER_1, 0));

        String result = atmController.withdrawAmountFromAccount(ACCOUNT_NUMBER_1, VALID_AMOUNT);

        assertThat(StatusCode.ACCOUNT_NOT_ENOUGH_MONEY.getValue(), is(equalTo(result)));
    }

    @Test
    public void testWithdrawAmountFromAccountATMStatusNotOK() {
        log.info("testWithdrawAmountFromAccountATMStatusNotOK started");
        when(atmUtil.isAmountValid(VALID_AMOUNT)).thenReturn(true);
        when(accountRepository.checkBalanceForAccount(ACCOUNT_NUMBER_1)).thenReturn(new Account(ACCOUNT_NUMBER_1, 2000));
        when(atmUtil.checkBalanceForATM(VALID_AMOUNT)).thenReturn(StatusCode.ATM_NOT_ENOUGH_MONEY);

        String result = atmController.withdrawAmountFromAccount(ACCOUNT_NUMBER_1, VALID_AMOUNT);

        assertThat(StatusCode.ATM_NOT_ENOUGH_MONEY.getValue(), is(equalTo(result)));
    }

    @Test
    public void testWithdrawAmountFromAccountWithdrawal() {
        log.info("testWithdrawAmountFromAccountWithdrawal started");
        Map<Banknote, Long> banknoteCountToWithdrawMap = new TreeMap<>(new BanknoteComparator());
        banknoteCountToWithdrawMap.put(Banknote.FIVE, 2L);
        banknoteCountToWithdrawMap.put(Banknote.TWNETY, 2L);

        when(atmUtil.isAmountValid(VALID_AMOUNT)).thenReturn(true);
        when(accountRepository.checkBalanceForAccount(ACCOUNT_NUMBER_1)).thenReturn(new Account(ACCOUNT_NUMBER_1, 2000));
        when(atmUtil.checkBalanceForATM(VALID_AMOUNT)).thenReturn(StatusCode.OK);
        when(atmUtil.withdrawAmount(VALID_AMOUNT)).thenReturn(banknoteCountToWithdrawMap);

        String result = atmController.withdrawAmountFromAccount(ACCOUNT_NUMBER_1, VALID_AMOUNT);

        assertThat(banknoteCountToWithdrawMap.toString(), is(equalTo(result)));
    }
}
