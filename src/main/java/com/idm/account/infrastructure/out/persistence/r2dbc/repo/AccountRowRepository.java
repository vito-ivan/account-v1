package com.idm.account.infrastructure.out.persistence.r2dbc.repo;

import com.idm.account.infrastructure.out.persistence.r2dbc.row.AccountRow;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccountRowRepository extends ReactiveCrudRepository<AccountRow, UUID> {

    Mono<AccountRow> findByAccountNumber(String accountNumber);
}
