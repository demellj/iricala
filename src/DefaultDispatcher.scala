import scala.collection.mutable.ListBuffer
import scala.actors.Actor
import java.util.concurrent.ExecutorService

trait DefaultDispatcher extends Dispatchable {
    private var _listeners = new ListBuffer[Handler]
    
    override def listeners = _listeners
    
    protected val workers : ExecutorService;

    override def +=(f : Message => Unit) = {
      _listeners += new Handler {
        override def onMessage(msg : Message) = f(msg);
      };
      ()
    }

    override def +=(h : Handler) : Unit = {
      _listeners += h;
      ()
    }
    
    override def -=(h : Handler) : Unit = {
      _listeners -= h;
      ()
    }
    
    override protected val dispatcher = new Actor {
      def act = loop { receive {
        case msg : Message => listeners foreach { x =>
          workers.execute(new Runnable {
            def run() = x onMessage msg;
          })
        }
        case _ => ()
      }}
    }
}