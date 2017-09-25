package com.goticks.json

import com.goticks.BoxOffice.{Event, Events}
import com.goticks.TicketSeller
import com.goticks.rest.{Error, TicketRequest, EventDescription}
import spray.json.DefaultJsonProtocol

trait EventMarshalling extends DefaultJsonProtocol {
  implicit val eventFormat = jsonFormat2(Event)
  implicit val eventsFormat = jsonFormat1(Events)
  implicit val eventDescriptionFormat = jsonFormat1(EventDescription)

  implicit val errorFormat = jsonFormat1(Error)

  implicit val ticketREquestFormat = jsonFormat1(TicketRequest)
  implicit val ticketFormat = jsonFormat1(TicketSeller.Ticket)
  implicit val ticketsFormat = jsonFormat2(TicketSeller.Tickets)
}
