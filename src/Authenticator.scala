trait Authenticator extends UserInfo with Core { thisAuth =>
  private var needsAuth : Boolean = true;

  private var _userName : String = "user"
  private var _nickName : String = "user"
  private var _userMode : String = "user"
  private var _realName : String = "user"

  private val authenticationHandler = new Handler {
    override def onMessage(msg: Message) = msg match {
      case Message(_, "NOTICE", List("AUTH"), _) => if (needsAuth) {
        thisAuth.send("REGISTER");
        thisAuth.send("USER", List(userName, userMode, "*"), Some(realName));
        thisAuth.send("NICK", List(nickName));
      }
      needsAuth = false
      case _ => ()
    }
  };

  {// Register Authentication Handler
    thisAuth += authenticationHandler;
  }

  def userName = _userName;
  def userName_(un : String) = _userName = un;

  def nickName = _nickName;
  def nickName_(nn : String) = _nickName = nn;

  def userMode = _userMode;
  def userMode_(um : String) = _userMode = um;

  def realName = _realName;
  def realName_(rn : String) = _realName = rn;
}