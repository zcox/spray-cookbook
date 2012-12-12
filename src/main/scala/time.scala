package com.pongr

import spray.http.{HttpRequest, HttpResponse}
import spray.routing.{Directive0, HttpService}

/** Mix-in to an HttpService to get basic timing functions. */
trait Timing { this: HttpService =>

  /** Measures time between request and response, then logs it. */
  def time(log: (HttpRequest, HttpResponse, Long) => Unit): Directive0 =
    mapRequestContext { ctx =>
      val timeStamp = System.currentTimeMillis
      ctx.mapHttpResponse { response =>
        log(ctx.request, response, System.currentTimeMillis - timeStamp)
        response
      }
    }

  /** Logs request/response pairs like "GET /plusone => 200 OK '2'" and only includes first 50 chars of response entity. */
  def logRequestTime(request: HttpRequest, response: HttpResponse, time: Long) {
    //TODO might want to only log response entity for certain content types
    val entity = response.entity.asString
    val body = if (entity.size > 50) entity.take(50) + "..." else entity
    log.debug("%s %s => %s '%s' took %d msec" format (request.method, request.path, response.status.formatPretty, body, time))
  }
}