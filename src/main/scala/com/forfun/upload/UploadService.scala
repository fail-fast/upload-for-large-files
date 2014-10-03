package com.forfun.upload

import scala.concurrent.duration._
import akka.util.Timeout
import akka.actor._
import spray.can.Http
import spray.http._
import HttpMethods._
import MediaTypes._
import spray.can.Http.RegisterChunkHandler


class UploadService extends Actor with ActorLogging {
  implicit val timeout: Timeout = 1.second
  import context.dispatcher

  def receive = {
    /** when a new connection comes in we register ourselves as the connection handler */
    case _: Http.Connected => sender ! Http.Register(self)


    /**
     * In case a post upload comes not considering the HTTP1.1 Transfer-Encoding: chunked
     */
    case r@HttpRequest(POST, Uri.Path("/file-upload"), headers, entity: HttpEntity.NonEmpty, protocol) =>
      // emulate chunked behavior for POST requests to this path
      val parts = r.asPartStream()
      val client = sender
      val handler = context.actorOf(Props(new UploadHandler(client, parts.head.asInstanceOf[ChunkedRequestStart])))
      parts.tail.foreach(handler !)

    /**
     * To deal with large upload files the approach is be using HTTP1.1 Transfer-Encoding: chunked
     * Relying on Transfer-Encoding: chunked between the client and server allows a easy way
     * to deal with large files upload. It still does not provide a kind of back-pressure or
     * stream processing..
     */
    case start@ChunkedRequestStart(HttpRequest(POST, Uri.Path("/file-upload"), _, _, _)) =>
      val client = sender
      val handler = context actorOf UploadHandler.props(client, start)
      client ! RegisterChunkHandler(handler)


    /** handle any unknown endpoint call */
    case _: HttpRequest =>
      sender ! HttpResponse(
        status = 404,
        entity = HttpEntity(
          contentType = ContentTypes.`application/json`,
          string = s""" {"error":"This is an invalid endpoint"} """
        )
      )


    case Timedout(HttpRequest(method, uri, _, _, _)) =>
      sender ! HttpResponse(
        status = 500,
        entity = HttpEntity(
          contentType = ContentTypes.`application/json`,
          string = s""" {"error":"The $method request to '$uri' has timed out..."} """
        )
      )
  }


}
