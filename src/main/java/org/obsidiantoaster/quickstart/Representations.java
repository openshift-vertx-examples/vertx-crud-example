package org.obsidiantoaster.quickstart;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Representations {

  public static JsonObject getLink(String name, String method, String url, String desc) {
    return new JsonObject()
      .put("rel", name)
      .put("method", method)
      .put("uri", url)
      .put("desc", desc);
  }

  public static JsonObject withLinks(JsonObject product, long id) {
    return product
      .put("_links", new JsonArray()
        .add(getLink("self", "GET", "/products/" + id, "Retrieve this product"))
        .add(getLink("prev", "GET", "/products/" + (id - 1), "Retrieve previous product"))
        .add(getLink("next", "GET", "/products/" + (id + 1), "Retrieve next product"))
        .add(getLink("product/delete", "DELETE", "/products/" + id, "Delete this product"))
        .add(getLink("product/list", "GET", "/products/", "Get all products"))
        .add(getLink("product/edit", "POST", "/products/" + id, "Edit this product"))
      );
  }

  public static JsonObject error(Throwable cause, String uri) {
    return error(cause.getMessage(), uri);
  }

  public static JsonObject error(String cause, String uri) {
    JsonObject result = new JsonObject();
    result.put("error", cause);
    if (uri != null) {
      result.put("uri", uri);
    }
    return result;
  }

  public static void error(RoutingContext ctx, int status, String cause, String uri) {
    ctx.response()
      .putHeader("Content-Type", "application/json")
      .setStatusCode(status)
      .end(error(cause, uri)
        .encodePrettily());
  }

  public static void error(RoutingContext ctx, int status, Throwable cause, String uri) {
    error(ctx, status, cause.getMessage(), uri);
  }


}
