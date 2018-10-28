package io.github.espresso4j.latte.internal;

public class Route<T> {

    private Path pathSpec;

    private T payload;

    private int priority;

    public Route(String path, T payload, int priority) {
        this.pathSpec = Path.from(path);
        this.payload = payload;
        this.priority = priority;
    }

    public Path getPathSpec() {
        return pathSpec;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
