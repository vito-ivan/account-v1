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
class DeleteAccountUseCaseTest {

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private DeleteAccountUseCase useCase;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Account buildExistingAccount(UUID id) {
        return Account.builder()
                .accountId(id)
                .customerId("customer-1")
                .accountNumber("00123456789012")
                .accountType(AccountType.SAVINGS)
                .currency(CurrencyType.USD)
                .balance(BigDecimal.valueOf(500.00))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Deletes account successfully when account exists")
    void deletesAccountWhenExists() {
        var existentAccountId = UUID.randomUUID();

        var existingAccount = buildExistingAccount(existentAccountId);

        when(repository.findById(existentAccountId)).thenReturn(Mono.just(existingAccount));
        when(repository.deleteById(existentAccountId)).thenReturn(Mono.empty());

        var result = useCase.delete(existentAccountId);

        StepVerifier.create(result)
                .expectComplete()
                .verify();

        verify(repository).findById(existentAccountId);
        verify(repository).deleteById(existentAccountId);
    }

    @Test
    @DisplayName("Emits NOT_FOUND when account does not exist")
    void notFoundWhenAccountDoesNotExist() {
        var nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Mono.empty());

        var result = useCase.delete(nonExistentId);

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof AppException;
                    assert ((AppException) ex).getCode() == AppException.Code.NOT_FOUND;
                })
                .verify();

        verify(repository).findById(nonExistentId);
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Propagates error when repository.deleteById fails")
    void propagatesErrorWhenDeleteFails() {
        var existentId = UUID.randomUUID();
        var existingAccount = buildExistingAccount(existentId);
        var dbError = new RuntimeException("DB connection lost");

        when(repository.findById(existentId)).thenReturn(Mono.just(existingAccount));
        when(repository.deleteById(existentId)).thenReturn(Mono.error(dbError));

        var result = useCase.delete(existentId);

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof RuntimeException;
                    assert ex.getMessage().equals("DB connection lost");
                })
                .verify();

        verify(repository).findById(existentId);
        verify(repository).deleteById(existentId);
    }
}

