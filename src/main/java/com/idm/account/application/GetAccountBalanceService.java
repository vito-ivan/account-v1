package com.idm.account.application;

import com.idm.account.domain.model.Account;
import com.idm.account.domain.port.in.GetAccountBalanceUseCase;
import com.idm.account.domain.port.out.AccountRepository;
import com.idm.account.shared.error.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAccountBalanceService implements GetAccountBalanceUseCase {

    private final AccountRepository repository;

    @Override
    public Mono<Account> getAccountBalance(UUID accountId) {
        return repository.findById(accountId)
                .switchIfEmpty(Mono.error(AppException.of(AppException.Code.NOT_FOUND, "Account not found")));
    }
}
