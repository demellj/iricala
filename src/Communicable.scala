import scala.collection.immutable.List

trait Communicable extends Responsive with CollectiveResponsive {
  protected def setupCommunicator : Unit
    
  protected def shutdownCommunicator : Unit
}