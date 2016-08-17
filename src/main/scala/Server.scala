/**
  * Created by jasoncarter on 2016-08-11.
  */

import akka.NotUsed
import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.io.StdIn
import scala.sys.process.ProcessBuilder.Source

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
//    path("echo") {
//      parameter('userName') { userName =>
//        handleWebSocketMessages(echoMessage)
//      }
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


  val chatRoom = actorSystem.actorOf(Props(new ChatRoomActor), "ChatRoom")

// TODO: fix this using scalac.io example
  def websocketChatFlow(user: String): Flow[Message, Message, _] = {

    // each new connection is a new user
    // TODO: good enough for 1st iteration, change later
    val userActor = actorSystem.actorOf(Props(new ChatUserActor(user, chatRoom)))

    // input flow, take Messages
    val fromWebsocket: Sink[Message, _] = {
      Flow[Message].map {
        // transform to domain message
        case TextMessage.Strict(text) => Send(text)
      }.to(Sink.actorRef[Send](userActor, PoisonPill))
    }

    val toWebsocket: Source[Message, _] = {
      Source.actorRef[Send](bufferSize = 10, OverflowStrategy.fail)
        .mapMaterializedValue {
          actor => userActor ! Send("source")
            NotUsed
        }.map {
        (outMessage: Send) => TextMessage(outMessage, _)
      }
    }

    Flow.fromSinkAndSource(fromWebsocket, toWebsocket)

  }


  val binding = Http().bindAndHandle(route, interface, port)
  StdIn.readLine()

  binding.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
  println("Server is shutting down...")

}
