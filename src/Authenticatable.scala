trait Authenticatable {
  def userName : String
  def userName_(un : String) : Unit

  def nickName : String
  def nickName_(nn : String) : Unit

  def userMode : String
  def userMode_(um : String) : Unit

  def realName : String
  def realName_(rn : String) : Unit
  
  def onAuthenticated : Unit
}