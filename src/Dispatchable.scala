import scala.collection.mutable.ListBuffer
import scala.actors.Actor

trait Dispatchable {
    protected def listeners : Iterable[Handler]
    
    protected def dispatcher : Actor
    
	def +=(f : Message => Unit) : Unit
	
	def +=(h : Handler) : Unit
	
	def -=(h : Handler) : Unit
}