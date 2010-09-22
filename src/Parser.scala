import scala.util.parsing.combinator.Parsers

trait Source

abstract class Remote extends Source
case class DomainName(name : String) extends Remote
case class IP4Addr(addr : String)    extends Remote
case class IP6Addr(addr : String)    extends Remote

case class Message( from     : Option[Source]
                  , command  : String
                  , args     : List[String]
                  , trailing : Option[String] )

abstract class Target
case class Person( nickname  : Option[String]
                 , username  : Option[String]
                 , host      : Option[Remote] 
                 , server    : Option[Remote]) extends Target with Source
case class Channel( name     : String
                  , suffix   : Option[String] ) extends Target


abstract class BaseParser extends Parsers {
    type Elem = Char

    protected def oneOf(ps : Iterable[Elem]) = 
        elem("oneOf(" ++ ps.mkString ++ ")", ch => ps exists (ch ==))

    protected def allExcept(ps : Iterable[Elem]) = 
        elem("allExcept(" ++ ps.mkString ++ ")", ch => ps forall (ch !=))

    protected def servername = host

    protected def host = hostaddr | hostname
    
    protected def hostname = {
    	val shortname = (letter | number) ~ rep(letter | number | '-') ~ rep(letter | number) ^^
    		{ case c ~ as ~ bs =>  (c :: as ++ bs) mkString }
    	
    	val d_shortname = '.' ~ shortname ^^ { case _ ~ sn => "." ++ sn }
    	
    	shortname ~ rep(d_shortname)  ^^ 
    		{ case sn ~ rest => DomainName(sn ++ (rest mkString)) }
    }
    
    protected def hostaddr = {
    	val d_number = '.' ~ rep1(number) ^^ 
    		{ case _ ~ n => "." ++ n }
    	val ip4addr  = rep1(number) ~ rep1(d_number) ^^ 
    		{ case s ~ ss => (s ++ ss) mkString }
    	
    	val hexdigit = number | oneOf("aAbBcCdDeEfF")
    	val c_hexdigit = ':' ~ rep1(hexdigit) ^^ 
    		{ case _ ~ d => ":" ++ d}
    	
    	val ip6addr_plain  = rep1(hexdigit) ~ rep1(c_hexdigit) ^^
    		{ case d ~ ds => (d ++ ds) mkString }
    	val ip6addr_ip4    = acceptSeq("0:0:0:0:0:") ~ (acceptSeq("0") | acceptSeq("FFFF")) ~ ':' ~ ip4addr ^^
    		{ case a ~ b ~ _ ~ d =>  ((a ++ b) mkString) ++ ":" ++ (d mkString)}
    	
    	val ip6addr        = ip6addr_ip4 ||| ip6addr_plain
    	
    	ip4addr ^^ 
    		{ s => IP4Addr(s) } |
    	ip6addr ^^
    		{ s => IP6Addr(s) }
    }
    
    protected def user     = rep1(allExcept(" \0\r\n@%!")) ^^ {_ mkString}

    protected def nick     = {
        def special  = oneOf("_[]\\|`^{}")

        (letter | special) ~ rep(letter | number | special | '-') ^^
            { case l ~ ls =>
                (l :: ls) mkString
            }
    }

    protected def space = rep1(' ') ^^ {_ mkString}

    protected def number = oneOf("0123456789")

    protected def uppercase = oneOf("ABCDEFGHIJKLMNOPQRSTUVWXYZ")

    protected def lowercase = oneOf("abcdefghijklmnopqrstuwxyz")

    protected def letter = uppercase | lowercase

    protected def crlf = acceptSeq("\r\n")

    type ResultType

    def parse : Parser[ResultType]

    def parseString(s : String) = {
        import scala.util.parsing.input.CharSequenceReader;
        parse(new CharSequenceReader(s))
    }

}

object MessageParser extends BaseParser {
    private def message = {
        val c_prefix_s = ':' ~ prefix ~ space ^^ { case _ ~ p ~ _ => p }

        opt(c_prefix_s) ~ command ~ params ~ crlf  ^^
            { case oPrfx ~ cmds ~ ((ls, otr)) ~ _ => Message(oPrfx, cmds, ls, otr) }
    }

    private def prefix : Parser[Source]  = {
        val e_user = '!' ~ user ^^ { case _ ~ u => u }
        val a_host = '@' ~ host ^^ { case _ ~ h => h }

        servername ||| nick ~ opt(e_user) ~ opt(a_host)  ^^
            { case nick ~ oUser ~ oHost => 
            	Person(Some(nick), oUser, oHost, None) 
            }
    }

    private def command =
        rep1(letter) ^^ 
            { _ mkString
            } |
        number ~ number ~ number ^^ 
            { case a ~ b ~ c => 
                (a :: b :: c :: Nil) mkString 
            }

    private def params : Parser[(List[String],Option[String])] = {
        val cln_trailing = 
            ':' ~ trailing  ^^ { case _ ~ t => (Nil, Some(t)) }

        val middle_params = 
            middle ~ params ^^ { case p ~ ((ps, t)) => (p :: ps, t)}

        opt(space) ~ opt(cln_trailing | middle_params)  ^^ 
            { case _ ~ rest => 
                rest getOrElse (Nil, None)
            }
    }

    private def middle   = allExcept(" :\r\n\0") ~ rep(allExcept(" \r\n\0"))  ^^
        { case c ~ cs => 
            (c :: cs) mkString
        }

    private def trailing = rep(allExcept("\r\n\0")) ^^ {_ mkString}
    
    type ResultType = Message

    override def parse = phrase(message)
}

object TargetParser extends BaseParser {
    private def target : Parser[List[Target]] = {
        val sep_to = ',' ~ to ^^ { case _ ~ s => s}

        to ~ rep(sep_to) ^^ { case r ~ rs => r :: rs }
    }

    private def to : Parser[Target] = {
        val p_host = '%' ~ host ^^ 
         	{ case _ ~ h => h }

        val a_servername = '@' ~ servername ^^ 
        	{ case _ ~ sn => sn} 

        val e_user_a_host = '!' ~ user ~ '@' ~ host ^^ 
            { case _ ~ u ~ _ ~ h => (u, h) }

        val user_server = user ~ opt(p_host) ~ a_servername ^^
            { case u ~ oh ~ sn => Person(None, Some(u), oh, Some(sn)) }

        val user_host = user ~ p_host  ^^
            { case u ~ h => Person(None, Some(u), Some(h), None) }

        val nick_optqualuser = nick ~ opt(e_user_a_host)  ^^
            { case n ~ opt => opt match {
                case Some((u,h)) => Person(Some(n), Some(u), Some(h), None)
                case None        => Person(Some(n), None, None, None)
            }}

        channel | targetmask | (nick_optqualuser ||| user_server ||| user_host)
    }

    private def targetmask = failure("fail") ^^ {_ => Channel("a" ++ "foo", None) }

    private def channelid = uppercase | number 

    private def channel = {
        val e_channelid = '!' ~ channelid ^^ { case _ ~ cid => cid }
        val c_chstring  = ':' ~ chstring  ^^ { case _ ~ cs  => cs  }

        (oneOf("#+&") | e_channelid) ~ chstring ~ opt(c_chstring)  ^^
            { case a ~ b ~ c => Channel(a.toString ++ b, c) }
    }

    private def chstring = rep1(allExcept(" \f\0\r\n,:")) ^^ {_ mkString}

    type ResultType = List[Target]

    override def parse = phrase(target)
}
