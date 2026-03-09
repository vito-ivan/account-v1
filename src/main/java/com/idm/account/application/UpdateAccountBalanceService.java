package com.idm.account.application;

import com.idm.account.domain.model.Account;
import com.idm.account.domain.port.in.UpdateAccountBalanceUseCase;
import com.idm.account.domain.port.out.AccountRepository;
import com.idm.account.shared.error.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateAccountBalanceService implements UpdateAccountBalanceUseCase {

    private final AccountRepository repository;

    @Override
    public Mono<Account> updateAccountBalance(UUID accountId, BigDecimal balance) {
        return repository.findById(accountId)
                .switchIfEmpty(Mono.error(AppException.of(AppException.Code.NOT_FOUND, "Account not found")))
                .flatMap(account -> {
                    account.setBalance(balance);
                    return repository.update(account);
                });
    }
}
