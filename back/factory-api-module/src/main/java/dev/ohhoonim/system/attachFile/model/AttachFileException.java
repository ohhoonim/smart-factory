package dev.ohhoonim.system.attachFile.model;

public class AttachFileException extends RuntimeException {
    public AttachFileException(String message) {
        super(message);
    }

    public AttachFileException(String message, Throwable e) {
        super(message, e);
    }
}
