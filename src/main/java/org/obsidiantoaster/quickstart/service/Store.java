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
package org.obsidiantoaster.quickstart.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * A CRUD to SQL interface
 * @author Paulo Lopes
 */
public interface Store {

    void create(JsonObject item, Handler<AsyncResult<JsonObject>> handler);

    void readAll(Handler<AsyncResult<JsonArray>> handler);

    void read(long id, Handler<AsyncResult<JsonObject>> handler);

    void update(long id, JsonObject item, Handler<AsyncResult<Void>> handler);

    void delete(long id, Handler<AsyncResult<Void>> handler);
}
