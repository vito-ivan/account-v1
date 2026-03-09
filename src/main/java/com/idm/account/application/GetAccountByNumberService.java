package com.idm.account.application;

import com.idm.account.domain.model.Account;
import com.idm.account.domain.port.in.GetAccountByNumberUseCase;
import com.idm.account.domain.port.out.AccountRepository;
import com.idm.account.shared.error.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetAccountByNumberService implements GetAccountByNumberUseCase {

    private final AccountRepository repository;


    @Override
    public Mono<Account> getAccountByNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber)
                .switchIfEmpty(Mono.error(AppException.of(AppException.Code.NOT_FOUND, "Account not found")));
    }
}
