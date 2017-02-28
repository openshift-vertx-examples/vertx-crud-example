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
package org.obsidiantoaster.quickstart.service.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import org.obsidiantoaster.quickstart.service.Store;

import java.util.List;

/**
 * The implementation of the store.
 * @author Paulo Lopes
 */
public class JdbcProductStore implements Store {

  private static final
  // language=sql
    String INSERT = "INSERT INTO products (name, stock) VALUES (?, ?)";

  private static final
  // language=sql
    String SELECT_ONE = "SELECT * FROM products WHERE id = ?";

  private static final
  // language=sql
    String SELECT_ALL = "SELECT * FROM products";

  private static final
  // language=sql
    String UPDATE = "UPDATE products SET name = ?, stock = ? WHERE id = ?";

  private static final
  // language=sql
    String DELETE = "DELETE FROM products WHERE id = ?";

  private final JDBCClient db;

  public JdbcProductStore(JDBCClient db) {
    this.db = db;
  }

  @Override
  public void create(JsonObject item, Handler<AsyncResult<JsonObject>> handler) {
    db.getConnection(res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        res.result().updateWithParams(INSERT, new JsonArray().add(item.getString("name")).add(item.getInteger("stock")), insert -> {
          // close the connection
          res.result().close();

          if (insert.failed()) {
            handler.handle(Future.failedFuture(insert.cause()));
          } else {
            handler.handle(Future.succeededFuture(item.put("id", insert.result().getKeys().getLong(0))));
          }
        });
      }
    });

  }

  @Override
  public void readAll(Handler<AsyncResult<JsonArray>> handler) {
    db.getConnection(res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        res.result().query(SELECT_ALL, select -> {
          // close the connection
          res.result().close();

          if (select.failed()) {
            handler.handle(Future.failedFuture(select.cause()));
          } else {
            handler.handle(Future.succeededFuture(new JsonArray(select.result().getRows())));
          }
        });
      }
    });
  }

  @Override
  public void read(long id, Handler<AsyncResult<JsonObject>> handler) {
    db.getConnection(res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        res.result().queryWithParams(SELECT_ONE, new JsonArray().add(id), select -> {
          // close the connection
          res.result().close();

          if (select.failed()) {
            handler.handle(Future.failedFuture(select.cause()));
          } else {
            List<JsonObject> rows = select.result().getRows();
            if (rows.size() > 0) {
              handler.handle(Future.succeededFuture(rows.get(0)));
            } else {
              handler.handle(Future.succeededFuture());
            }
          }
        });
      }
    });
  }

  @Override
  public void update(long id, JsonObject item, Handler<AsyncResult<Void>> handler) {
    db.getConnection(res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        res.result().updateWithParams(UPDATE, new JsonArray().add(item.getString("name")).add(item.getInteger("stock")).add(id), update -> {
          // close the connection
          res.result().close();

          if (update.failed()) {
            handler.handle(Future.failedFuture(update.cause()));
          } else {
            handler.handle(Future.succeededFuture());
          }
        });
      }
    });
  }

  @Override
  public void delete(long id, Handler<AsyncResult<Void>> handler) {
    db.getConnection(res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        res.result().updateWithParams(DELETE, new JsonArray().add(id), delete -> {
          // close the connection
          res.result().close();

          if (delete.failed()) {
            handler.handle(Future.failedFuture(delete.cause()));
          } else {
            handler.handle(Future.succeededFuture());
          }
        });
      }
    });
  }
}
