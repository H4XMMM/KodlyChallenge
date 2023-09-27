package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.EmailNotificationService;
import com.dws.challenge.service.NotificationService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    private final NotificationService notificationService;

    @Autowired
    public AccountsRepositoryInMemory(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void transferFunds(BigDecimal amount, String accountIdFrom, String accountIdTo) throws DuplicateAccountIdException {
        Account accountFrom = accounts.get(accountIdFrom);
        if (accountFrom == null) {
            throw new AccountNotFoundException("Account id " + accountIdFrom + " does not exist!");
        }
        Account accountTo = accounts.get(accountIdTo);
        if (accountTo == null) {
            throw new AccountNotFoundException("Account id " + accountIdTo + " does not exist!");
        }

        BigDecimal balanceFrom = accountFrom.getBalance();
        BigDecimal balanceTo = accountTo.getBalance();
        if (balanceFrom.compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Account id " + accountFrom.getAccountId() + " does not have sufficient funds!");
        }
        else {
            String transferDescriptionFrom = "You transferred " + amount + " to account id " + accountIdTo + ".";
            String transferDescriptionTo = "You received " + amount + " from account id " + accountIdFrom + ".";
            accountFrom.setBalance(balanceFrom.subtract(amount));
            accountTo.setBalance(balanceTo.add(amount));
            accounts.put(accountTo.getAccountId(), accountTo);
            accounts.put(accountFrom.getAccountId(), accountFrom);
            notificationService.notifyAboutTransfer(accountFrom, transferDescriptionFrom);
            notificationService.notifyAboutTransfer(accountTo, transferDescriptionTo);
        }
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

}
