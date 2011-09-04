class Client(private val numWorkers: Int) extends Core with Authenticator { thisClient =>
  override protected val workers = java.util.concurrent.Executors.newFixedThreadPool(numWorkers);

  trait EnableHandler extends UserInfo with Responsive {
    def userMode = thisClient.userMode
    def realName = thisClient.realName
    def nickName = thisClient.nickName
    def userName = thisClient.userMode

    def send(cmd: String, args: List[String], trailing: Option[String]) = thisClient.send(cmd, args, trailing)
  }
}
