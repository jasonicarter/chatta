/**
  * Created by jasoncarter on 2016-08-09.
  */

import akka.actor._

class ChatServerActor extends Actor {

  var clients = List[(String, ActorRef)]()

  override def receive: Receive = {
    case Connect(username) => {
      broadcast(Info(f"$username%s joined the chat"))
      clients = (username, sender) :: clients // add new client to list of clients
      context.watch(sender)
    }
    case Broadcast(msg) => {
      val username = getUsername(sender)
      broadcast(NewMsg(username, msg))
    }
    case Terminated(client) => {
      val username = getUsername(client)
      clients = clients.filter(sender != _._2) // filter out every ActorRef != sender put back into clients
      broadcast(Info(f"$username%s left the chat"))
    }
  }

  def broadcast(msg: Msg): Unit = {
    clients.foreach(x => x._2 ! msg) // fire and forget - send a message asynchronously and return immediately.
  }

  def getUsername(actor: ActorRef): String = {
    clients.filter(actor == _._2).head._1 // find actor == actor from clients get head, then get username
  }
}
