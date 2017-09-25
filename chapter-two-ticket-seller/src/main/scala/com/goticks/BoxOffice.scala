package com.goticks

import akka.actor._
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.concurrent.Future

object BoxOffice {
  def props(implicit timeout: Timeout) = Props(new BoxOffice)
  def name = "boxOffice"

  case class CreateEvent(name:String, tickets:Int)
  case class GetEvent(name: String)
  case object GetEvents
  case class GetTickets(event: String, tickets:Int)
  case class CancelEvent(name:String)
  case class Event(name:String, tickets: Int)
  case class Events(events:Vector[Event])

  sealed trait EventResponse
  case class EventCreated(event:Event) extends EventResponse
  case object EventExists extends EventResponse
}

class BoxOffice(implicit timeout: Timeout) extends Actor {
  import BoxOffice._
  import context._

  def createTicketSeller(name: String) = context.actorOf(TicketSeller.props(name), name)

  override def receive: Receive = {
    case CreateEvent(eventName, tickets) =>
      def create = {
        val ticketSeller = createTicketSeller(eventName)
        val newTickets = (1 to tickets).map(TicketSeller.Ticket).toVector
        ticketSeller ! TicketSeller.Add(newTickets)
        sender() ! EventCreated(Event(eventName, tickets))
      }

      context.child(eventName).fold(create)(_ => sender() ! EventExists)

    case GetEvent(event) =>
      def notFound() = sender() ! None
      def getEvent(child: ActorRef) = child forward TicketSeller.GetEvent
      context.child(event).fold(notFound())(getEvent)

    case GetEvents =>
      def getEvents = context.children.map { child =>
        (self ? GetEvent(child.path.name)).mapTo[Option[Event]]
      }
      def convertToEvents(i: Iterable[Option[Event]]) = Events(i.flatten.toVector)

      Future.sequence(getEvents).map(convertToEvents) pipeTo sender()

    case CancelEvent(event) =>
      def notFound = sender() ! None
      def cancelEvent(child: ActorRef) = child forward TicketSeller.Cancel
      context.child(event).fold(notFound)(cancelEvent)

    case GetTickets(event, tickets) =>
      def notFound = sender() ! TicketSeller.Tickets(event)
      def buy(child:ActorRef) = child forward TicketSeller.Buy(tickets)
      context.child(event).fold(notFound)(buy)
  }
}
