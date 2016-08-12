/**
  * Created by jasoncarter on 2016-08-11.
  */

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

import scala.io.StdIn

object Server extends App {

  import akka.http.scaladsl.server.Directives._
  import actorSystem.dispatcher

  implicit val actorSystem = ActorSystem("akkaChat")
  implicit val flowMaterializer = ActorMaterializer()

  // TODO: pull from config file
  val interface = "localhost"
  val port = 8080

  // Create a couple routes in rule
  val route = get {
      pathEndOrSingleSlash {
        complete("Hello websocket server.")
      }
    } ~
    path("echo") {
      get {
        handleWebSocketMessages(echoMessage)
      }
    }

  // handle websocket connection and look for Message at input and offer Message as output
  val echoMessage: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => TextMessage("WS-ECHO: " + txt)
    case _ => TextMessage("Message type unsupported.")
  }

  val binding = Http().bindAndHandle(route, interface, port)
  StdIn.readLine()

  binding.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
  println("Server is shutting down...")

}
