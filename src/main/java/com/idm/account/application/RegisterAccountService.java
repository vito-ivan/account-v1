package com.idm.account.application;

import com.idm.account.domain.model.Account;
import com.idm.account.domain.port.in.RegisterAccountUseCase;
import com.idm.account.domain.port.out.AccountRepository;
import com.idm.account.shared.error.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterAccountService implements RegisterAccountUseCase {

    private final AccountRepository repository;

    public Mono<Account> create(Account account) {

        return repository.findByAccountNumber(account.getAccountNumber())
                .flatMap(_ -> Mono.<Account>error(AppException.of(AppException.Code.CONFLICT,
                        "Customer with identification already exists")))
                .switchIfEmpty(Mono.defer(() -> {

                    account.setAccountId(UUID.randomUUID());
                    account.setActive(true);

                    return repository.save(account);
                }));
    }

}
