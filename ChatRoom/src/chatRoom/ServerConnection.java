package chatRoom;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ServerConnection extends Thread {
	Socket serverSocket;
	private BufferedReader reader;
	private BufferedWriter writer;
	private String currentServerId;
	JSONObject msgJObj = null;
	ServerSocket listeningServerSocket = null;
	
	

	public  ServerConnection(ServerSocket listeningServerSocket , String currentServerId){
		try {
			this.currentServerId = currentServerId;
			this.listeningServerSocket = listeningServerSocket;
		
			this.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		try {
			
			JSONParser parser = new JSONParser();
			msgJObj = new JSONObject();
			String userIdentity = null;
			String roomId = null;
			boolean found = false;
			
			
			while(true) {
				
				Socket serverSocket = listeningServerSocket.accept();
				reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream(), "UTF-8"));
				writer = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream(), "UTF-8"));
				msgJObj = (JSONObject) parser.parse(reader.readLine());
				
				String s = (String) msgJObj.get("type");
				JSONObject lockIdentity = new JSONObject();
			
				if(s.equals("lockidentity")){	
					userIdentity= (String) msgJObj.get("identity");
					
					boolean foundInLockeIds = false;
					
					for(UserInfo user: Server.listOfusers){
						if(userIdentity.equals(user.username)){
							found = true;
							
						}
						
					}
					
					lockIdentity.put("type","lockidentity");
					lockIdentity.put("serverid",currentServerId);
					lockIdentity.put("identity",userIdentity);
					
					if(found){
						
						lockIdentity.put("locked","false");
						
					}else{
						
						for(UserInfo user: Server.listOflockedIds){
							if(userIdentity.equals(user.username)){
								foundInLockeIds = true;
								
							}
							
						}
						if(foundInLockeIds){
							lockIdentity.put("locked","false");
							
						}else{
							lockIdentity.put("locked","true");
							
						}
						
					}
					
					UserInfo user = new UserInfo();
					user.username = userIdentity;
					Server.listOflockedIds.add(user);
					writer.write(lockIdentity.toJSONString() + "\n");	
					writer.flush();
					
				}
				
				if(s.equals("releaseidentity")){
				
					userIdentity= (String) msgJObj.get("identity");
					
					Iterator<UserInfo> iter = Server.listOflockedIds.iterator();

					while (iter.hasNext()) {
					    UserInfo str = iter.next();

					    if(str.username.equals(userIdentity))
					        iter.remove();
					}
					
				}
				
				if(s.equals("lockroomid")){
				
					roomId= (String) msgJObj.get("roomid");
					JSONObject lockedRoomId = new JSONObject();
					boolean foundInLockedRooms = false;
					System.out.println("This is the room id = " +roomId);
					
					for(chatRoomInfo room: Server.listOfrooms){
						if(room.getchatRoomId().equals(roomId)){
							found = true;
							
						}
						
					}
					
					if (found){
						lockedRoomId.put("locked","false");	
					}else{
						
						
							for (chatRoomInfo room :Server.listOfLockedRooms){
								if(room.chatRoomId.equals(roomId)){
									foundInLockedRooms = true;
								}
							}
							if(foundInLockedRooms){
								lockedRoomId.put("locked","false");
							}else{
								lockedRoomId.put("locked","true");
							}
					}
					
					chatRoomInfo room = new chatRoomInfo();
					room.chatRoomId = roomId;
					room.owner = "";
					room.serverId =currentServerId;
					Server.listOfLockedRooms.add(room);
					writer.write(lockedRoomId.toJSONString() + "\n");	
					writer.flush();
					
				}
				
				if(s.equals("releaseroomid")){
					roomId= (String) msgJObj.get("roomid");
					String serverId = (String) msgJObj.get("serverid");
					String approval = (String) msgJObj.get("approved");
					
					Iterator<chatRoomInfo> iter = Server.listOfLockedRooms.iterator();

					while (iter.hasNext()) {
					    chatRoomInfo str = iter.next();

					    if(str.chatRoomId.equals(roomId))
					        iter.remove();
					}
					
					if(approval.equals("true")){
						
						chatRoomInfo room = new chatRoomInfo();
						
						room.chatRoomId = roomId;
						room.serverId = serverId;
						room.owner = "";
						Server.listOfrooms.add(room);
					}
				}
				
				if(s.equals("deleteroom")){
					String deletedRoom = (String) msgJObj.get("roomid");
					
					Iterator<chatRoomInfo> iter = Server.listOfrooms.iterator();
					while (iter.hasNext()) {
					    chatRoomInfo str = iter.next();
					    if(str.chatRoomId.equals(deletedRoom))
					        iter.remove();
					}
				
				}
			
				System.out.println(s);
				System.out.println("Message: " + msgJObj);
			
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
		
	}
}



