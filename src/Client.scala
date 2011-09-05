class Client(private val numOfWorkers: Int) 
	extends Core 
	    with DefaultDispatcher
	    with DefaultAuthenticator 
        with DefaultConnector 
        with DefaultCommunicator
{
  override protected val numWorkers = numOfWorkers;
}
