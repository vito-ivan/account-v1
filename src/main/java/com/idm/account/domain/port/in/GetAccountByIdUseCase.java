package com.idm.account.domain.port.in;

import com.idm.account.domain.model.Account;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetAccountByIdUseCase {

    Mono<Account> getAccountById(UUID accountId);
}
