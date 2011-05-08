
trait Responsive {
  def send(cmd : String, args : List[String], trailing : Option[String]) : Unit
  def send(cmd : String, args : List[String]) : Unit = send(cmd, args, None)
  def send(cmd : String) : Unit = send(cmd, List.empty, None)
}
