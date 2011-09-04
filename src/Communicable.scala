import scala.collection.immutable.List

trait Communicable extends Responsive with CollectiveResponsive {
    def sender   : {def start() : Unit}
    
    def receiver : {def start() : Unit}
    
    def setupCommunicator : Unit
}