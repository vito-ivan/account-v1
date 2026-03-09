package com.idm.account.application;

import com.idm.account.domain.port.out.AccountRepository;
import com.idm.account.shared.error.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteAccountUseCase {

    private final AccountRepository repository;

    public Mono<Void> delete(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(AppException.of(AppException.Code.NOT_FOUND, "Account not found")))
                .then(Mono.defer(() -> repository.deleteById(id)));
    }
}
