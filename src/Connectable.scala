import java.io._;
import java.net._;

trait Connectable {
    def inputStream : InputStream
    
    def outputStream : OutputStream
    
    def onConnected : Unit
}