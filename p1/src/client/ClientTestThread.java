package client;

public class ClientTestThread extends Thread{
	
	
	Client client;
	String clientId;
	public ClientTestThread(Client client) {
		this.client = client;
		this.clientId = client.handleRequest("newcustomer,1");
	}

	public void run() {
		while (true){
			
			if(!client.handleRequest("itinerary,0,"+clientId +",0,0,true,true").equals("true")){
				break;
			};
			
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		client.handleRequest("deletecustomer,0,"+clientId);
	}


}
