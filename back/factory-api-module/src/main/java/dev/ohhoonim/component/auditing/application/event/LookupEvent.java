package dev.ohhoonim.component.auditing.application.event;

import org.apache.poi.ss.formula.functions.T;
import dev.ohhoonim.component.unit.Created;
import dev.ohhoonim.component.unit.Entity;
import dev.ohhoonim.component.unit.EntityId;
import tools.jackson.databind.ObjectMapper;

public final class LookupEvent<T extends Entity> {// implements ChangedEvent<T> {

    // private EntityId id;
    // private String entityType;
    // private String entityId;
    // private String eventType;
    // private Created creator;
    // private String jsonData;

    // public LookupEvent() {
    // }

    // public LookupEvent(EntityId entityId, Class<T> entityType){
    //     this.entityId = entityId.toString();
    //     this.entityType = entityType.getName();
    // }

    // public LookupEvent(EntityId id, String entityType, String entityId, String eventType, Created creator, String jsonData) {
    //     this.id = id;
    //     this.entityType = entityType;
    //     this.entityId = entityId;
    //     this.eventType = eventType;
    //     this.creator = creator;
    //     this.jsonData = jsonData;
    // }

    // @Override
    // public EntityId getEventId() {
    //     return id;
    // }

    // @Override
    // public String getEntityType() {
    //     return entityType;
    // }

    // @Override
    // public String getEntityId() {
    //     return entityId;
    // }

    // @Override
    // public String getEventType() {
    //     return eventType;
    // }

    // @Override
    // public Created getCreator() {
    //     return creator;
    // }

    // @Override
    // public String getJsonData() {
    //     return jsonData;
    // }

    // public void setId(EntityId id) {
    //     this.id = id;
    // }

    // public void setEntityType(String entityType) {
    //     this.entityType = entityType;
    // }

    // public void setEntityId(String entityId) {
    //     this.entityId = entityId;
    // }

    // public void setEventType(String eventType) {
    //     this.eventType = eventType;
    // }

    // public void setCreator(Created creator) {
    //     this.creator = creator;
    // }

    // public void setJsonData(String jsonData) {
    //     this.jsonData = jsonData;
    // }

    // public T getEntity() {
    //     var objectMapper = new ObjectMapper();
    //     Class<T> targetClass ;
    //     try {
    //         targetClass = (Class<T>)Class.forName(this.entityType);
    //     } catch (ClassNotFoundException e) {
    //         throw new RuntimeException("entity 클래스를 찾을 수 없습니다.");
    //     }

    //     String cleanJson = this.jsonData;
    //     if (this.jsonData.startsWith("\"")) {
    //         cleanJson = objectMapper.readValue(this.jsonData, String.class);
    //     }
    //     return objectMapper.readValue(cleanJson, targetClass);
    // }

}
