package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  void transferFunds() {
    Account accountFrom = new Account("1");
    accountFrom.setBalance(new BigDecimal(1000));
    Account accountTo = new Account("2");
    accountTo.setBalance(new BigDecimal(200));
    BigDecimal amount = BigDecimal.valueOf(300);
    this.accountsService.createAccount(accountFrom);
    this.accountsService.createAccount(accountTo);

    this.accountsService.transferFunds(amount, "1", "2");

    assertThat(accountFrom.getBalance()).isEqualTo(BigDecimal.valueOf(700));
    assertThat(accountTo.getBalance()).isEqualTo(BigDecimal.valueOf(500));
  }

  @Test
  void transferFundsAccountNotFoundException() {
    Account account = new Account("3");
    account.setBalance(new BigDecimal(1000));
    BigDecimal amount = BigDecimal.valueOf(300);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.transferFunds(amount, "DoesNotExist", "3");
      fail("Should have failed when transferring from an account that doesn't exist");
    } catch (AccountNotFoundException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id DoesNotExist does not exist!");
    }

    try {
      this.accountsService.transferFunds(amount, "3", "DoesNotExist");
      fail("Should have failed when transferring to an account that doesn't exist");
    } catch (AccountNotFoundException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id DoesNotExist does not exist!");
    }
  }

  @Test
  void transferFundsInsufficientBalanceException() {
    Account accountFrom = new Account("4");
    accountFrom.setBalance(new BigDecimal(10));
    Account accountTo = new Account("5");
    BigDecimal amount = BigDecimal.valueOf(20);
    this.accountsService.createAccount(accountFrom);
    this.accountsService.createAccount(accountTo);

    try {
      this.accountsService.transferFunds(amount, "4", "5");
      fail("Should have failed when transferring higher value than existing balance");
    } catch (InsufficientFundsException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + accountFrom.getAccountId() + " does not have sufficient funds!");
    }
  }
}
