package com.pongr

import akka.actor._
import akka.dispatch._
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import spray.routing._
import spray.can.server._
import spray.http._
import spray.io._

class PlusOneActor extends Actor {
  def receive = {
    case n: Int => sender ! (n + 1)
  }
}

/** Shows how to complete a response using a future. */
trait PlusOneService extends HttpService {
  def plusOneActor: ActorRef
  implicit val timeout = Timeout(1 second)

  val route = 
    get {
      path("plusone") {
        parameter('number.as[Int]) { n => 
          complete {
            (plusOneActor ? n).map(_.toString)
          }
        }
      } ~ 
      path("plusone2") {
        parameter('number.as[Int]) { n => 
          complete {
            Future { n + 1 } map { _.toString }
          }
        }
      }
    }
}

object PlusOneMain extends SprayCanMain {
  val messageHandler = SingletonHandler(system.actorOf(Props(new Actor with PlusOneService {
    def actorRefFactory = context
    def receive = runRoute(route)
    val plusOneActor = system.actorOf(Props[PlusOneActor])
  }), "plus-one-service"))
}