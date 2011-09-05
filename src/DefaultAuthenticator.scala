trait DefaultAuthenticator 
	extends UserInfo with Dispatchable
	                  with Responsive
	                  with Authenticatable
{ thisAuth =>
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
        needsAuth = false
        onAuthenticated
      }
      case _ => ()
    }
  };

  {// Register Authentication Handler
    thisAuth += authenticationHandler;
  }

  override def userName = _userName;
  override def userName_(un : String) = _userName = un;

  override def nickName = _nickName;
  override def nickName_(nn : String) = _nickName = nn;

  override def userMode = _userMode;
  override def userMode_(um : String) = _userMode = um;

  override def realName = _realName;
  override def realName_(rn : String) = _realName = rn;
  
  override protected def setupAuthenticator = ()
  
  override protected def shutdownAuthenticator = {
    needsAuth = true; ();
  }
}