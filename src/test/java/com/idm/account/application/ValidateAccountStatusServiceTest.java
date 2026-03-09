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
class ValidateAccountStatusServiceTest {

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private ValidateAccountStatusService service;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Account buildAccount(UUID accountId, boolean active) {
        return Account.builder()
                .accountId(accountId)
                .customerId("customer-1")
                .accountNumber("4756123400")
                .accountType(AccountType.SAVINGS)
                .currency(CurrencyType.USD)
                .balance(BigDecimal.valueOf(1800.00))
                .active(active)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Returns account when account exists and is active")
    void returnsAccountWhenExistsAndActive() {
        var existentAccountId = UUID.randomUUID();

        var account = buildAccount(existentAccountId, true);

        when(repository.findById(existentAccountId)).thenReturn(Mono.just(account));

        var result = service.validateAccountStatus(existentAccountId);

        StepVerifier.create(result)
                .assertNext(found -> {
                    assert found.getAccountId().equals(existentAccountId);
                    assert found.getCustomerId().equals("customer-1");
                    assert found.getAccountNumber().equals("4756123400");
                    assert found.getAccountType() == AccountType.SAVINGS;
                    assert found.getCurrency() == CurrencyType.USD;
                    assert found.getBalance().compareTo(BigDecimal.valueOf(1800.00)) == 0;
                    assert Boolean.TRUE.equals(found.getActive());
                })
                .expectComplete()
                .verify();

        verify(repository).findById(existentAccountId);
    }

    @Test
    @DisplayName("Returns account and logs warning when account exists but is inactive")
    void returnsAccountWhenExistsButInactive() {
        var inactiveAccountId = UUID.randomUUID();

        var account = buildAccount(inactiveAccountId, false);

        when(repository.findById(inactiveAccountId)).thenReturn(Mono.just(account));

        var result = service.validateAccountStatus(inactiveAccountId);

        StepVerifier.create(result)
                .assertNext(found -> {
                    assert found.getAccountId().equals(inactiveAccountId);
                    assert Boolean.FALSE.equals(found.getActive());
                })
                .expectComplete()
                .verify();

        verify(repository).findById(inactiveAccountId);
    }

    @Test
    @DisplayName("Emits NOT_FOUND when account does not exist")
    void notFoundWhenAccountDoesNotExist() {
        var nonExistentAccountId = UUID.randomUUID();

        when(repository.findById(nonExistentAccountId)).thenReturn(Mono.empty());

        var result = service.validateAccountStatus(nonExistentAccountId);

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
    @DisplayName("Propagates error when repository throws an exception")
    void propagatesErrorWhenRepositoryFails() {
        var errorAccountId = UUID.randomUUID();

        var dbError = new RuntimeException("DB unavailable");

        when(repository.findById(errorAccountId)).thenReturn(Mono.error(dbError));

        var result = service.validateAccountStatus(errorAccountId);

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof RuntimeException;
                    assert ex.getMessage().equals("DB unavailable");
                })
                .verify();

        verify(repository).findById(errorAccountId);
    }
}

