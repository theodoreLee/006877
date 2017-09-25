package com.goticks

import akka.util.Timeout
import com.typesafe.config.Config

import scala.concurrent.duration._

trait RequestTimeout {
  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}
