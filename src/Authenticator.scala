
trait Authenticator extends ResponsiveHandler with UserInfo {
  private var needsAuth : Boolean = true;

  def onMessage(msg: Message) = msg match {
    case Message(_, "NOTICE", List("AUTH"), _) => if (needsAuth) {
      send("REGISTER");
      send("USER", List(userName, userMode, "*"), Some(realName));
      send("NICK", List(nickName));
      needsAuth = false
    }
    case _ => ()
  }
}