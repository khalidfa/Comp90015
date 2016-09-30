package chatRoom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerState {

	private static ServerState instance;
	private List<ClientConnection> connectedClients;
	private List<ServerConnection> connectedServers;
	
	
	
	private ServerState() {
		connectedClients = new ArrayList<>();
		connectedServers = new ArrayList<>();
	}
	
	public static synchronized ServerState getInstance() {
		if(instance == null) {
			instance = new ServerState();
		}
		return instance;
	}
	
	public synchronized void clientConnected(ClientConnection client) {
		connectedClients.add(client);
	}
	
	public synchronized void DisconnectClient(ClientConnection client){
		
		Iterator<ClientConnection> iter = connectedClients.iterator();
		while (iter.hasNext()) {
			ClientConnection str = iter.next();
		    if(str.equals(client))
		        iter.remove();
		}
		
	}
	public synchronized void clientDisconnected(ClientConnection client) {
		connectedClients.add(client);
	}
	
	public synchronized List<ClientConnection> getConnectedClients() {
		return connectedClients;
	}

	public synchronized void ServerConnected(ServerConnection server) {
		connectedServers.add(server);
		
	}

	public synchronized List<ServerConnection> getConnectedServers() {
		return connectedServers;
	}
	
}
