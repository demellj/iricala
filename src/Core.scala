import java.util.concurrent._;
import java.io.PrintWriter
import java.io.BufferedReader;
import java.net._;
import java.nio.channels._;
import locks.ReentrantLock
import scala.actors._;
import scala.collection.mutable._;

trait Core extends Responsive with CollectiveResponsive {
    private var sockchan : SocketChannel = null;
    private var in : BufferedReader = null;
    private var out : PrintWriter = null;

    protected val workers : ExecutorService;

    private val sendLock = new ReentrantLock;

    private val sendqueue = new SynchronousQueue[String];
    
    private var listeners = new ListBuffer[Handler];
    
    private def sendString(m : String) : Unit = {sendqueue put m}
    
    private val sender = new Thread {
      override def run = while (true) {
        val line = sendqueue take;
        
        out println (line + "\r\n");
        out flush;
      }
    }

    private val reader = new Thread {
      override def run = while (true) {
        val line = in readLine;
        dispatcher ! MessageParser.parseString(line + "\r\n").get
      }
    }

    private val dispatcher = new Actor {
      def act = loop { receive {
        case msg : Message => listeners foreach { x =>
          workers.execute(new Runnable {
            def run() = x onMessage msg;
          })
        }
        case _ => ()
      }}
    }

    override def atomicSend(f : Responsive => Unit) : Unit = {
      sendLock.lock(); try {
        f(this);
      } finally {
        sendLock.unlock();
      }
    }

    override def send(cmd: String, args: List[String], trailing: Option[String]) = {
      val argsToString =
        if (args != null && args.length > 0)
          " " + args.mkString("", " ", "")
        else
          "";
      val trailingToString =
        if (trailing != null && !trailing.isEmpty)
          " :" + trailing.get
        else
          "";
      sendLock.lock(); try {
        sendString(cmd + argsToString + trailingToString)
      } finally {
        sendLock.unlock();
      }
    }

    def +=(f : Message => Unit) = {
      listeners += new Handler {
        override def onMessage(msg : Message) = f(msg);
      };
      ()
    }

    def +=(h : Handler) : Unit = {
      listeners += h;
      ()
    }
    
    def -=(h : Handler) : Unit = {
      listeners -= h;
      ()
    }

    def connect(server : String) : Unit = connect(server, 6667)

    def connect(server : String, port : Int) : Unit = {
      sockchan = SocketChannel.open;
      sockchan.socket.connect(new InetSocketAddress(server, port));
      in = new BufferedReader(Channels.newReader(sockchan, "UTF-8"));
      out = new PrintWriter(Channels.newWriter(sockchan, "UTF-8"));

      sender start;
      dispatcher start;
      reader start;
        
      ()
    }
}
