package tech.murguia.sensor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

import java.text.DecimalFormat;

public class Listener extends AbstractVerticle {
  private final Logger logger = LoggerFactory.getLogger(Listener.class);
  private final DecimalFormat format=new DecimalFormat("#.##");

  @Override
  public void start() throws Exception {
    EventBus bus=vertx.eventBus();
    bus.consumer("sensor.updates",msg->{
      JsonObject body= (JsonObject) msg.body();
      String id=body.getString("id");
      String temperature=format.format(body.getDouble("temp"));
      logger.info(String.format("%s reporta una temperatura de %s C°",id,temperature));
    });
  }
}
