import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.channels.Channels
import java.nio.channels.SocketChannel

trait DefaultConnector extends Connectable {
  private var _sockchan : SocketChannel  = null;
  private var _inputStream : InputStream = null;
  private var _outputStream : OutputStream = null;
    
  def connect(server : String) : Unit = connect(server, 6667)

  def connect(server : String, port : Int) : Unit = {
    _sockchan = SocketChannel.open;
    _sockchan.socket.connect(new InetSocketAddress(server, port));
    _inputStream = Channels.newInputStream(_sockchan);
    _outputStream = Channels.newOutputStream(_sockchan);

    onConnected;
    ()
  }
    
  override def inputStream = _inputStream
    
  override def outputStream = _outputStream
}