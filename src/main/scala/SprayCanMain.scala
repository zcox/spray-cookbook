package com.pongr

import akka.actor._
import spray.can.server.HttpServer
import spray.io._

/** Minimal spray-can server setup. Mix-in to an object and provide a MessageHandler to get a runnable Main. */
trait SprayCanMain {
  lazy val system = ActorSystem("spray-cookbook")
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
  }
}
