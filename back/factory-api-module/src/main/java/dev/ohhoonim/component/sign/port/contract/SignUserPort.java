package dev.ohhoonim.component.sign.port.contract;

import java.util.Optional;

public interface SignUserPort {

    Optional<SignUserDto> findByUsernamePassword(String name, String password);
}