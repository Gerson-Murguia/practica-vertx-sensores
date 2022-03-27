package tech.murguia.sensor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.stream.Collectors;

public class SensorData extends AbstractVerticle {
  private final HashMap<String,Double> lastValues=new HashMap<>();

  @Override
  public void start() {
    EventBus bus=vertx.eventBus();
    bus.consumer("sensor.updates",this::update);
    bus.consumer("sensor.average",this::average);
  }

  private void update(Message<JsonObject> message) {
    JsonObject json=message.body();
    lastValues.put(json.getString("id"),json.getDouble("temp"));
  }

  private void average(Message<JsonObject> message) {
    //el mensaje que llega no se usa, ya que se esta respondiendo, no consumiendo
    //hacer promedio de Doubles y convertirlo en double
    double avg=lastValues.values().stream()
      .collect(Collectors.averagingDouble(Double::doubleValue));
    JsonObject json=new JsonObject().put("average",avg);
    //responder con el promedio de values
    message.reply(json);
  }
}
