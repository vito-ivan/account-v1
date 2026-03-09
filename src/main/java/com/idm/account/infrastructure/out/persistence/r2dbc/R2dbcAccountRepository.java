package com.idm.account.infrastructure.out.persistence.r2dbc;

import com.idm.account.infrastructure.out.persistence.r2dbc.repo.AccountRowRepository;
import com.idm.account.infrastructure.out.persistence.r2dbc.row.AccountRow;
import com.idm.account.infrastructure.out.persistence.r2dbc.row.AccountType;
import com.idm.account.infrastructure.out.persistence.r2dbc.row.CurrencyType;
import com.idm.account.domain.model.Account;
import com.idm.account.domain.port.out.AccountRepository;
import com.idm.account.shared.error.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class R2dbcAccountRepository implements AccountRepository {

    private final AccountRowRepository repository;

    @Transactional
    @Override
    public Mono<Account> save(Account account) {

        AccountRow cr = toRow(account, true);

        return repository.save(cr)
                .doOnError(th -> log.error("[DB] Error saving AccountRow. accountId={}",
                        account.getAccountId(), th))
                .onErrorMap(th -> AppException.of(AppException.Code.DB_ERROR,
                        "Database error while saving account", th))
                .map(this::toDomain);
    }

    @Override
    public Mono<Account> update(Account account) {
        AccountRow cr = toRow(account, false);
        return repository.save(cr)
                .doOnError(th -> log.error("[DB] Error updating AccountRow. accountId={}",
                        account.getAccountId(), th))
                .onErrorMap(th -> AppException.of(AppException.Code.DB_ERROR,
                        "Database error while updating account", th))
                .map(this::toDomain);
    }


    @Override
    public Mono<Account> findById(UUID id) {
        return repository.findById(id)
                .doOnError(th ->
                        log.error("[DB] Error fetching AccountRow by id. accountId={}", id, th))
                .onErrorMap(th -> AppException.of(AppException.Code.DB_ERROR,
                        "Database error while fetching account by id", th))
                .map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id)
                .doOnError(th ->
                        log.error("[DB] Error deleting AccountRow by id. accountId={}", id, th))
                .onErrorMap(th -> AppException.of(AppException.Code.DB_ERROR,
                        "Database error while deleting account by id", th));
    }

    @Override
    public Mono<Account> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber)
                .doOnError(th ->
                        log.error("[DB] Error fetching AccountRow by account number. accountNumber={}",
                                accountNumber, th))
                .onErrorMap(th -> AppException.of(AppException.Code.DB_ERROR,
                        "Database error while fetching account by account number", th))
                .map(this::toDomain);
    }

    private Account toDomain(AccountRow accountRow) {

        return Account.builder()
                .accountId(accountRow.getAccountId())
                .customerId(accountRow.getCustomerId())
                .accountNumber(accountRow.getAccountNumber())
                .accountType(com.idm.account.domain.model.AccountType.valueOf(accountRow.getAccountType().name()))
                .currency(com.idm.account.domain.model.CurrencyType.valueOf(accountRow.getCurrency().name()))
                .balance(accountRow.getBalance())
                .active(accountRow.getActive())
                .createdAt(accountRow.getCreatedAt())
                .build();
    }

    private AccountRow toRow(Account account, boolean isNew) {

        return AccountRow.builder()
                .accountId(account.getAccountId())
                .customerId(account.getCustomerId())
                .accountNumber(account.getAccountNumber())
                .accountType(AccountType.valueOf(account.getAccountType().name()))
                .currency(CurrencyType.valueOf(account.getCurrency().name()))
                .balance(account.getBalance())
                .active(account.getActive())
                .createdAt(LocalDateTime.now())
                .newRecord(isNew)
                .build();
    }
}
