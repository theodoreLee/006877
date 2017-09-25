package com.goticks

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.goticks.rest.RestApi
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success}

object Main extends App with RequestTimeout {
  //config 로드
  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher

  val log = Logging(system.eventStream, "go-tickets")

  //A ActorMaterializer takes the list of transformations comprising a Flow and materializes them in the form of org.reactivestreams.Processor instances.
  // How transformation steps are split up into asynchronous regions is implementation dependent.
  implicit val materializer = ActorMaterializer()

  val api = new RestApi(system, requestTimeout(config)).routes

  Http().bindAndHandle(api, host, port).map { serverBinding =>
    log.info(s"RestApi bound to ${serverBinding.localAddress} ")
  }.onComplete {
    case Success(v) =>
    case Failure(ex) =>
      log.error(ex, "Failed to bind to {}:{}!", host, port)
      system.terminate()
  }
}
