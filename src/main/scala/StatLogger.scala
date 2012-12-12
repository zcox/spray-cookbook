package com.pongr

import akka.actor._
import akka.util.duration._
import spray.can.server.HttpServer
import spray.can.server.HttpServer._

//TODO IOBridge also supports GetStats http://spray.io/documentation/spray-io/io-bridge/

/** Periodically requests stats from an HttpServer actor and logs them. */
class StatLogger(server: ActorRef, delay: Long = 3000l) extends Actor with ActorLogging {
  def receive = {
    case GetStats => server ! GetStats
    case stats: Stats => 
      log.debug(stats.toString)
      context.system.scheduler.scheduleOnce(delay milliseconds, self, GetStats)
  }
}

object StatLogger {
  def apply(system: ActorSystem, httpServer: ActorRef): ActorRef = {
    val logger = system.actorOf(Props(new StatLogger(httpServer)), "stat-logger")
    logger ! HttpServer.GetStats
    logger
  }
}
