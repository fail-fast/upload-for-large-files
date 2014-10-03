package com.forfun.upload

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

/**
 * Main class to define and bind the service
 */
object Rest extends App{

  implicit val system = ActorSystem("upload-service")

  val handler = system.actorOf(Props[UploadService], name = "upload-service-man")

  val config = new ServiceConfig().apiConfig

  IO(Http) ! Http.Bind(handler, interface = config.host, port = config.port)

}
