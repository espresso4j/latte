package io.github.espresso4j.latte.internal;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Node<T> {

    private TreeMap<String, Node<T>> children = new TreeMap<>();

    private Map<String, Route<T>> methodToRoutes = new HashMap<>(7);

    private String name;

    public Node(String name) {
        this.name = name;
    }

    public Collection<Node<T>> getWildCardChildren() {
        return children.subMap(":", true, ";", false).values();
    }

    public Collection<Node<T>> getCatchAllChildren() {
        return children.subMap("*", true, "+", false).values();
    }

    public TreeMap<String, Node<T>> getChildren() {
        return children;
    }

    public Map<String, Route<T>> getMethodToRoutes() {
        return methodToRoutes;
    }

    public String getName() {
        return name;
    }

    public Stream<Route<T>> getRoutes(String method) {
        Route<T> wildMethodRoute = methodToRoutes.get(Route.ANY_METHOD);
        Route<T> methodRoute = methodToRoutes.get(method);

        return Stream.of(wildMethodRoute, methodRoute).filter(Objects::nonNull);
    }
}
