package client;

public class ClientThread extends Thread {

	NewClient client;


	public ClientThread(String serviceName, String serviceHost, int servicePort){
		try{
			this.client = new NewClient(serviceName,serviceHost,servicePort);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void run(){
		while(true){
			
			client.run();


			try{
				Thread.sleep(5000);

			} catch(InterruptedException e) {
            	e.printStackTrace();
			}
		}
	}
}