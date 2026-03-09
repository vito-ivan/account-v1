package com.idm.account.application;

import com.idm.account.domain.model.Account;
import com.idm.account.domain.port.in.ValidateAccountStatusUseCase;
import com.idm.account.domain.port.out.AccountRepository;
import com.idm.account.shared.error.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateAccountStatusService implements ValidateAccountStatusUseCase {

    private final AccountRepository repository;


    @Override
    public Mono<Account> validateAccountStatus(UUID accountId) {
        return repository.findById(accountId)
                .switchIfEmpty(Mono.error(AppException.of(AppException.Code.NOT_FOUND, "Account not found")))
                .flatMap(account -> {
                    if (!account.getActive()) {
                        log.warn("account is not active");
                    }
                    return Mono.just(account);
                });
    }
}