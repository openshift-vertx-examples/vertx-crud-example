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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.jdbc.JDBCClient;

/**
 * Simple helper to bootstrap your Database.
 *
 * @author Paulo Lopes
 */
public class DBInitHelper {

  private DBInitHelper() {}

  public static void initDatabase(Vertx vertx, JDBCClient jdbc, Handler<AsyncResult<Void>> handler) {
    vertx.fileSystem().readFile("ddl.sql", read -> {
      if (read.failed()) {
        handler.handle(Future.failedFuture(read.cause()));
        return;
      }

      String[] statements = read.result().toString().split(";");

      jdbc.getConnection(get -> {
        if (get.failed()) {
          handler.handle(Future.failedFuture(read.cause()));
          return;
        }

        new Handler<Integer>() {
          @Override
          public void handle(Integer pos) {
            if (pos == statements.length) {
              handler.handle(Future.succeededFuture());
              return;
            }

            get.result().execute(statements[pos], exec -> {
              if (exec.failed()) {
                handler.handle(Future.failedFuture(read.cause()));
                return;
              }
              this.handle(pos + 1);
            });

          }
        }.handle(0);
      });
    });
  }
}
