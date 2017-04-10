package org.bank.atm.service;

import org.bank.atm.model.Account;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AccountRepository extends CrudRepository<Account, Long> {

    @Query(value="SELECT a FROM Account a where a.accountNumber = ?1")
    Account checkBalanceForAccount(String accountNumber);

    @Transactional
    @Modifying
    @Query(value="UPDATE Account a SET a.balance = (a.balance - ?2) WHERE a.accountNumber = ?1")
    public void withdrawAmountFromAccount(String accountNumber, double amount);
}