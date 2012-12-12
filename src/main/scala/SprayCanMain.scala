package com.pongr

import akka.actor.{Props, ActorRef, ActorSystem}
import spray.io.{ServerSSLEngineProvider, MessageHandler, IOExtension}
import spray.can.server.{HttpServer, ServerSettings}
import spray.util.actorSystemNameFrom

/** Minimal spray-can server setup. Mix-in to an object and provide a MessageHandler to get a runnable Main. */
trait SprayCanMain {
  /*lazy val system = ActorSystem("spray-cookbook")
  lazy val ioBridge = new IOBridge(system).start()
  def messageHandler: MessageHandler
  lazy val httpServer = system.actorOf(Props(new HttpServer(ioBridge, messageHandler)), "http-server")
  
  def run() {
    httpServer ! HttpServer.Bind("localhost", 8080)
    system.registerOnTermination {
      ioBridge.stop()
    }
  }

  def main(args: Array[String]) {
    run()
  }*/


  val system = ActorSystem(actorSystemNameFrom(getClass))

  val ioBridge = IOExtension(system).ioBridge

  val Bind = HttpServer.Bind

  def newHttpServer(messageHandler: MessageHandler, settings: ServerSettings = ServerSettings(), name: String = "http-server") =
    system.actorOf(Props(new HttpServer(ioBridge, messageHandler, settings)), name)
}
