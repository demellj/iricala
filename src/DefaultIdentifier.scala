trait DefaultIdentifier 
    extends Identifiable
       with Dispatchable
       with Responsive
{ thisIdent =>
  private var needsAuth : Boolean = true;

  private var _userName : String = "user"
  private var _nickName : String = "user"
  private var _userMode : String = "user"
  private var _realName : String = "user"

  private val identificationHandler = new Handler {
    override def onMessage(msg: Message) = msg match {
      case Message(_, "NOTICE", List("AUTH"), _) => 
        identify;
      case Message(_, "NOTICE", List("*"), Some("*** Checking Ident")) =>
        identify;
      case _ => ()
    }
  };

  private def identify = {
    if (needsAuth) {
      thisIdent.send("REGISTER");
      thisIdent.send("USER", List(userName, userMode, "*"), Some(realName));
      thisIdent.send("NICK", List(nickName));
      needsAuth = false
      onIdentified
    }
  }

  {// Register Identification Handler
    thisIdent += identificationHandler;
  }

  override def userName = _userName;
  def userName_(un : String) = _userName = un;

  override def nickName = _nickName;
  def nickName_(nn : String) = _nickName = nn;

  override def userMode = _userMode;
  def userMode_(um : String) = _userMode = um;

  override def realName = _realName;
  def realName_(rn : String) = _realName = rn;
  
  override protected def setupIdentifier = ()
  
  override protected def shutdownIdentifier = {
    needsAuth = true; ();
  }
}
