import scala.collection.mutable.ListBuffer
import scala.actors.Actor

trait Dispatchable {
  protected def dispatcher : Actor
    
  def +=(f : Message => Unit) : Unit = {
    this += new Handler {
      override def onMessage(msg : Message) = f(msg)
    }
  }

  def +=(h : Handler) : Unit
	
  def -=(h : Handler) : Unit
  
  def contains(h : Handler) : Boolean
	
  protected def setupDispatcher : Unit
	
  protected def shutdownDispatcher : Unit
}