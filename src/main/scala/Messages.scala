/**
  * Created by jasoncarter on 2016-08-09.
  */

abstract class Msg

// client messages
case class Send(msg: String) extends Msg
case class NewMsg(from: String, msg: String) extends Msg
case class Info(msg: String) extends Msg

// server messages
case class Connect(username: String) extends Msg
case class Broadcast(msg: String) extends Msg
case object Disconnect extends Msg
