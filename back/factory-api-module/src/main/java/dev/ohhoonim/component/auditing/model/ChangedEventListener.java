package dev.ohhoonim.component.auditing.model;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import dev.ohhoonim.component.auditing.application.ChangeEventActivity;
import dev.ohhoonim.component.auditing.application.event.ChangedEvent;
import dev.ohhoonim.component.auditing.application.event.CreatedEvent;
import dev.ohhoonim.component.auditing.application.event.LookupEvent;
import dev.ohhoonim.component.auditing.application.event.ModifiedEvent;
import dev.ohhoonim.component.auditing.application.event.SigninEvent;
import dev.ohhoonim.component.auditing.port.ChangedEventPort;
import dev.ohhoonim.component.unit.Entity;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChangedEventListener implements ChangeEventActivity {

    private final ChangedEventPort<?> repository;

    @Override
    @ApplicationModuleListener 
    public void changedEvent(ChangedEvent<? extends Entity> event) {
        // switch (event) {
        //     case CreatedEvent c -> repository.recordingChangedData(c);
        //     case ModifiedEvent m -> repository.recordingChangedData(m);
        //     case LookupEvent _ -> new RuntimeException("Not supported event");
        //     case SigninEvent _ -> new RuntimeException("Not supported event");
        // }
    }

    @Override
    @ApplicationModuleListener
    public void signinEvent(SigninEvent signUser) {
        repository.recordingSignin(signUser);
    }
}
