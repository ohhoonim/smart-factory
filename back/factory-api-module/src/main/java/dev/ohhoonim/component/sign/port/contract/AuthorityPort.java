package dev.ohhoonim.component.sign.port.contract;

import java.util.List;

public interface AuthorityPort {
    List<AuthorityDto> authoritiesByUsername(String name);
}