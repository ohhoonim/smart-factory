package dev.ohhoonim.component.sign.port.contract;

public record AuthorityDto(String authority) {

    public String getAuthority() {
        return this.authority;
    }
}
