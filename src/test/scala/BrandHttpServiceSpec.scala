package com.pongr

import org.specs2.mutable.Specification
import akka.actor._
import akka.testkit.TestProbe
import spray.testkit.Specs2RouteTest
import spray.routing._
import spray.http.StatusCodes._
import spray.http.MediaTypes._
import BrandJsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.httpx.unmarshalling.pimpHttpEntity

class TestBrandSearchService extends Actor {
  def receive = {
    case FindBrands(query) => sender ! FoundBrands(Seq(Brands.starbucks))
  }
}

/** Shows how to test an HttpService by mocking-out the actors that it uses. It just tests the http request before the actor, and the http response after the actor. */
class BrandHttpServiceSpec extends Specification with Specs2RouteTest with BrandHttpService {
  def actorRefFactory = system

  /*NOTE could not find a good way for TestProbe to reply to sender, to complete the future
  val probe1 = TestProbe() //TODO do we need to recreate these for every test? if so we probably need to recreate BrandHttpService too
  val probe2 = TestProbe()
  val brandSearchService = probe1.ref
  val brandService = probe2.ref*/

  val brandSearchService = system.actorOf(Props[TestBrandSearchService])
  val brandService: ActorRef = null

  "The Brand http service" should {
    "handle brand searches" in {
      "reject if no query parameter" in {
        Get("/brands") ~> route ~> check {
          handled must beFalse
          rejection must_== MissingQueryParamRejection("query")
        }
      }

      "send query to brandSearchService and render json response" in {
        /*probe1.expectMsg(FindBrands("s"))
        probe1.send(probe1.sender, FoundBrands(Seq(Brands.starbucks)))*/
        Get("/brands?query=s") ~> route ~> check {
          handled must beTrue
          mediaType must_== `application/json`
          entityAs[String] must_== 
          """|[{
             |  "id": "b1",
             |  "name": "Starbucks",
             |  "email": "starbucks@pongr.com",
             |  "urlName": "Starbucks"
             |}]""".stripMargin
        }
      }
    }
  }
}