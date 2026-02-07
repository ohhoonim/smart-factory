package dev.ohhoonim.system.attachFile.model;

import java.time.Instant;

public interface AttachFilePolicy {
   void verifyAddition(int currentCount);
    
    void verifyExtension(String extension);

    boolean isExpired(Instant modifiedAt);

    Instant getUnlinkedThreshold(); 
}
