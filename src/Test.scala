object Test {
    def main(s : Array[String]) = {
      val client = new Client("foo", "foo", "0", "foo bar")

      client += new PingResponder with client.EnableHandler
      client += new Authenticator with client.EnableHandler

      client += new Handler {
        override def onMessage(msg : Message) = Console println msg; ()
      }

      client connect "localhost"
      
      //Console println (MessageParser.parseString(":my.server.name 001 test :Welcome to the Internet Relay Network test\r\n"))
    }
}
