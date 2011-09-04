class Client(private val numWorkers: Int) 
	extends Core 
	    with DefaultDispatcher
	    with DefaultAuthenticator 
        with DefaultConnector 
        with DefaultCommunicator
{
  override protected val workers = java.util.concurrent.Executors.newFixedThreadPool(numWorkers);
}
