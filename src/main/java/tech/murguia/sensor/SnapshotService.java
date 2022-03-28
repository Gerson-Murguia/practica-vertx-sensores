package tech.murguia.sensor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class SnapshotService extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(SnapshotService.class);

  @Override
  public void start() {
    vertx.createHttpServer()
      .requestHandler(this::handler)
      .listen(config().getInteger("http.port",4000));
  }

  private void handler(HttpServerRequest request) {
    if (badRequest(request)){
      request.response().setStatusCode(400).end();
    }
    request.bodyHandler(buffer -> {
      logger.info("Ultimas temperaturas: "+buffer.toJsonObject().encodePrettily());
      request.response().end();
    });
  }

  private boolean badRequest(HttpServerRequest request) {
    //solo acepta post y application/json
    return !request.method().equals(HttpMethod.POST)||
      !"application/json".equals(request.getHeader("Content-Type"));
  }
}
