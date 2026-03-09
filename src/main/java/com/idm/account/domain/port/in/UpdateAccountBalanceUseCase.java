package com.idm.account.domain.port.in;

import com.idm.account.domain.model.Account;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateAccountBalanceUseCase {

    Mono<Account> updateAccountBalance(UUID accountId, BigDecimal balance);
}
