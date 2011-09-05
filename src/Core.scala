import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.SynchronousQueue

trait Core 
    extends Dispatchable
       with Connectable
       with Identifiable
       with Communicable
{ thisCore =>
  
  private val shutdownHandler : Handler = new Handler {
    override def onLinkClosed = {
      thisCore -= shutdownHandler;
      shutdownCommunicator;
      shutdownDispatcher;
      shutdownIdentifier;
      shutdownConnector;
    }
  }
  
  trait EnableHandler extends UserInfo with Responsive with CollectiveResponsive {
    def userMode = thisCore.userMode
    def realName = thisCore.realName
    def nickName = thisCore.nickName
    def userName = thisCore.userMode

    def send(cmd: String, args: List[String], trailing: Option[String]) = thisCore.send(cmd, args, trailing)
    	
    def atomicSend(f : Responsive => Unit) = thisCore.atomicSend(f)
  }
  
  override def onIdentified = ()
  
  override def onConnected = {
    if (inputStream != null && outputStream != null) {
      setupIdentifier;
      setupCommunicator;
      setupDispatcher;
      thisCore += shutdownHandler;
    }
  }
}
