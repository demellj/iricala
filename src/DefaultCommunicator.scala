import scala.collection.immutable.List
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.SynchronousQueue
import java.io.BufferedReader
import java.io.PrintWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.InterruptedIOException

trait DefaultCommunicator extends Communicable with Connectable with Dispatchable {
  private val sendLock = new ReentrantLock

  private val sendqueue = new SynchronousQueue[String]
    
  private var in  : BufferedReader = null;
  private var out : PrintWriter    = null;
    
  private def sendString(m : String) : Unit = {sendqueue put m}
  
  private var _sender   : Thread = null;
  private var _receiver : Thread = null;
  
  private var running = true;

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
  
  override protected def setupCommunicator = {
    if (inputStream != null && outputStream != null) {
      in  = new BufferedReader(new InputStreamReader(new InterruptibleInputStream(inputStream), "UTF-8"))
      out = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"))
      _sender = new Thread {
        override def run = while (running) {
          try {
            val line = sendqueue take;
        
            if (line != null && line.length() > 0) {
              out println (line + "\r\n");
              out flush;
            }
          } catch {
            case ie : InterruptedException => { running = false; }
          }
        }
      }
      _sender start;
      _receiver = new Thread {
        override def run = while (running) {
          try {
            val line = in readLine;
            if (line != null && line.length() > 0)
              dispatcher ! MessageParser.parseString(line + "\r\n").get;
            else
              dispatcher ! LinkClosed
          } catch {
            case iioe : InterruptedIOException => { running = false; }
          }
        }
      }
      _receiver start;
    }
  }
  
  override protected def shutdownCommunicator = {
    running = false;
    _sender interrupt;
    _receiver interrupt;
    _sender = null;
    _receiver = null;
    in  = null;
    out = null;
  }
}
