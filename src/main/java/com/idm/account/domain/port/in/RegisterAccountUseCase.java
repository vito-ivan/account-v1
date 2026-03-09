package com.idm.account.domain.port.in;

import com.idm.account.domain.model.Account;
import reactor.core.publisher.Mono;

public interface RegisterAccountUseCase {

    Mono<Account> create(Account account);
}
