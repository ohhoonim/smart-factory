package dev.ohhoonim.component.sign.port.contract;

import java.util.List;
import dev.ohhoonim.component.unit.Entity;
import dev.ohhoonim.component.unit.EntityId;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignUserDto implements Entity  {

    private String name;
    private String password;
    private List<AuthorityDto> authorities;

    public SignUserDto(String username, String password) {
        this(username, password, null);
    }

    public SignUserDto(String username) {
        this(username, null, null);
    }

    @Builder
    public SignUserDto(String name, String password, List<AuthorityDto> authorities) {
        this.name = name;
        this.password = password;
        this.authorities = authorities;
    }

    @Override
    public EntityId getId() {
        return null ; //new EntityId();
    }

}
