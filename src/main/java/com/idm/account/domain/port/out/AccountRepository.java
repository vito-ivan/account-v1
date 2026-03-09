package com.idm.account.domain.port.out;

import com.idm.account.domain.model.Account;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccountRepository {

    Mono<Account> save(Account account);

    Mono<Account> update(Account account);

    Mono<Account> findById(UUID accountId);

    Mono<Void> deleteById(UUID accountId);

    Mono<Account> findByAccountNumber(String accountNumber);
}
