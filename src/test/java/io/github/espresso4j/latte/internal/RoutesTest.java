package io.github.espresso4j.latte.internal;

import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RoutesTest {

    private static final String GET = "GET";

    @Test
    public void testAddRoutes() {
        Routes<Integer> intRoutes = new Routes<>();

        intRoutes.addRoute(new Route<>("/", 1, 1));
        intRoutes.addRoute(new Route<>("/plus", 2, 2));
        intRoutes.addRoute(new Route<>("/minus", 3, 3));
        intRoutes.addRoute(new Route<>("/minus/1", 4, 4));
        intRoutes.addRoute(new Route<>("/minus/:value", 5, 5));
        intRoutes.addRoute(new Route<>("/minus/*foo", 6, 6));

        assertEquals(intRoutes.getRoot().getRoutes(GET).findFirst().get().getPayload().intValue(), 1);
        assertEquals(intRoutes.getRoot().getChildren().get("plus").getRoutes(GET).findFirst().get().getPayload().intValue(), 2);
        assertEquals(intRoutes.getRoot().getChildren().get("minus").getRoutes(GET).findFirst().get().getPayload().intValue(), 3);
        assertEquals(intRoutes.getRoot().getChildren().get("minus").getChildren().get("1").getRoutes(GET).findFirst().get().getPayload().intValue(), 4);

        Collection<Node<Integer>> wcc = intRoutes.getRoot().getChildren().get("minus").getWildCardChildren();
        assertEquals(wcc.size(), 1);
        Node<Integer> n1 = wcc.iterator().next();
        assertTrue(n1.getRoutes(GET).findFirst().isPresent());
        assertEquals(n1.getRoutes(GET).findFirst().get().getPayload().intValue(), 5);
        assertEquals(n1.getName(), ":value");

        Collection<Node<Integer>> cac = intRoutes.getRoot().getChildren().get("minus").getCatchAllChildren();
        assertEquals(cac.size(), 1);
        Node<Integer> n2 = cac.iterator().next();
        assertTrue(n2.getRoutes(GET).findFirst().isPresent());
        assertEquals(n2.getRoutes(GET).findFirst().get().getPayload().intValue(), 6);
        assertEquals(n2.getName(), "*foo");
    }

    @Test
    public void testMatchRoutes() {
        Routes<Integer> intRoutes = new Routes<>();

        intRoutes.addRoute(new Route<>("/", 1, 1));
        intRoutes.addRoute(new Route<>("/plus", 2, 2));
        intRoutes.addRoute(new Route<>("/minus", 3, 3));
        intRoutes.addRoute(new Route<>("/minus/1", 4, 4));
        intRoutes.addRoute(new Route<>("/minus/:value", 5, 5));
        intRoutes.addRoute(new Route<>("/minus/:foo/abc", 6, 6));
        intRoutes.addRoute(new Route<>("/minus/*foo", 7, 7));

        final String method = "GET";

        assertEquals(1, intRoutes.matchRoute(method, "/").get().getPayload().intValue());
        assertEquals(2, intRoutes.matchRoute(method, "/plus").get().getPayload().intValue());
        assertEquals(2, intRoutes.matchRoute(method, "/plus/").get().getPayload().intValue());
        assertEquals(3, intRoutes.matchRoute(method, "/minus").get().getPayload().intValue());
        assertEquals(4, intRoutes.matchRoute(method, "/minus/1").get().getPayload().intValue());
        assertEquals(5, intRoutes.matchRoute(method, "/minus/2").get().getPayload().intValue());
        assertEquals(6, intRoutes.matchRoute(method, "/minus/2/abc").get().getPayload().intValue());
        assertEquals(7, intRoutes.matchRoute(method, "/minus/2/3").get().getPayload().intValue());

        assertEquals(3, intRoutes.matchRoutes(method, "/minus/1").size());
        assertEquals(0, intRoutes.matchRoutes(method, "/multi/1").size());
    }

    @Test
    public void testMatchRoutesWithMethod() {
        Routes<Integer> intRoutes = new Routes<>();

        intRoutes.addRoute(new Route<>("GET", "/", 1, 1));
        intRoutes.addRoute(new Route<>("POST", "/", 2, 2));

        assertEquals(2, intRoutes.matchRoute("POST", "/").get().getPayload().intValue());
        assertTrue(intRoutes.matchRoute("DELETE", "/").isEmpty());

    }

    @Test
    public void testEmptyNode() {
        Routes<Integer> intRoutes = new Routes<>();
        intRoutes.addRoute(new Route<>("/minus/:value", 1, 1));
        final String method = "GET";

        assertEquals(0, intRoutes.matchRoutes("/minus", method).size());
        assertFalse(intRoutes.matchRoute("/minus", method).isPresent());
    }
}
