package dev.ohhoonim.component.auditing.application.event;

import dev.ohhoonim.component.unit.Created;
import dev.ohhoonim.component.unit.EntityId;

public interface ChangedEvent <T>{
       // permits CreatedEvent, ModifiedEvent{
    
    public EntityId getEventId();
    public Class<T> getEntityType(); 
    public EntityId getEntityId();
    public EventType getEventType() ; 
    public Created getCreator();
    public String getJsonData();
}
