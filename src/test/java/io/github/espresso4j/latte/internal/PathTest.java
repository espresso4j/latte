package io.github.espresso4j.latte.internal;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PathTest {

    @Test
    public void testMatchSpec() {
        Map<String, String> captures = Path.from("/abc/:hello").matchSpec("/abc/123");
        assertEquals("123", captures.get("hello"));

        Map<String, String> captures2 = Path.from("/abc/:hello/:world").matchSpec("/abc/123/234");
        assertEquals("123", captures2.get("hello"));
        assertEquals("234", captures2.get("world"));

        Map<String, String> captures3 = Path.from("/:hello/*world").matchSpec("/abc/123/234/345");
        assertEquals("abc", captures3.get("hello"));
        assertEquals("123/234/345", captures3.get("world"));

        Map<String, String> captures4 = Path.from("/abc/*world").matchSpec("/abc/hello/");
        assertEquals("hello/", captures4.get("world"));

        assertTrue(Path.from("/abc/bcd").matchSpec("/abc/bcd").isEmpty());
    }

}
