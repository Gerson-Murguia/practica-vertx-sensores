package tech.murguia.sensor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CollectorService extends AbstractVerticle {
  public static final Logger logger = LoggerFactory.getLogger(CollectorService.class);
  private WebClient webClient;

  @Override
  public void start() {
    webClient=WebClient.create(vertx);
    vertx.createHttpServer()
      .requestHandler(this::handleRequest)
      .listen(8080);
  }

  private void handleRequest(HttpServerRequest request) {
    //lista para juntar los json responses
    List<JsonObject> responses=new ArrayList<>();
    //Contador para trackear respuestas, ya que pueden ser menos que el numero
    //de request cuando hay errores
    AtomicInteger counter=new AtomicInteger(0);
    for (int i=0; i < 3; i++){
      //manda un GET request en el puerto 3000+i
      webClient
        .get(3000+i,"localhost","/")
        //el predicado tira un error cuando el http status no esta en el rango 2xx
        .expect(ResponsePredicate.SC_SUCCESS)
        .as(BodyCodec.jsonObject())
        .send(ar->{
          if (ar.succeeded()){
            //agrega el jsonObject del body al response list
            responses.add(ar.result().body());
          }else{
            logger.error("Sensor apagado?",ar.cause());
          }
          //incrementa el counter hasta 3
          // luego pasa al snapshot la list responses
          if (counter.incrementAndGet()==3){
            JsonObject data=new JsonObject()
              .put("data",new JsonArray(responses));
            sendToSnapshot(request,data);
          }
        });
    }
  }

  private void sendToSnapshot(HttpServerRequest request, JsonObject data) {
    webClient
      .post(4000,"localhost","/")
      .expect(ResponsePredicate.SC_SUCCESS)
      .sendJsonObject(data,ar->{
        if (ar.succeeded()){
          sendResponse(request,data);
        }else{
          logger.error("Snapshot apagado?: ",ar.cause());
          request.response().setStatusCode(500).end();
        }
      });
  }

  private void sendResponse(HttpServerRequest request, JsonObject data) {
    request.response()
      .putHeader("Content-Type","application/json")
      //da un json compacto
      .end(data.encode());
  }
  

}
