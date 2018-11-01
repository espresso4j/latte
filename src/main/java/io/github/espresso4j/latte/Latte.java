package io.github.espresso4j.latte;

import io.github.espresso4j.espresso.Espresso;
import io.github.espresso4j.espresso.Request;
import io.github.espresso4j.espresso.Response;
import io.github.espresso4j.latte.internal.Route;
import io.github.espresso4j.latte.internal.Routes;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class Latte<T> {

    private Routes<T> routes = new Routes<>();

    private T notFound;

    private int priority;

    private Class<T> type;

    private Latte (Class<T> type) {
        this.type = type;
    }

    /**
     *
     * @return
     */
    public static <T> Latte<T> by(Class<T> type) {
        return new Latte<>(type);
    }

    /**
     *
     * @param pathSpec
     * @param handler
     * @return
     */
    public Latte<T> on(@Nonnull String pathSpec, @Nonnull T handler) {
        this.routes.addRoute(new Route<>(pathSpec, handler, this.priority++));
        return this;
    }

    /**
     * handler to invoke when url not found
     * @param handler
     * @return
     */
    public Latte<T> notFound(@Nonnull T handler) {
        this.notFound = handler;
        return this;
    }

    /**
     * Create latte handler
     * @return
     */
    public Espresso intoEspresso() {
        if (this.type == Espresso.class) {
            Espresso notFoundHandler = this.notFound != null ? (Espresso) this.notFound : (request -> Response.of(404));

            return request -> {
                String requestUri = request.getUri();
                Optional<Route<T>> handler = Latte.this.routes.matchRoute(requestUri);

                if (handler.isPresent()) {
                    // matched
                    Route<T> route = handler.get();
                    Espresso espressoHandler = (Espresso) route.getPayload();

                    Map<String, String> captures = route.getPathSpec().matchSpec(requestUri);
                    request.extension(Latte.class, captures);
                    return espressoHandler.call(request);
                } else {
                    // not found
                    return notFoundHandler.call(request);
                }
            };
        } else {
            throw new IllegalStateException("Cannot build sync espresso from async latte.");
        }
    }

    /**
     * Create latte handler
     * @return
     */
    public Espresso.Async intoEspressoAsync() {
        if (this.type == Espresso.Async.class) {
            Espresso.Async notFoundHandler = this.notFound != null ? (Espresso.Async) this.notFound :
                    (request, send, raise) -> send.accept(Response.of(404));

            return (request, send, raise) -> {
                String requestUri = request.getUri();
                Optional<Route<T>> handler = Latte.this.routes.matchRoute(requestUri);

                if (handler.isPresent()) {
                    // matched
                    Route<T> route = handler.get();
                    Espresso.Async espressoHandler = (Espresso.Async) route.getPayload();

                    Map<String, String> captures = route.getPathSpec().matchSpec(requestUri);
                    request.extension(Latte.class, captures);
                    espressoHandler.call(request, send, raise);
                } else {
                    // not found
                    notFoundHandler.call(request, send, raise);
                }
            };
        } else {
            throw new IllegalStateException("Cannot build async espresso from sync latte.");
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> extension(Request request) {
        Object value = request.extensions().get(Latte.class);
        if (value != null) {
            return (Map<String, String>) value;
        } else {
            return Collections.emptyMap();
        }
    }
}
