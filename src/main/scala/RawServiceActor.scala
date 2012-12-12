package com.pongr

import akka.actor._
import spray.io._
import spray.can.server._
import spray.http._
import spray.http.HttpMethods._
import spray.http.StatusCodes._

/** Simple actor that matches a few different HTTP requests and sends HTTP responses. */
class RawServiceActor extends Actor with ActorLogging {
  override def preStart() {
    log.debug("Starting {}", this)
  }

  override def postStop() {
    log.debug("Stopped {}", this)
  }

  def receive = {
    case HttpRequest(GET, "/ping", _, _, _) => sender ! HttpResponse(entity = "PONG")
    case HttpRequest(GET, "/pong", _, _, _) => sender ! HttpResponse(entity = "WAT")
    case HttpRequest(GET, "/pongr", _, _, _) => sender ! HttpResponse(entity = "w00t")
    case HttpRequest(_, _, _, _, _) => sender ! HttpResponse(status = NotFound, entity = "This is not the resource you are looking for...")
  }
}

/** Uses one RawServiceActor instance to handle all requests. */
object RawServiceSingletonMain extends App with SprayCanMain {
  newHttpServer(SingletonHandler(system.actorOf(Props[RawServiceActor], "raw-service"))) ! Bind("localhost", 5555)
}

/** Uses one RawServiceActor instance per connection. */
object RawServicePerConnectionMain extends App with SprayCanMain {
  newHttpServer(PerConnectionHandler(pc => pc.connectionActorContext.actorOf(Props[RawServiceActor], "raw-service"))) ! Bind("localhost", 5555)
}

/** Uses one RawServiceActor instance per request. */
object RawServicePerMessageMain extends App with SprayCanMain {
  newHttpServer(PerMessageHandler(pc => pc.connectionActorContext.actorOf(Props[RawServiceActor], "raw-service"))) ! Bind("localhost", 5555)
}
