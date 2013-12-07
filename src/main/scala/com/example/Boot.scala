package com.example

import spray.http._
import spray.client.pipelining._
import akka.actor.{PoisonPill, Props, Actor, ActorSystem}
import scala.concurrent._
import spray.json._
import spray.httpx.SprayJsonSupport._
import scala.concurrent.duration._
import scala.language.postfixOps
import DefaultJsonProtocol._
import akka.pattern.after
import scala.util._
import akka.pattern.ask
import akka.util.Timeout

case class Url(url: String)
case class Urls(urls: List[Url])
object UrlsJsonProtocol extends DefaultJsonProtocol {
  implicit val urlFormat = jsonFormat1(Url)
  implicit val urlsFormat = jsonFormat1(Urls)
}
import UrlsJsonProtocol._

case class AskCount()
class Counter extends Actor {
  var total = 0
  var bad = 0
  var good = 0

  def receive = {
    case HttpResponse(status, _, _, _) =>
      total += 1
      if (status.intValue == 200) good += 1
      else bad += 1
    case AskCount =>
      sender ! "Total: %s, good: %s, bad: %s".format(total, good, bad)
  }
}

object Boot extends App {
  println("start...")

  implicit val system = ActorSystem()
  import system.dispatcher // execution context for futures

  val pipeline: HttpRequest => Future[Urls] = sendReceive ~> unmarshal[Urls]
  val pipelineBody: HttpRequest => Future[HttpResponse] = sendReceive

  val response: Future[Urls] = pipeline(Get("http://peaceful-falls-6706.herokuapp.com/sample?n=1000"))

  val counterActor = system.actorOf(Props[Counter])

  response foreach {
    case Urls(urls) =>
      val futures: List[Future[HttpResponse]] = urls.map(url => pipelineBody(Get(url.url)))

      for (f <- futures) {
        f onSuccess {
          case response => println(response)
            counterActor ! response
        }
      }

      akka.pattern.after(5 seconds, using = system.scheduler)(Future.successful(true)) onComplete {
        case _ =>
          implicit val timeout = Timeout(1 seconds)
          (counterActor ? AskCount) foreach println
          system.shutdown()
      }
  }
}
