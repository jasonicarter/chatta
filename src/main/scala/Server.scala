/**
  * Created by jasoncarter on 2016-08-11.
  */

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import scala.io.StdIn

object Server extends App {

  import akka.http.scaladsl.server.Directives._
  import actorSystem.dispatcher

  implicit val actorSystem = ActorSystem("chat-system")
  implicit val flowMaterializer = ActorMaterializer()

  val interface = "localhost"
  val port = 8080

  val route = get(
    pathEndOrSingleSlash {
      complete("Hello websocket server.")
    })

  val binding = Http().bindAndHandle(route, interface, port)
  StdIn.readLine()

  binding.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
  println("Server is shutting down...")

}
