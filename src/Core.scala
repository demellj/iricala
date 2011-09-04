import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.SynchronousQueue

trait Core 
	extends Dispatchable
	   with Connectable
	   with Authenticatable
	   with Communicable
{ thisCore =>
  
  trait EnableHandler extends UserInfo with Responsive with CollectiveResponsive {
    def userMode = thisCore.userMode
    def realName = thisCore.realName
    def nickName = thisCore.nickName
    def userName = thisCore.userMode

    def send(cmd: String, args: List[String], trailing: Option[String]) = thisCore.send(cmd, args, trailing)
    	
    def atomicSend(f : Responsive => Unit) = thisCore.atomicSend(f)
  }
  
  override def onAuthenticated = ()
  
  override def onConnected = {
    if (inputStream != null && outputStream != null) {
      setupCommunicator;
      sender start;
   	  dispatcher start;
      receiver start;
    }
  }
}
