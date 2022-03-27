package tech.murguia.sensor;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class FirstInstance {
  private static final Logger logger = LoggerFactory.getLogger(FirstInstance.class);

  public static void main(String[] args) {
    //comenzar un clustered vertx app es una operación asincrona
    Vertx.clusteredVertx(new VertxOptions(),ar->{
      //si es exitosa, obtenemos la instancia de vertx
      if (ar.succeeded()){
        logger.info("Primera instancia iniciada");
        Vertx vertx=ar.result();
        vertx.deployVerticle("tech.murguia.sensor.HeatSensor",
          new DeploymentOptions().setInstances(4));
        vertx.deployVerticle("tech.murguia.sensor.HttpServer");
      }else{
        //una razon para fallar seria la ausencia de libreria  de cluster manager
        logger.error("No se pudo iniciar: ",ar.cause());
      }
    });
  }
}
