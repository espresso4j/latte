package io.github.espresso4j.latte;

import io.github.espresso4j.espresso.Espresso;
import io.github.espresso4j.espresso.ExtensionHolder;
import io.github.espresso4j.espresso.Request;
import io.github.espresso4j.espresso.Response;
import io.github.espresso4j.latte.internal.Route;
import io.github.espresso4j.latte.internal.Routes;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Url route for Espresso based application.
 *
 * Url pattern is also supported:
 *
 * * `:var` will match a single segment of url, and resolved to `var`
 * * `*var will match any segments of url, and resolved to `var
 *
 * Resolved url variable will be stored in extension of Request. Latte provides a static helper function `extension`
 * to get the variable-value map.
 *
 * @param <T> Espresso or Espresso.Async
 */
public class Latte<T> {

    private Routes<T> routes = new Routes<>();

    private T notFound;

    private int priority;

    private Class<T> type;

    private Latte (Class<T> type) {
        this.type = type;
    }

    /**
     * Create a Latte instance for Espresso or Espresso.Async
     *
     * @return a latte instance
     */
    public static <T> Latte<T> by(Class<T> type) {
        return new Latte<>(type);
    }

    /**
     * Bind a url pattern for a HTTP method request particular Espresso or Espresso.Async handler
     *
     * @param pathSpec the path spec
     * @param handler the handler to run when path spec matched
     * @return self
     */
    public Latte<T> on(@Nonnull Request.Method method, @Nonnull String pathSpec, @Nonnull T handler) {
        this.routes.addRoute(new Route<>(method.name(), pathSpec, handler, this.priority++));
        return this;
    }

    /**
     * Bind a url pattern for a particular Espresso or Espresso.Async handler
     *
     * @param pathSpec the path spec
     * @param handler the handler to run when path spec matched
     * @return self
     */
    public Latte<T> on(@Nonnull String pathSpec, @Nonnull T handler) {
        this.routes.addRoute(new Route<>(Route.ANY_METHOD, pathSpec, handler, this.priority++));
        return this;
    }

    /**
     * handler to invoke when url not found
     * @param handler the handler when no path spec matched
     * @return self
     */
    public Latte<T> notFound(@Nonnull T handler) {
        this.notFound = handler;
        return this;
    }

    /**
     * Create latte handler
     * @return an espresso handler contains current routes
     */
    public Espresso intoEspresso() {
        if (this.type == Espresso.class) {
            Espresso notFoundHandler = this.notFound != null ? (Espresso) this.notFound : (request -> Response.of(404));

            return request -> {
                String requestUri = request.getUri();
                String method = request.getRequestMethod().name();
                Optional<Route<T>> handler = Latte.this.routes.matchRoute(method, requestUri);

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
     * Create latte async handler
     * @return an espresso async handler contains for current routes
     */
    public Espresso.Async intoEspressoAsync() {
        if (this.type == Espresso.Async.class) {
            Espresso.Async notFoundHandler = this.notFound != null ? (Espresso.Async) this.notFound :
                    (request, send, raise) -> send.accept(Response.of(404));

            return (request, send, raise) -> {
                String requestUri = request.getUri();
                String method = request.getRequestMethod().name();

                Optional<Route<T>> handler = Latte.this.routes.matchRoute(method, requestUri);

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

    /**
     * A helper function to access latte variables in request
     *
     * @param ext the request instance
     * @return the latte variable map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> extension(ExtensionHolder ext) {
        Object value = ext.extension(Latte.class);
        if (value != null) {
            return (Map<String, String>) value;
        } else {
            return Collections.emptyMap();
        }
    }
}
