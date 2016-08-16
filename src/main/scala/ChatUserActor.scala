/**
  * Created by jasoncarter on 2016-08-09.
  */

import akka.actor._

class ChatUserActor(val username: String, server: ActorRef) extends Actor {

  server ! Connect(username) // send connect msg to serverActor

  override def receive: Receive = {
    case NewMsg(from, msg) => {
      println(f"[$username%s's client] - $from%s: $msg%s")
    }
    case Send(msg) => server ! Broadcast(msg)
    case Info(msg) => {
      println(f"[$username%s's client] - $msg%s")
    }
    case Disconnect => {
      self ! PoisonPill // trigger termination and sends terminated msg to server
    }
  }

}
