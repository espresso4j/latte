package io.github.espresso4j.latte;

import io.github.espresso4j.espresso.Espresso;
import io.github.espresso4j.espresso.Request;
import io.github.espresso4j.espresso.Response;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LatteTest {

    private static Request requestOnUri(String uri) {
        Request request = new Request();
        request.setRequestMethod(Request.Method.GET);
        request.setUri(uri);

        return request;
    }

    @Test
    public void testLatte() throws Exception {
        Espresso latte = Latte.by(Espresso.class)
                .on("/", (req) -> Response.of(200).body("root"))
                .on("/sub", (req) -> Response.of(200).body("/sub"))
                .on("/sub/:foo", (req) -> Response.of(200).body("/sub/:foo"))
                // duplicated with a lower priority
                .on("/sub/:bar", (req) -> Response.of(200).body("/sub/:bar"))
                .on("/sub/*foo", (req) -> Response.of(200).body("/sub/*foo"))
                .intoEspresso();

        assertEquals("root", latte.call(requestOnUri("/")).body().get());
        assertEquals("/sub", latte.call(requestOnUri("/sub")).body().get());
        assertEquals("/sub/:foo", latte.call(requestOnUri("/sub/1")).body().get());
        assertEquals("/sub/*foo", latte.call(requestOnUri("/sub/1/2")).body().get());
        assertEquals(404, latte.call(requestOnUri("/call")).status().intValue());

        Espresso latte2 = Latte.by(Espresso.class)
                .notFound((req) -> Response.of(404).body("foobar"))
                .intoEspresso();

        assertEquals("foobar", latte2.call(requestOnUri("/call")).body().get());
    }

    @Test
    public void testAsyncLatte() {
        Espresso.Async latte = Latte.by(Espresso.Async.class)
                .on("/", (req, send, raise) -> send.accept(Response.of(200).body("root")))
                .on("/sub", (req, send, raise) -> send.accept(Response.of(200).body("/sub")))
                .on("/sub/:foo", (req, send, raise) -> send.accept(Response.of(200).body("/sub/:foo")))
                // duplicated with a lower priority
                .on("/sub/:bar", (req, send, raise) -> send.accept(Response.of(200).body("/sub/:bar")))
                .on("/sub/*foo", (req, send, raise) -> send.accept(Response.of(200).body("/sub/*foo")))
                .intoEspressoAsync();

        latte.call(requestOnUri("/"), (resp) -> assertEquals("root", resp.body().get()), null);
        latte.call(requestOnUri("/sub"), (resp) -> assertEquals("/sub", resp.body().get()), null);
        latte.call(requestOnUri("/sub/1"), (resp) -> assertEquals("/sub/:foo", resp.body().get()), null);
        latte.call(requestOnUri("/sub/1/2"), (resp) -> assertEquals("/sub/*foo", resp.body().get()), null);
        latte.call(requestOnUri("/call"), (resp) -> assertEquals(404, resp.status().intValue()), null);
    }

    @Test
    public void testPathSpecCatching()  throws Exception {
        Espresso latte = Latte.by(Espresso.class)
                .on("/", (req) -> Response.of(200).body(String.valueOf(Latte.extension(req).size())))
                .on("/sub/:foo", (req) -> Response.of(200).body(Latte.extension(req).get("foo")))
                .on("/sub/*foo", (req) -> Response.of(200).body(Latte.extension(req).get("foo")))
                .intoEspresso();

        assertEquals("0", latte.call(requestOnUri("/")).body().get());
        assertEquals("123", latte.call(requestOnUri("/sub/123")).body().get());
        assertEquals("123/456", latte.call(requestOnUri("/sub/123/456")).body().get());
    }

    @Test
    public void testMethodMatching() throws Exception {
        Espresso latte = Latte.by(Espresso.class)
                .on(Request.Method.POST, "/foo", (req) -> Response.of(200).body("POST"))
                .on(Request.Method.GET, "/foo", (req) -> Response.of(200).body("GET"))
                .on(Request.Method.DELETE, "/foo", (req) -> Response.of(200).body("DELETE"))
                .intoEspresso();

        assertEquals("GET", latte.call(requestOnUri("/foo")).body().get());
    }

}
