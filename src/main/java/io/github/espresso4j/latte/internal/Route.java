package io.github.espresso4j.latte.internal;

public class Route<T> {

    public static final String ANY_METHOD = "any";

    private Path pathSpec;

    private T payload;

    private int priority;

    private String method;

    public Route(String path, T payload, int priority) {
        this(ANY_METHOD, path, payload, priority);
    }

    public Route(String method, String path, T payload, int priority) {
        this.method = method;
        this.pathSpec = Path.from(path);
        this.payload = payload;
        this.priority = priority;
    }

    public String getMethod() {
        return method;
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

    public boolean methodMatches (String method) {
        if (ANY_METHOD.equals(this.method)) {
            return true;
        }

        return this.method.equals(method);
    }
}
