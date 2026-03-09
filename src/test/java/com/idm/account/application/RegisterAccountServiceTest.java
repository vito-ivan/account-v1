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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterAccountServiceTest {

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private RegisterAccountService service;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Account buildInput() {
        return Account.builder()
                .customerId("CUST-20001")
                .accountNumber("00123456789012")
                .accountType(AccountType.SAVINGS)
                .currency(CurrencyType.PEN)
                .balance(new BigDecimal("1500.00"))
                .build();
    }

    private Account buildExisting() {
        return Account.builder()
                .accountId(UUID.randomUUID())
                .customerId("CUST-20001")
                .accountNumber("00123456789012")
                .accountType(AccountType.SAVINGS)
                .currency(CurrencyType.PEN)
                .balance(new BigDecimal("2000.00"))
                .active(true)
                .build();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------


    @Test
    @DisplayName("Should emit CONFLICT when account number already exists")
    void shouldEmitConflictWhenAccountNumberAlreadyExists() {
        var input = Account.builder()
                .customerId("CUST-20001")
                .accountNumber("00123456789012")
                .accountType(AccountType.SAVINGS)
                .currency(CurrencyType.PEN)
                .balance(new BigDecimal("1500.00"))
                .build();

        var existing = Account.builder()
                .accountId(UUID.randomUUID())
                .customerId("CUST-20001")
                .accountNumber("00123456789012")
                .accountType(AccountType.SAVINGS)
                .currency(CurrencyType.PEN)
                .balance(new BigDecimal("2000.00"))
                .active(true)
                .build();

        when(repository.findByAccountNumber("00123456789012"))
                .thenReturn(Mono.just(existing));

        var result = service.create(input);

        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assert error instanceof AppException;
                    assert ((AppException) error).getCode() == AppException.Code.CONFLICT;
                })
                .verify();

        verify(repository).findByAccountNumber("00123456789012");
        verify(repository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should create account when account number does not exist")
    void shouldCreateAccountWhenAccountNumberDoesNotExist() {
        var input = Account.builder()
                .customerId("CUST-20001")
                .accountNumber("00123456789012")
                .accountType(AccountType.SAVINGS)
                .currency(CurrencyType.PEN)
                .balance(new BigDecimal("1500.00"))
                .build();

        when(repository.findByAccountNumber("00123456789012"))
                .thenReturn(Mono.empty());

        when(repository.save(any(Account.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var result = service.create(input);

        StepVerifier.create(result)
                .assertNext(created -> {
                    assert created.getAccountId() != null;
                    assert Boolean.TRUE.equals(created.getActive());
                    assert created.getCustomerId().equals("CUST-20001");
                    assert created.getAccountNumber().equals("00123456789012");
                    assert created.getAccountType() == AccountType.SAVINGS;
                    assert created.getCurrency() == CurrencyType.PEN;
                    assert created.getBalance().compareTo(new BigDecimal("1500.00")) == 0;
                })
                .expectComplete()
                .verify();

        verify(repository).findByAccountNumber("00123456789012");
        verify(repository).save(any(Account.class));
    }
}

