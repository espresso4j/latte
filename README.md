# Latte

[![Maven Central](https://img.shields.io/maven-central/v/io.github.espresso4j/latte.svg)](https://search.maven.org/artifact/io.github.espresso4j/latte)
[![Javadocs](http://www.javadoc.io/badge/io.github.espresso4j/latte.svg)](http://www.javadoc.io/doc/io.github.espresso4j/latte)
[![Travis (.org)](https://img.shields.io/travis/espresso4j/latte.svg)](https://travis-ci.org/espresso4j/latte)
![GitHub](https://img.shields.io/github/license/espresso4j/latte.svg)
[![Liberapay patrons](https://img.shields.io/liberapay/patrons/Sunng.svg)](https://liberapay.com/Sunng/donate)

~~Streamed milk~~ Router for
[espresso](https://github.com/espresso4j/espresso).

Latte allows you to route requests to certain espresso handler based
on URL path pattern. It also extract variables from url spec for your
handler.

## Usage

Latte breaks URL into slash `/` separated segments.

* `/foo` matches url `/foo`
* `:foo` matches a single segment in url path
* `*foo` matches rest of url segments

If you don't specify request method, it will match any incoming request
for this path.

```java
import io.github.espresso4j.espresso.*;
import io.github.espresso4j.latte.*;

var index = req -> Response.of(200).body("It works");
var fetch = req -> {
    var id = Latte.extension(req).get("id");
    return Response.of(200).body(String.format("Getting %s", id));
};

var notFound = req -> Response.of(404);

var app = Latte.by(Espresso.class)
    .on("/", index)
    .on(Request.Method.GET, "/fetch/:id", fetch)
    .notFound(notFound)
    .intoEspresso();

```

## License

See [license](https://github.com/espresso4j/latte/blob/master/LICENSE)
