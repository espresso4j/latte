package io.github.espresso4j.latte.internal;

import java.util.*;

public class Routes<T> {

    private Node<T> root = new Node<>("");

    Node<T> getRoot() {
        return root;
    }

    /**
     *
     * @param route
     */
    public void addRoute(Route<T> route) {
        Path thePath = route.getPathSpec();

        Node<T> currentNode = root;

        for (Path.Segment seg: thePath.getSegments()) {
            Node<T> childNode = currentNode.getChildren().get(seg.name);
            if (childNode != null) {
                currentNode = childNode;
            } else {
                // create new node
                Node<T> newNode = new Node<>(seg.name);
                currentNode.getChildren().put(seg.name, newNode);
                currentNode = newNode;
            }
        }

        currentNode.setRoute(route);
    }

    /**
     *
     * @param path
     * @return
     */
    public SortedSet<Route<T>> matchRoutes(String path) {
        Path thePath = Path.from(path);
        Deque<Node<T>> matchingNodes0 = new ArrayDeque<>();
        Deque<Node<T>> matchingNodes1 = new ArrayDeque<>();

        matchingNodes0.add(root);

        SortedSet<Route<T>> results = new TreeSet<>((tRoute, t1) -> t1.getPriority() - tRoute.getPriority());

        for (Path.Segment seg: thePath.getSegments()) {
            while (! matchingNodes0.isEmpty()) {
                Node<T> matchingParent = matchingNodes0.pop();

                // static path matching
                Node<T> matchingChild = matchingParent.getChildren().get(seg.name);
                if (matchingChild != null) {
                    matchingNodes1.push(matchingChild);
                }

                // wildcard
                Collection<Node<T>> wildCardChildren = matchingParent.getWildCardChildren();
                matchingNodes1.addAll(wildCardChildren);

                // catchAll
                Collection<Node<T>> catchAllChildren = matchingParent.getCatchAllChildren();
                catchAllChildren.forEach((n) -> n.getRoute().ifPresent(results::add));
            }

            Deque<Node<T>> swap = matchingNodes0;
            matchingNodes0 = matchingNodes1;
            matchingNodes1 = swap;
        }

        matchingNodes0.forEach((n) -> n.getRoute().ifPresent(results::add));

        return results;
    }

    /**
     *
     * @param path
     * @return
     */
    public Optional<Route<T>> matchRoute(String path) {
        SortedSet<Route<T>> results = matchRoutes(path);
        if (results.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(results.first());
        }
    }

}
