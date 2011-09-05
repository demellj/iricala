trait Handler {
  def onMessage(msg : Message) : Unit = ()

  def onLinkClosed : Unit = ()
}