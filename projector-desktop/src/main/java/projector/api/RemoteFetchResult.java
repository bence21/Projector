package projector.api;

public final class RemoteFetchResult<T> {

    private final T data;
    private final RemoteFetchFailureKind failureKind;

    private RemoteFetchResult(T data, RemoteFetchFailureKind failureKind) {
        this.data = data;
        this.failureKind = failureKind;
    }

    public static <T> RemoteFetchResult<T> success(T data) {
        return new RemoteFetchResult<>(data, null);
    }

    public static <T> RemoteFetchResult<T> failure(RemoteFetchFailureKind kind) {
        return new RemoteFetchResult<>(null, kind);
    }

    public boolean isSuccess() {
        return failureKind == null;
    }

    public T getData() {
        return data;
    }

    public T getDataOrNull() {
        return data;
    }

    public RemoteFetchFailureKind getFailureKind() {
        return failureKind;
    }
}
