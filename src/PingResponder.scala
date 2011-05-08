
trait PingResponder extends ResponsiveHandler {
  def onMessage(msg: Message) = msg match {
    case Message(_, "PING", _, x) => send("PONG", List.empty, x)
    case _ => ()
  }
}