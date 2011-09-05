import java.io.InputStream
import java.io.FilterInputStream;
import java.io.InterruptedIOException

class InterruptibleInputStream(in : InputStream, pollDelay : Int, timeout : Int) extends FilterInputStream(in) {
  def this(in : InputStream, pollDelay : Int) = this(in, pollDelay, -1)
  
  def this(in : InputStream) = this(in, 150, -1)
  
  override def read() : Int = {
    if (waitTillAvailable)
      super.read;
    else
      throw new InterruptedIOException;
  }
  
  override def read(b : Array[Byte], off : Int, len : Int) : Int = {
    if (waitTillAvailable)
      super.read(b, off, len);
    else
      throw new InterruptedIOException;
  }
  
  private def waitTillAvailable : Boolean = {
    val startTime = System.currentTimeMillis;
    var cont = true;
    while (cont && in.available() < 0) {
      try { 
        Thread sleep pollDelay 
      } catch { 
        case ie : InterruptedException => {cont = false;} 
      }
      if (timeout != -1 && System.currentTimeMillis - startTime > timeout)
        cont = false;
    }
    cont;
  }
}