package org.bank.atm.controller;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.bank.atm.service.AccountRepository;
import org.bank.atm.util.ATMUtil;
import org.bank.atm.util.Banknote;
import org.bank.atm.util.BanknoteComparator;
import org.bank.atm.util.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/atm")
public class ATMController {

    static Logger log = Logger.getLogger(ATMController.class.getName());

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ATMUtil atmUtil;

    @RequestMapping(value = "/replenish", method = RequestMethod.GET)
    public void replenishATM(@RequestParam(value="five") long fiveBanknotes,
                          @RequestParam(value="ten") long tenBanknotes,
                          @RequestParam(value="twenty") long twentyBanknotes,
                          @RequestParam(value="fifty") long fiftyBanknotes) {

        log.info("Replenish request: received banknotes for ATM replenish: five: " + fiveBanknotes
                + " ten " + tenBanknotes + " twenty " + twentyBanknotes
                + " fifty " + fiftyBanknotes);

        Map<Banknote, Long> banknoteCountReceivedMap = new TreeMap<>(new BanknoteComparator());
        banknoteCountReceivedMap.put(Banknote.FIVE, fiveBanknotes);
        banknoteCountReceivedMap.put(Banknote.TEN, tenBanknotes);
        banknoteCountReceivedMap.put(Banknote.TWNETY, twentyBanknotes);
        banknoteCountReceivedMap.put(Banknote.FIFTY, fiftyBanknotes);
        atmUtil.replenishATM(banknoteCountReceivedMap);
    }

    @RequestMapping(value = "/checkBalance", method = RequestMethod.GET)
    @ResponseBody
    public String checkBalanceForAccount(@RequestParam(value="accountNumber") String accountNumber) {
        log.info("Check balance for account request: account number is " + accountNumber);

        return accountRepository.checkBalanceForAccount(accountNumber).toString();
    }

    @RequestMapping(value = "/withdraw", method = RequestMethod.GET)
    @ResponseBody
    public String withdrawAmountFromAccount(@RequestParam(value="accountNumber") String accountNumber,
                                            @RequestParam(value="amount") long amount) {
        log.info("Withdraw amount from account request: account number is " + accountNumber
                + " and amount is " + amount);

        String withdrawResultString;
        if (atmUtil.isAmountValid(amount)) {
            if (accountRepository.checkBalanceForAccount(accountNumber).getBalance() >= amount) {
                StatusCode atmBalanceStatusCode = atmUtil.checkBalanceForATM(amount);
                if (atmBalanceStatusCode.equals(StatusCode.OK)) {
                    log.info("Amount valid, user has enough money in account, atm can process the request " +
                            "=> Amount" + amount + "will be withdrawn from the user account and from the ATM");
                    accountRepository.withdrawAmountFromAccount(accountNumber, amount);
                    log.info(amount + " withdrawn from user account");
                    withdrawResultString = atmUtil.withdrawAmount(amount).toString();
                    log.info(amount + " withdrawn from ATM in banknotes: " + withdrawResultString);
                } else {
                    withdrawResultString = atmBalanceStatusCode.getValue();
                    log.warn("ATM cannot process request: " + withdrawResultString);
                }
            } else {
                withdrawResultString = StatusCode.ACCOUNT_NOT_ENOUGH_MONEY.getValue();
                log.warn("User does not have enough money in account: " + withdrawResultString);
            }
        } else {
            withdrawResultString = StatusCode.INVALID_AMOUNT.getValue();
            log.warn("Entered invalid amount for this ATM: " + withdrawResultString);
        }
        return withdrawResultString;
    }
}
