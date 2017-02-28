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

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.obsidiantoaster.quickstart.service.Store;
import org.obsidiantoaster.quickstart.service.impl.JdbcProductStore;

public class RestApplication extends AbstractVerticle {

  @Override
  public void start() {
    // Create a router object.
    Router router = Router.router(vertx);
    // Create a JDBC client
    // TODO: password shoud be retreived from the secret store + connection string from config map
    JDBCClient jdbc = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", "jdbc:postgresql://localhost:5432/quickstart")
      .put("driver_class", "org.postgresql.Driver")
      .put("user", "postgres")
      .put("password", "password"));

    DBInitHelper.initDatabase(vertx, jdbc, ready -> {
      if (ready.failed()) {
        ready.cause().printStackTrace();
        System.err.println("Deployment failed (check DDL or Postgres)!");
      } else {

        // Creata a JDBC store
        Store store = new JdbcProductStore(jdbc);

        // implement a basic REST CRUD mapping

        router.get("/products").handler(ctx -> {
          store.readAll(readAll -> {
            if (readAll.failed()) {
              ctx.fail(readAll.cause());
            } else {
              ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(readAll.result().encode());
            }
          });
        });

        router.get("/products/:id").handler(ctx -> {
          long id;

          try {
            id = Long.parseLong(ctx.pathParam("id"));
          } catch (NumberFormatException e) {
            ctx.fail(400);
            return;
          }

          store.read(id, read -> {
            if (read.failed()) {
              ctx.fail(read.cause());
            } else {
              if (read.result() == null) {
                ctx.response()
                  .setStatusCode(404)
                  .end();
              } else {
                ctx.response()
                  .putHeader("Content-Type", "application/json")
                  .end(read.result().encode());
              }
            }
          });
        });

        // enable parsing of request bodies
        router.route().handler(BodyHandler.create());

        router.post("/products").handler(ctx -> {
          JsonObject item;
          try {
            item = ctx.getBodyAsJson();
          } catch (RuntimeException e) {
            ctx.fail(e);
            return;
          }

          if (item == null) {
            ctx.fail(400);
            return;
          }

          store.create(item, create -> {
            if (create.failed()) {
              ctx.fail(create.cause());
            } else {
              ctx.response()
                .putHeader("Location", "/products/" + create.result())
                .putHeader("Content-Type", "application/json")
                .setStatusCode(201)
                .end(create.result().encode());
            }
          });
        });

        router.put("/products/:id").handler(ctx -> {
          long id;

          try {
            id = Long.parseLong(ctx.pathParam("id"));
          } catch (NumberFormatException e) {
            ctx.fail(400);
            return;
          }

          JsonObject item;
          try {
            item = ctx.getBodyAsJson();
          } catch (RuntimeException e) {
            ctx.fail(e);
            return;
          }

          if (item == null) {
            ctx.response().setStatusCode(400).end();
            return;
          }

          store.update(id, item, update -> {
            if (update.failed()) {
              ctx.fail(update.cause());
            } else {
              ctx.response()
                .setStatusCode(204)
                .end();
            }
          });
        });

        router.delete("/products/:id").handler(ctx -> {
          long id;

          try {
            id = Long.parseLong(ctx.pathParam("id"));
          } catch (NumberFormatException e) {
            ctx.fail(400);
            return;
          }

          store.delete(id, delete -> {
            if (delete.failed()) {
              ctx.fail(delete.cause());
            } else {
              ctx.response()
                .setStatusCode(204)
                .end();
            }
          });
        });

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
          .createHttpServer()
          .requestHandler(router::accept)
          .listen(
            // Retrieve the port from the configuration,
            // default to 8080.
            config().getInteger("http.port", 8080));

        System.out.println("Server ready!");
      }
    });
  }
}
