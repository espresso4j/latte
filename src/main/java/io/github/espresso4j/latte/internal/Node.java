package io.github.espresso4j.latte.internal;

import java.util.*;

public class Node<T> {

    private TreeMap<String, Node<T>> children = new TreeMap<>();

    private Route<T> route;

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

    public Optional<Route<T>> getRoute() {
        return Optional.of(route);
    }

    public void setRoute(Route<T> route) {
        this.route = route;
    }

    public String getName() {
        return name;
    }
}
