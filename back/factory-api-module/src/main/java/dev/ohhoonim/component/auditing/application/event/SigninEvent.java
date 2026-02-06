package dev.ohhoonim.component.auditing.application.event;

import dev.ohhoonim.component.sign.port.contract.SignUserDto;
import dev.ohhoonim.component.unit.Created;
import dev.ohhoonim.component.unit.EntityId;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public final class SigninEvent {//implements ChangedEvent<SignUserDto> {

    private SignUserDto signUser;
    private Created creator;

    public SigninEvent(SignUserDto signUser, Created creator) {
        this.signUser = signUser;
        this.creator = creator;
    }

    public String username() {
        return signUser.getName();
    }

    public EntityId getEventId() {
        return null ; //new EntityId();
    }

    public Class<SignUserDto> getEntityType() {
        return SignUserDto.class; //"sign_user";//Id.entityType(signUser.getClass()); //sign_user
    }

    public String getEntityId() {
        return null;
    }

    public String getEventType() {
        return EventType.SIGNIN.toString();
    }

    public Created getCreator() {
        return creator;
    }

    public String getJsonData() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(signUser);
        } catch (JacksonException e) {
            throw new RuntimeException("json 변환에 실패하였습니다.");
        }
    }
}
