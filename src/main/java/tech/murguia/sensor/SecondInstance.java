package tech.murguia.sensor;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

public class SecondInstance {
  private static final Logger logger = LoggerFactory.getLogger(SecondInstance.class);

  public static void main(String[] args) {
    Vertx.clusteredVertx(new VertxOptions(),ar -> {
      if (ar.succeeded()){
        logger.info("Segunda instancia iniciada");
        Vertx vertx=ar.result();
        vertx.deployVerticle("tech.murguia.sensor.HeatSensor",
          new DeploymentOptions().setInstances(4));
        vertx.deployVerticle("tech.murguia.sensor.Listener");
        vertx.deployVerticle("tech.murguia.sensor.SensorData");
        //puerto distinto para usar ambas instancias
        JsonObject conf=new JsonObject()
          .put("port",8081);
        vertx.deployVerticle("tech.murguia.sensor.HttpServer",
          new DeploymentOptions(conf));
      }else{
        logger.error("No se pudo iniciar:", ar.cause().getCause());
      }
    });
  }
}
