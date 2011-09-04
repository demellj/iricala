import scala.collection.immutable.List
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.SynchronousQueue
import java.io.BufferedReader
import java.io.PrintWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

trait DefaultCommunicator extends Communicable with Connectable with Dispatchable {
  private val sendLock = new ReentrantLock

  private val sendqueue = new SynchronousQueue[String]
    
  private var in  : BufferedReader = null;
  private var out : PrintWriter    = null;
    
  private def sendString(m : String) : Unit = {sendqueue put m}
    
  private val _sender = new Thread {
    override def run = while (true) {
      val line = sendqueue take;
      
      out println (line + "\r\n");
      out flush;
    }
  }

  private val _reader = new Thread {
    override def run = while (true) {
      val line = in readLine;
      dispatcher ! MessageParser.parseString(line + "\r\n").get
    }
  }

  override def sender  = _sender

  override def receiver = _reader
    
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
  
  override def setupCommunicator = {
    if (inputStream != null && outputStream != null) {
      in  = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))
      out = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"))
    }
  }
}