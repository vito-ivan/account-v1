package com.idm.account.application;

import com.idm.account.domain.model.Account;
import com.idm.account.domain.model.AccountType;
import com.idm.account.domain.model.CurrencyType;
import com.idm.account.domain.port.out.AccountRepository;
import com.idm.account.shared.error.AppException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAccountBalanceServiceTest {

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private UpdateAccountBalanceService service;

    private static final UUID accountId = UUID.randomUUID();

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Account buildAccount() {
        return Account.builder()
                .accountId(accountId)
                .customerId("customer-1")
                .accountNumber("4756123400")
                .accountType(AccountType.SAVINGS)
                .currency(CurrencyType.USD)
                .balance(BigDecimal.valueOf(3000.00))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Updates balance when account exists")
    void updatesBalanceWhenAccountExists() {
        var account = buildAccount();
        var newBalance = BigDecimal.valueOf(5000.00);
        var updated = buildAccount();
        updated.setBalance(newBalance);

        when(repository.findById(accountId)).thenReturn(Mono.just(account));
        when(repository.update(any(Account.class))).thenReturn(Mono.just(updated));

        var result = service.updateAccountBalance(accountId, newBalance);

        StepVerifier.create(result)
                .assertNext(found -> {
                    assert found.getAccountId().equals(accountId);
                    assert found.getCustomerId().equals("customer-1");
                    assert found.getAccountNumber().equals("4756123400");
                    assert found.getBalance().compareTo(newBalance) == 0;
                    assert Boolean.TRUE.equals(found.getActive());
                })
                .expectComplete()
                .verify();

        verify(repository).findById(accountId);
        verify(repository).update(any(Account.class));
    }

    @Test
    @DisplayName("Emits NOT_FOUND when account does not exist")
    void notFoundWhenAccountDoesNotExist() {
        when(repository.findById(accountId)).thenReturn(Mono.empty());

        var result = service.updateAccountBalance(accountId, BigDecimal.valueOf(5000.00));

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof AppException;
                    assert ((AppException) ex).getCode() == AppException.Code.NOT_FOUND;
                    assert ex.getMessage().equals("Account not found");
                })
                .verify();

        verify(repository).findById(accountId);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Propagates error when repository findById throws an exception")
    void propagatesErrorWhenFindByIdFails() {
        var dbError = new RuntimeException("DB unavailable");

        when(repository.findById(accountId)).thenReturn(Mono.error(dbError));

        var result = service.updateAccountBalance(accountId, BigDecimal.valueOf(5000.00));

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof RuntimeException;
                    assert ex.getMessage().equals("DB unavailable");
                })
                .verify();

        verify(repository).findById(accountId);
        verify(repository, never()).update(any());
    }

    @Test
    @DisplayName("Propagates error when repository save throws an exception")
    void propagatesErrorWhenSaveFails() {
        var account = buildAccount();
        var saveError = new RuntimeException("Save failed");

        when(repository.findById(accountId)).thenReturn(Mono.just(account));
        when(repository.update(any(Account.class))).thenReturn(Mono.error(saveError));

        var result = service.updateAccountBalance(accountId, BigDecimal.valueOf(5000.00));

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof RuntimeException;
                    assert ex.getMessage().equals("Save failed");
                })
                .verify();

        verify(repository).findById(accountId);
        verify(repository).update(any(Account.class));
    }

    @Test
    @DisplayName("Updates balance to zero successfully")
    void updatesBalanceToZeroSuccessfully() {
        var account = buildAccount();
        var updated = buildAccount();
        updated.setBalance(BigDecimal.ZERO);

        when(repository.findById(accountId)).thenReturn(Mono.just(account));
        when(repository.update(any(Account.class))).thenReturn(Mono.just(updated));

        var result = service.updateAccountBalance(accountId, BigDecimal.ZERO);

        StepVerifier.create(result)
                .assertNext(found -> {
                    assert found.getBalance().compareTo(BigDecimal.ZERO) == 0;
                })
                .expectComplete()
                .verify();

        verify(repository).findById(accountId);
        verify(repository).update(any(Account.class));
    }

    @Test
    @DisplayName("Balance is correctly set on account before saving")
    void balanceIsCorrectlySetOnAccountBeforeSaving() {
        var account = buildAccount();
        var newBalance = BigDecimal.valueOf(9999.99);

        when(repository.findById(accountId)).thenReturn(Mono.just(account));
        when(repository.update(any(Account.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        var result = service.updateAccountBalance(accountId, newBalance);

        StepVerifier.create(result)
                .assertNext(saved -> {
                    assert saved.getBalance().compareTo(newBalance) == 0;
                    assert saved.getAccountId().equals(accountId);
                    assert saved.getAccountType() == AccountType.SAVINGS;
                    assert saved.getCurrency() == CurrencyType.USD;
                })
                .expectComplete()
                .verify();

        verify(repository).findById(accountId);
        verify(repository).update(any(Account.class));
    }
}

