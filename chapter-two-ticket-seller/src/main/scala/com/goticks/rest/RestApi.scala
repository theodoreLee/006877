package com.goticks.rest

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.goticks.BoxOffice

class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout = timeout

  implicit def executionContext = system.dispatcher

  override def createBoxOffice(): ActorRef = system.actorOf(BoxOffice.props, BoxOffice.name)
}

