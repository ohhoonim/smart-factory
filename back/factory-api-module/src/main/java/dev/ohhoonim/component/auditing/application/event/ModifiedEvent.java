package dev.ohhoonim.component.auditing.application.event;

import dev.ohhoonim.component.auditing.model.ChangedField;
import dev.ohhoonim.component.unit.Created;
import dev.ohhoonim.component.unit.Entity;
import dev.ohhoonim.component.unit.EntityId;
import tools.jackson.databind.ObjectMapper;

public final class ModifiedEvent<T extends Entity> { //implements ChangedEvent<T>{

    private T oldRecord;
    private T newRecord;
    private Created creator;

    public ModifiedEvent(T oldRecord, T newRecord, Created creator) {
        this.oldRecord = oldRecord;
        this.newRecord = newRecord;
        this.creator = creator;
    }

    // @Override
    // public EntityId getEventId() {
    //    return newRecord.getId(); 
    // }

    // @Override
    // public String getEntityType() {
    //     return newRecord.getClass().getName(); 
    // }

    // @Override
    // public String getEntityId() {
    //     return newRecord.getId().toString();
    // }

    // @Override
    // public String getEventType() {
    //     return EventType.MODIFIED.toString();
    // }

    // @Override
    // public Created getCreator() {
    //     return creator;
    // }

    // @Override
    // public String getJsonData() {
    //     ObjectMapper objectMapper = new ObjectMapper();
    //     var changedField = new ChangedField(objectMapper);
    //     return changedField.apply(oldRecord, newRecord);
    // }
    
}
