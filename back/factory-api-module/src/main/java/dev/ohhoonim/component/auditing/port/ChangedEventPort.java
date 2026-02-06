package dev.ohhoonim.component.auditing.port;

import java.util.List;
import org.apache.poi.ss.formula.functions.T;
import dev.ohhoonim.component.auditing.application.event.ChangedEvent;
import dev.ohhoonim.component.auditing.application.event.LookupEvent;
import dev.ohhoonim.component.auditing.application.event.SigninEvent;
import dev.ohhoonim.component.unit.Entity;

public interface ChangedEventPort <T extends Entity> {

    public void recordingChangedData(ChangedEvent<T> event);

    public List<LookupEvent> lookupEvent(LookupEvent lookup);

    public void recordingSignin(SigninEvent signUser);
}
