import java.util.concurrent.SynchronousQueue
import java.io.PrintWriter
import java.io.BufferedReader;
import java.net._;
import java.nio.channels._;
import scala.actors._;
import scala.collection.mutable._;

trait Core extends Responsive {
    private var sockchan : SocketChannel = null;
    private var in : BufferedReader = null;
    private var out : PrintWriter = null;

    private var sendqueue = new SynchronousQueue[String];
    
    private var listeners = new ListBuffer[Handler];
    
    private def sendString(m : String) = {sendqueue put m}
    
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
        case msg : Message => listeners foreach {_ onMessage msg}
        case _ => ()
      }}
    }

    def send(cmd: String, args: List[String], trailing: Option[String]) = {
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
      sendString(cmd + argsToString + trailingToString )
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
