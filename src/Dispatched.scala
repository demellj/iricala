trait Dispatched

/* The generic IRC Message as received from the server */
case class Message( from     : Option[Source]
                  , command  : String
                  , args     : List[String]
                  , trailing : Option[String] ) extends Dispatched

/* Issued when the remote server closes the link 
 *   or when a local disconnect is requested */
object LinkClosed extends Dispatched
