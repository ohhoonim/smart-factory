package dev.ohhoonim.component.auditing.application;

import dev.ohhoonim.component.auditing.application.event.ChangedEvent;
import dev.ohhoonim.component.auditing.application.event.SigninEvent;
import dev.ohhoonim.component.unit.Entity;

public interface ChangeEventActivity {
    public void changedEvent(ChangedEvent<? extends Entity> event);

    public void signinEvent(SigninEvent signUser);
}
