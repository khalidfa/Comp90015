package chatRoom;

import org.json.simple.JSONObject;

public class Message {

	//True if the message comes from a client, false if it comes from a thread
	private boolean isFromClient;

	private JSONObject message;
	
	public Message(boolean isFromClient, JSONObject message) {
		super();
		this.isFromClient = isFromClient;
		this.message = message;
	}
	
	public boolean isFromClient() {
		return isFromClient;
	}
	public JSONObject getMessage() {
		return message;
	}
	
	
}
