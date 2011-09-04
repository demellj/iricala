object Test {
    def main(s : Array[String]) = {
      val client = new Client(8)

      client nickName_ "nickName";
      client userName_ "userName";
      client realName_ "realName";
      client userMode_ "t";

      client += new PingResponder with client.EnableHandler

      client += {msg : Message => Console println msg}

      client connect "localhost"
      
      //Console println (MessageParser.parseString(":my.server.name 001 test :Welcome to the Internet Relay Network test\r\n"))
    }
}
