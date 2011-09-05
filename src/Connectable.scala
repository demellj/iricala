import java.io._;
import java.net._;

trait Connectable {
  protected def inputStream  : InputStream
    
  protected def outputStream : OutputStream
    
  protected def onConnected : Unit
    
  protected def shutdownConnector : Unit
}