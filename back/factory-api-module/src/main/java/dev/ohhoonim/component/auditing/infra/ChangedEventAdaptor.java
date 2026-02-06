package dev.ohhoonim.component.auditing.infra;

import java.util.List;
import org.springframework.stereotype.Component;
import dev.ohhoonim.component.auditing.application.event.ChangedEvent;
import dev.ohhoonim.component.auditing.application.event.LookupEvent;
import dev.ohhoonim.component.auditing.application.event.SigninEvent;
import dev.ohhoonim.component.auditing.port.ChangedEventPort;

@Component
public class ChangedEventAdaptor implements ChangedEventPort{

    @Override
    public void recordingChangedData(ChangedEvent event) {
        
    }

    @Override
    public List lookupEvent(LookupEvent lookup) {
        return List.of();
    }

    @Override
    public void recordingSignin(SigninEvent signUser) {
    }
    
}
