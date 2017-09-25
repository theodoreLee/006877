package com.goticks.rest

import akka.actor.ActorRef
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.util.Timeout
import akka.pattern.ask
import com.goticks.BoxOffice._
import com.goticks.{BoxOffice, TicketSeller}
import com.goticks.json.EventMarshalling

import scala.concurrent.ExecutionContext

trait RestRoutes extends BoxOfficeApi with EventMarshalling {
  import akka.http.scaladsl.model.StatusCodes._

  def routes: Route = homeRoute ~ eventsRoute ~ eventRoute ~ ticketsRoute

  def eventsRoute = pathPrefix("events") {
    pathEndOrSingleSlash {
      get {
        onSuccess(getEvents()) { events =>
          complete(OK, events)
        }
      }
    }
  }

  def eventRoute = pathPrefix("events" / Segment) { event =>
    pathEndOrSingleSlash {
      post {
        // POST /events/:event
        entity(as[EventDescription]) { ed =>
          onSuccess(createEvent(event, ed.tickets)) {
            case BoxOffice.EventCreated(eventName) => complete(Created, eventName)
            case BoxOffice.EventExists =>
              val err = Error(s"$Event event exists already.")
              complete(BadRequest, err)
          }
        }
      } ~
      get {
        // GET /events/:event
        onSuccess(getEvent(event)) {
          _.fold(complete(NotFound))(complete(OK, _))
        }
      } ~
      delete {
        onSuccess(cancelEvent(event)) {
          _.fold(complete(NotFound))(complete(OK, _))
        }
      }
    }
  }

  def ticketsRoute = pathPrefix("events" / Segment / "tickets") { event =>
    post {
      pathEndOrSingleSlash {
        entity(as[TicketRequest]) { request =>
          onSuccess(requestTickets(event, request.tickets)) {tickets =>
            if(tickets.entries.isEmpty) complete(NotFound)
            else complete(Created, tickets)
          }
        }
      }
    }
  }
  def homeRoute = pathEndOrSingleSlash {
    get {
      complete(OK, "")
    }
  }
}

trait BoxOfficeApi {

  def createBoxOffice(): ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val boxOffice = createBoxOffice()

  def createEvent(event: String, nrOfTickets: Int) =
    boxOffice.ask(CreateEvent(event, nrOfTickets)).mapTo[EventResponse]

  def getEvent(event:String) =
    boxOffice.ask(GetEvent(event)).mapTo[Option[Event]]

  def getEvents() = boxOffice.ask(GetEvents).mapTo[Events]

  def cancelEvent(event: String) =
    boxOffice.ask(CancelEvent(event)).mapTo[Option[Event]]

  def requestTickets(event: String, tickets: Int) =
    boxOffice.ask(GetTickets(event, tickets)).mapTo[TicketSeller.Tickets]
}