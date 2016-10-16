package chatRoom;

public class ServerInfo  {
	
private String serverId;
private String serverAddress;
private int clientsPort;
private int serversPort;
private String isNew;
	 ServerInfo(String Id, String Address, int cPort ,int sPort){
		this.serverId = Id;
		this.serverAddress = Address;
		this.clientsPort = cPort;
		this.serversPort = sPort;
		
	}
	
	 public ServerInfo(String Id, String Address, int cPort ,int sPort,String isNew){
		 	
		 	this.serverId = Id;
			this.serverAddress = Address;
			this.clientsPort = cPort;
			this.serversPort = sPort;
			this.isNew = isNew;
		 
	 }
	
	 public   String getServerId(){
			return serverId;
		}
	 public   String getServerAddress(){
			return serverAddress;
		}
	 public   int getClientsPort(){
			return clientsPort;
		}
	 public   int getServersPort(){
			return serversPort;
		}
	
	public String getIsNew() {
		return isNew;
	}

	public void setIsNew(String isNew) {
		this.isNew = isNew;
	}
}
