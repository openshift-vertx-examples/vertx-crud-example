/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.obsidiantoaster.quickstart;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.obsidiantoaster.quickstart.service.Store;
import org.obsidiantoaster.quickstart.service.impl.JdbcProductStore;

import static org.obsidiantoaster.quickstart.Representations.getLink;
import static org.obsidiantoaster.quickstart.Representations.error;
import static org.obsidiantoaster.quickstart.Representations.withLinks;

public class RestApplication extends AbstractVerticle {

  private Store store;

  @Override
  public void start() {
    // Create a router object.
    Router router = Router.router(vertx);
    // implement a basic REST CRUD mapping
    router.get("/products").handler(this::retrieveAll);
    router.get("/products/:id").handler(this::getOne);
    // enable parsing of request bodies
    router.route().handler(BodyHandler.create());
    router.post("/products").handler(this::addOne);
    router.put("/products/:id").handler(this::updateOne);
    router.delete("/products/:id").handler(this::deleteOne);
    // health check
    router.get("/health").handler(rc -> rc.response().end("OK"));
    // web interface
    router.get().handler(StaticHandler.create());


    // Create a JDBC client

    JDBCClient jdbc = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", "jdbc:postgresql://" + getEnv("MY_DATABASE_SERVICE_HOST", "localhost") + ":5432/my_data")
      .put("driver_class", "org.postgresql.Driver")
      .put("user", getEnv("DB_USERNAME", "postgres"))
      .put("password", getEnv("DB_PASSWORD", "postgres"))
    );

    DBInitHelper.initDatabase(vertx, jdbc, ready -> {
      if (ready.failed()) {
        ready.cause().printStackTrace();
        System.err.println("Deployment failed (check DDL or Postgres)!");
      } else {

        // Create a JDBC store
        store = new JdbcProductStore(jdbc);


        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
          .createHttpServer()
          .requestHandler(router::accept)
          .listen(8080);

        System.out.println("Server ready!");
      }
    });
  }


  private void retrieveAll(RoutingContext ctx) {
    store.readAll(readAll -> {
      if (readAll.failed()) {
        error(ctx, 400, readAll.cause(), "/products");
      } else {
        JsonArray res = new JsonArray();
        for (int i = 0; i < readAll.result().size(); i++) {
          JsonObject object = readAll.result().getJsonObject(i);
          res.add(withLinks(object, object.getLong("id")));
        }
        ctx.response()
          .putHeader("Content-Type", "application/json")
          .end(new JsonObject()
            .put("items", res)
            .put("_links", new JsonArray()
              .add(getLink("self", "GET", "/products", "Retrieve all products"))
              .add(getLink("product/new", "PUT", "/products", "Create a new product"))
              .add(getLink("product/get", "GET", "/products/:id", "Get a specific product")))
            .encodePrettily());
      }
    });
  }


  private void getOne(RoutingContext ctx) {
    long id = getId(ctx);
    if (id == -1) {
      error(ctx, 400, "invalid id", "/products/:id");
      return;
    }

    store.read(id, read -> {
      if (read.failed()) {
        error(ctx, 400, read.cause(), "/products/" + id);
      } else {
        if (read.result() == null) {
          error(ctx, 404, "unknown product", "/products/" + id);
        } else {
          ctx.response()
            .putHeader("Content-Type", "application/json")
            .end(withLinks(read.result(), id).encodePrettily());
        }
      }
    });
  }

  private void addOne(RoutingContext ctx) {
    JsonObject item;
    try {
      item = ctx.getBodyAsJson();
    } catch (RuntimeException e) {
      error(ctx, 400, "invalid payload", "/products");
      return;
    }

    if (item == null) {
      error(ctx, 400, "invalid payload", "/products");
      return;
    }

    store.create(item, create -> {
      if (create.failed()) {
        error(ctx, 409, create.cause(), "/products");
      } else {
        ctx.response()
          .putHeader("Location", "/products/" + create.result())
          .putHeader("Content-Type", "application/json")
          .setStatusCode(201)
          .end(withLinks(create.result(), create.result().getLong("id")).encode());
      }
    });
  }

  private void updateOne(RoutingContext ctx) {
    long id = getId(ctx);
    if (id == -1) {
      error(ctx, 404, "unknown product", "/products/:id");
      return;
    }

    JsonObject item;
    try {
      item = ctx.getBodyAsJson();
    } catch (RuntimeException e) {
      error(ctx, 400, "invalid payload", "/products/:id");
      return;
    }

    if (item == null) {
      error(ctx, 422, "invalid payload", "/products/:id");
      return;
    }

    store.update(id, item, update -> {
      if (update.failed()) {
        error(ctx, 409, update.cause(), "/products/" + id);
      } else {
        ctx.response()
          .putHeader("Content-Type", "application/json")
          .setStatusCode(200)
          .end(withLinks(item.put("id", id), id).encodePrettily());
      }
    });
  }

  private void deleteOne(RoutingContext ctx) {
    long id = getId(ctx);
    if (id == -1) {
      error(ctx, 404, "unknown product", "/products/:id");
      return;
    }

    store.delete(id, delete -> {
      if (delete.failed()) {
        error(ctx, 400, delete.cause(), "/products/" + id);
      } else {
        ctx.response()
          .setStatusCode(204)
          .end();
      }
    });
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
