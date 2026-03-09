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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAccountBalanceServiceTest {

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private GetAccountBalanceService service;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Account buildAccount(UUID accountId) {
        return Account.builder()
                .accountId(accountId)
                .customerId("customer-1")
                .accountNumber("4756123400")
                .accountType(AccountType.SAVINGS)
                .currency(CurrencyType.USD)
                .balance(BigDecimal.valueOf(2500.75))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Returns account with balance when account exists")
    void returnsAccountWhenExists() {
        var existentAccountId = UUID.randomUUID();
        var account = buildAccount(existentAccountId);

        when(repository.findById(existentAccountId)).thenReturn(Mono.just(account));

        var result = service.getAccountBalance(existentAccountId);

        StepVerifier.create(result)
                .assertNext(found -> {
                    assert found.getAccountId().equals(existentAccountId);
                    assert found.getBalance().compareTo(BigDecimal.valueOf(2500.75)) == 0;
                    assert found.getAccountNumber().equals("4756123400");
                    assert found.getAccountType() == AccountType.SAVINGS;
                    assert found.getCurrency() == CurrencyType.USD;
                    assert Boolean.TRUE.equals(found.getActive());
                })
                .expectComplete()
                .verify();

        verify(repository).findById(existentAccountId);
    }

    @Test
    @DisplayName("Emits NOT_FOUND when account does not exist")
    void notFoundWhenAccountDoesNotExist() {
        var nonExistentAccountId = UUID.randomUUID();

        when(repository.findById(nonExistentAccountId)).thenReturn(Mono.empty());

        var result = service.getAccountBalance(nonExistentAccountId);

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof AppException;
                    assert ((AppException) ex).getCode() == AppException.Code.NOT_FOUND;
                    assert ex.getMessage().equals("Account not found");
                })
                .verify();

        verify(repository).findById(nonExistentAccountId);
    }

    @Test
    @DisplayName("Returns account with zero balance when balance is zero")
    void returnsAccountWithZeroBalance() {
        var accountId = UUID.randomUUID();

        var account = buildAccount(accountId);
        account.setBalance(BigDecimal.ZERO);

        when(repository.findById(accountId)).thenReturn(Mono.just(account));

        var result = service.getAccountBalance(accountId);

        StepVerifier.create(result)
                .assertNext(found -> {
                    assert found.getAccountId().equals(accountId);
                    assert found.getBalance().compareTo(BigDecimal.ZERO) == 0;
                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Propagates error when repository throws an exception")
    void propagatesErrorWhenRepositoryFails() {
        var accountId = UUID.randomUUID();

        var dbError = new RuntimeException("DB unavailable");

        when(repository.findById(accountId)).thenReturn(Mono.error(dbError));

        var result = service.getAccountBalance(accountId);

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof RuntimeException;
                    assert ex.getMessage().equals("DB unavailable");
                })
                .verify();

        verify(repository).findById(accountId);
    }
}

