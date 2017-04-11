package io.openshift.booster;

import io.openshift.booster.service.Store;
import io.openshift.booster.service.impl.JdbcProductStore;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.jdbc.JDBCClient;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import rx.Single;

import java.util.NoSuchElementException;

import static io.openshift.booster.Errors.error;

public class CrudApplication extends AbstractVerticle {

  private Store store;

  @Override
  public void start() {
    // Create a router object.
    Router router = Router.router(vertx);
    // implement a basic REST CRUD mapping
    router.get("/api/fruits").handler(this::retrieveAll);
    router.get("/api/fruits/:id").handler(this::getOne);


    // enable parsing of request bodies
    router.route().handler(BodyHandler.create());
    router.post("/api/fruits").handler(this::addOne);
    router.put("/api/fruits/:id").handler(this::updateOne);
    router.delete("/api/fruits/:id").handler(this::deleteOne);
    // health check
    router.get("/health").handler(rc -> rc.response().end("OK"));
    // web interface
    router.get().handler(StaticHandler.create());


    // Create a JDBC client
    JDBCClient jdbc = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", "jdbc:postgresql://" + getEnv("MY_DATABASE_SERVICE_HOST", "localhost") + ":5432/my_data")
      .put("driver_class", "org.postgresql.Driver")
      .put("user", getEnv("DB_USERNAME", "user"))
      .put("password", getEnv("DB_PASSWORD", "password "))
    );

    DBInitHelper.initDatabase(vertx, jdbc)
      .andThen(initHttpServer(router, jdbc))
      .subscribe(
        (http) -> System.out.println("Server ready on port " + http.actualPort()),
        Throwable::printStackTrace
      );
  }

  private Single<HttpServer> initHttpServer(Router router, JDBCClient client) {
    store = new JdbcProductStore(client);
    // Create the HTTP server and pass the "accept" method to the request handler.
    return vertx
      .createHttpServer()
      .requestHandler(router::accept)
      .rxListen(8080);
  }


  private void retrieveAll(RoutingContext ctx) {
    HttpServerResponse response = ctx.response()
      .putHeader("Content-Type", "application/json");
    JsonArray res = new JsonArray();
    store.readAll()
      .subscribe(
        res::add,
        err -> error(ctx, 415, err),
        () -> response.end(res.encodePrettily())
      );
  }


  private void getOne(RoutingContext ctx) {
    HttpServerResponse response = ctx.response()
      .putHeader("Content-Type", "application/json");

    long id = getId(ctx);
    if (id == -1) {
      error(ctx, 400, "invalid id");
      return;
    }

    store.read(id)
      .subscribe(
        json -> response.end(json.encodePrettily()),
        err -> {
          if (err instanceof NoSuchElementException) {
            error(ctx, 404, err);
          } else if (err instanceof IllegalArgumentException) {
            error(ctx, 415, err);
          } else {
            error(ctx, 500, err);
          }
        }
      );
  }

  private void addOne(RoutingContext ctx) {
    JsonObject item;
    try {
      item = ctx.getBodyAsJson();
    } catch (RuntimeException e) {
      error(ctx, 415, "invalid payload");
      return;
    }

    if (item == null) {
      error(ctx, 415, "invalid payload");
      return;
    }

    store.create(item)
      .subscribe(
        json ->
          ctx.response()
            .putHeader("Location", "/api/fruits/" + json.getInteger("id"))
            .putHeader("Content-Type", "application/json")
            .setStatusCode(201)
            .end(json.encodePrettily()),
        err -> writeError(ctx, err)
      );
  }

  private void updateOne(RoutingContext ctx) {
    long id = getId(ctx);
    if (id == -1) {
      error(ctx, 404, "unknown fruit");
      return;
    }

    JsonObject item;
    try {
      item = ctx.getBodyAsJson();
    } catch (RuntimeException e) {
      error(ctx, 415, "invalid payload");
      return;
    }

    if (item == null) {
      error(ctx, 415, "invalid payload");
      return;
    }

    store.update(id, item)
      .subscribe(
        () ->
          ctx.response()
            .putHeader("Content-Type", "application/json")
            .setStatusCode(200)
            .end(item.put("id", id).encodePrettily()),
        err -> writeError(ctx, err)
      );
  }

  private void writeError(RoutingContext ctx, Throwable err) {
    if (err instanceof NoSuchElementException) {
      error(ctx, 404, err);
    } else if (err instanceof IllegalArgumentException) {
      error(ctx, 422, err);
    } else {
      error(ctx, 409, err);
    }
  }

  private void deleteOne(RoutingContext ctx) {
    long id = getId(ctx);
    if (id == -1) {
      error(ctx, 404, "unknown product");
      return;
    }

    store.delete(id)
      .subscribe(
        () ->
          ctx.response()
            .setStatusCode(204)
            .end(),
        err -> {
          if (err instanceof NoSuchElementException) {
            error(ctx, 404, err);
          } else {
            error(ctx, 415, err);
          }
        }
      );
  }

  private long getId(RoutingContext ctx) {
    try {
      return Long.parseLong(ctx.pathParam("id"));
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private String getEnv(String key, String dv) {
    String s = System.getenv(key);
    if (s == null) {
      return dv;
    }
    return s;
  }
}
