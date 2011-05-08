/* A Source is the end-point the Message is sent from. */
trait Source

/* A Target is the end-point the Message is addressed to */
trait Target

/* A Remote is a source identifiable by an IP Address or Domain Name */
abstract class Remote extends Source
case class DomainName(name : String) extends Remote
case class IP4Addr(addr : String)    extends Remote
case class IP6Addr(addr : String)    extends Remote

/* A Person can be both a Target and a Source */
case class Person( nickname  : Option[String]
                 , username  : Option[String]
                 , host      : Option[Remote]
                 , server    : Option[Remote] ) extends Target with Source

/* Channels are only Targets */
case class Channel( name     : String
                  , suffix   : Option[String] ) extends Target