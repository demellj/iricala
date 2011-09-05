trait Identifiable extends UserInfo {
  protected def setupIdentifier : Unit
  
  protected def shutdownIdentifier : Unit
  
  protected def onIdentified : Unit  
}