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
class GetAccountByNumberServiceTest {

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private GetAccountByNumberService service;

    private static final UUID accountId = UUID.randomUUID();

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Account buildAccount(String accountNumber) {
        return Account.builder()
                .accountId(accountId)
                .customerId("customer-1")
                .accountNumber(accountNumber)
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
    @DisplayName("Returns account when account number exists")
    void returnsAccountWhenAccountNumberExists() {
        var account = buildAccount("4756123400");

        when(repository.findByAccountNumber("4756123400")).thenReturn(Mono.just(account));

        var result = service.getAccountByNumber("4756123400");

        StepVerifier.create(result)
                .assertNext(found -> {
                    assert found.getAccountId().equals(accountId);
                    assert found.getCustomerId().equals("customer-1");
                    assert found.getAccountNumber().equals("4756123400");
                    assert found.getAccountType() == AccountType.SAVINGS;
                    assert found.getCurrency() == CurrencyType.USD;
                    assert found.getBalance().compareTo(BigDecimal.valueOf(3000.00)) == 0;
                    assert Boolean.TRUE.equals(found.getActive());
                })
                .expectComplete()
                .verify();

        verify(repository).findByAccountNumber("4756123400");
    }

    @Test
    @DisplayName("Emits NOT_FOUND when account number does not exist")
    void notFoundWhenAccountNumberDoesNotExist() {
        when(repository.findByAccountNumber("0000000000")).thenReturn(Mono.empty());

        var result = service.getAccountByNumber("0000000000");

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof AppException;
                    assert ((AppException) ex).getCode() == AppException.Code.NOT_FOUND;
                    assert ex.getMessage().equals("Account not found");
                })
                .verify();

        verify(repository).findByAccountNumber("0000000000");
    }

    @Test
    @DisplayName("Propagates error when repository throws an exception")
    void propagatesErrorWhenRepositoryFails() {
        var dbError = new RuntimeException("DB unavailable");

        when(repository.findByAccountNumber("4756123400")).thenReturn(Mono.error(dbError));

        var result = service.getAccountByNumber("4756123400");

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof RuntimeException;
                    assert ex.getMessage().equals("DB unavailable");
                })
                .verify();

        verify(repository).findByAccountNumber("4756123400");
    }

    @Test
    @DisplayName("Returns account with inactive status when account is inactive")
    void returnsInactiveAccount() {
        var account = buildAccount("4756123400");
        account.setActive(false);

        when(repository.findByAccountNumber("4756123400")).thenReturn(Mono.just(account));

        var result = service.getAccountByNumber("4756123400");

        StepVerifier.create(result)
                .assertNext(found -> {
                    assert found.getAccountNumber().equals("4756123400");
                    assert Boolean.FALSE.equals(found.getActive());
                })
                .expectComplete()
                .verify();

        verify(repository).findByAccountNumber("4756123400");
    }
}

