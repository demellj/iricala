trait Authenticatable extends UserInfo {
  def userName_(un : String) : Unit

  def nickName_(nn : String) : Unit

  def userMode_(um : String) : Unit

  def realName_(rn : String) : Unit
  
  protected def setupAuthenticator : Unit
  
  protected def shutdownAuthenticator : Unit
  
  protected def onAuthenticated : Unit  
}