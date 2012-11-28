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
import spray.json.DefaultJsonProtocol
import java.util.UUID.{randomUUID => randomUuid}

case class Brand(id: String, name: String, email: String, urlName: String)
case class FindBrands(query: String)
case class FoundBrands(brands: Seq[Brand])
case class CreateBrand(name: String, email: String, urlName: String)
case class CreatedBrand(brand: Brand)

/** Allows us to marshall a Brand or a Seq[Brand]. */
object BrandJsonProtocol extends DefaultJsonProtocol {
  implicit val BrandFormat = jsonFormat4(Brand)
}

object Brands {
  val starbucks = Brand("b1", "Starbucks", "starbucks@pongr.com", "Starbucks")
  val samsClub = Brand("b2", "Sam's Club", "samsclub@pongr.com", "SamsClub")
  val pepsi = Brand("b3", "Pepsi", "pepsi@pongr.com", "Pepsi")
  val square = Brand("b4", "Square", "square@pongr.com", "Square")
  val all = Seq(starbucks, samsClub, pepsi, square)
}

/** Complex search logic that we don't want in BrandHttpService goes here. */
class BrandSearchService extends Actor {
  def receive = {
    case FindBrands(query) => sender ! FoundBrands(Brands.all.filter(_.name.toLowerCase.startsWith(query.toLowerCase)))
  }
}

/** Example of using a trait that returns a Future, instead of using an actor. */
trait BrandSearchService2 {
  def findBrands(query: String): Future[FoundBrands]
}

/** Complex brand CRUD logic that we don't want in BrandHttpService goes here. */
class BrandService extends Actor {
  def receive = {
    //TODO validations: name is unique, email & urlName well-formed, etc
    case CreateBrand(name, email, urlName) => sender ! CreatedBrand(Brand(randomUuid.toString, name, email, urlName))
  }
}

/** Example of the http service deferring all logic to other actors, completing with futures, and marshalling domain objects to json. 
  * GET /brands?query=s
  * POST /brands name=New%20Brand&email=newbrand%40.com&urlName=NewBrand
  */
trait BrandHttpService extends HttpService {
  import BrandJsonProtocol._
  import spray.httpx.SprayJsonSupport._
  
  def brandSearchService: ActorRef
  def brandService: ActorRef
  implicit val timeout = Timeout(1 second)

  val route = 
    path("brands") {
      get {
        parameter('query).as(FindBrands) { query => 
          complete {
            (brandSearchService ? query).mapTo[FoundBrands].map(_.brands)
          }
        }
      } ~ 
      post {
        formFields('name, 'email, 'urlName).as(CreateBrand) { command => 
          complete {
            (brandService ? command).mapTo[CreatedBrand].map(_.brand)
          }
        }
      }
    }
}

object BrandServiceMain extends SprayCanMain {
  val messageHandler = SingletonHandler(system.actorOf(Props(new Actor with BrandHttpService {
    def actorRefFactory = context
    def receive = runRoute(route)
    val brandSearchService = system.actorOf(Props[BrandSearchService])
    val brandService = system.actorOf(Props[BrandService])
  }), "brand-http-service"))
}