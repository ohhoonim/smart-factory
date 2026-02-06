package dev.ohhoonim.component.payload;

public sealed interface Response extends Payload {

    public record Success<T>(
        ResponseCode code,
        T data
    ) implements Response { }

    public record Fail<T> (
        ResponseCode code,
        String message,
        T data
    ) implements Response { }
}
