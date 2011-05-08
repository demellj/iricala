class Client(private var _userName : String,
             private var _nickName : String,
             private var _userMode : String,
             private var _realName : String) extends Core {

  private val thisClient : Client = this

  def userName = _userName
  def userName_(un : String) = _userName = un;

  def nickName = _nickName
  def nickName_(nn : String) = _nickName = nn;

  def userMode = _userMode
  def userMode_(um : String) = _userMode = um;

  def realName = _realName
  def realName_(rn : String) = _realName = rn;

  trait EnableHandler extends UserInfo with Responsive {
    def userMode = thisClient.userMode
    def realName = thisClient.realName
    def nickName = thisClient.nickName
    def userName = thisClient.userName

    def send(cmd: String, args: List[String], trailing: Option[String]) = thisClient.send(cmd, args, trailing)
  }
}
