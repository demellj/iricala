/* The generic IRC Message as received from the server */
case class Message( from     : Option[Source]
                  , command  : String
                  , args     : List[String]
                  , trailing : Option[String] )
