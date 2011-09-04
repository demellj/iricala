trait CollectiveResponsive {
  def atomicSend(f : Responsive => Unit) : Unit
}