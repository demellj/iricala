import java.io._;
import java.net._;

trait Connectable {
    protected def inputStream  : InputStream
    
    protected def outputStream : OutputStream
    
    def isConnected : Boolean
    
    protected def onConnected : Unit
    
    protected def closeConnection : Unit
}