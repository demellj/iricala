import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

import scala.actors.Actor
import scala.collection.mutable.ListBuffer

trait DefaultDispatcher extends Dispatchable {
  private object CloseActor;
   
  private val _listeners = new ListBuffer[Handler];
    
  private var _dispatcher : Actor = null;
    
  private var workers : ExecutorService = null;
	
  protected val numWorkers : Int;
    
  override protected def shutdownDispatcher = {
    dispatcher ! CloseActor;
    _dispatcher = null;
    workers.shutdown();
    workers = null;
  }

  override protected def setupDispatcher = {
    workers = Executors.newFixedThreadPool(numWorkers);
    _dispatcher = new Actor {
      def act = loop { receive {
        case msg : Message => _listeners foreach { x => dispatch {x onMessage msg} }
        case LinkClosed    => _listeners foreach { x => dispatch {x onLinkClosed } }
        case CloseActor    => exit;
        case _ => ()
      }}
    }
    dispatcher start;
  }

  override def +=(h : Handler) : Unit = {
    _listeners += h;
    ()
  }
    
  override def -=(h : Handler) : Unit = {
    _listeners -= h;
    ()
  }
  
  override def contains(h : Handler) = _listeners contains h;
    
  override protected def dispatcher = _dispatcher;
    
  private def dispatch(f : Unit) = {
    try {
      if (workers != null)
        workers execute new Runnable { def run() = f }
    } catch {
      case ree : RejectedExecutionException => ()
    }
  }
}