package com.goticks

package object rest {
  case class EventDescription(tickets : Int) {
    require(tickets > 0)
  }
  case class Error(message: String)

  case class TicketRequest(tickets: Int) {
    require(tickets > 0)
  }
}
