package chatRoom.chatClient;

public class State {

	private String identity;
	private String roomId;
	private String username;
	private String password;
	
	public State(String identity, String roomId,String username,String password) {
		this.identity = identity;
		this.roomId = roomId;
		this.username = username;
		this.password = password;
		
	}
	
	public synchronized String getRoomId() {
		return roomId;
	}
	public synchronized void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	
	public synchronized void setUsername(String username) {
		this.username = username;
	}	
	
	public synchronized String getUsername() {
		return username;
	}
	public synchronized void setPassword(String password) {
		this.password = password;
	}
	public synchronized String getPassword() {
		return password;
	}
	
	public String getIdentity() {
		return identity;
	}
	
	
}
