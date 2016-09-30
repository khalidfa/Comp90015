package chatRoom;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MessageHandler {

	 private static BufferedReader reader;
	 private static BufferedWriter writer;
	 
	 static JSONObject login(String username,String password) throws ParseException{
		
		 JSONObject Authentication = new JSONObject();
		 Socket s;
		 JSONParser parser = new JSONParser();
		 String AuthApproval = "true";
		 
		 Authentication.put("type", "login");
		 Authentication.put("username", username);
		 
		 for(ServerInfo server : Server.listOfservers){
				if (!(server.getServerId().equals("AS"))){
					
					String hostName = server.getServerAddress();
					int serverPort = server.getServersPort();
				
					try{
						
						s = new Socket(hostName, serverPort);
						
						DataOutputStream out =new DataOutputStream( s.getOutputStream());
						DataInputStream in = new DataInputStream(s.getInputStream());
						System.out.println("Sending data");
						 
						 out.write((Authentication.toJSONString() + "\n").getBytes("UTF-8"));
						 out.flush();
						 
						 JSONObject message;
						
						 message = (JSONObject) parser.parse(in.readLine());
						
						 String authenticated = (String) message.get("approved");
						 System.out.println(authenticated);
						 
						 if(authenticated.equals("false")){
							  AuthApproval = "false";
						 }
					}catch (UnknownHostException e) {
						 System.out.println("Socket:"+e.getMessage());
						 }catch (EOFException e){
						 System.out.println("EOF:"+e.getMessage());
						 }catch (IOException e){
						 System.out.println("readline:"+e.getMessage());
						 }
				}
			
		 }
		 Authentication.put("authenticated", AuthApproval);
		 
		 return Authentication;
	 }
	 
	 static JSONObject deleteRoom(String id, String roomId,String serverId){
		 JSONObject dRoom = new JSONObject();
		 JSONObject deleteRoomServers = new JSONObject();
		 boolean found = false;
		 Socket s;
		 
		 for(chatRoomInfo room : Server.listOfrooms){
			 if((room.owner.equals(id))&&(room.chatRoomId.equals(roomId))){
				 found = true;
			 }
		 }
		 dRoom.put("type", "deleteroom");
		 dRoom.put("roomid", roomId);
		
		 if (found){
			 
			 dRoom.put("approved", "true");
			 
			 deleteRoomServers.put("type", "deleteroom");
			 deleteRoomServers.put("serverid", serverId);
			 deleteRoomServers.put("roomid", roomId);
			 
			 for(ServerInfo server : Server.listOfservers){
					if (!(server.getServerId().equals(serverId))){
						
						String hostName = server.getServerAddress();
						int serverPort = server.getServersPort();
					
						try{
							
							s = new Socket(hostName, serverPort);
							
							DataOutputStream out =new DataOutputStream( s.getOutputStream());
							System.out.println("Sending data");
							 
							 out.write((deleteRoomServers.toJSONString() + "\n").getBytes("UTF-8"));
							 out.flush();
						}catch (UnknownHostException e) {
							 System.out.println("Socket:"+e.getMessage());
							 }catch (EOFException e){
							 System.out.println("EOF:"+e.getMessage());
							 }catch (IOException e){
							 System.out.println("readline:"+e.getMessage());
							 }
					}
				
			 }
		 
		 }else{
		 
		 dRoom.put("approved", "false");
		 }
		 return dRoom;
		 
	 }
	 
	 static JSONObject moveJoin(String id ,String newRoom , String formerRoom, String currentServerId){
		JSONObject moveJoin = new JSONObject();
		boolean found = false;
		String serverId=null;
		for (chatRoomInfo room : Server.listOfrooms)
		{
			if(room.chatRoomId.equals(newRoom)){
				found = true;
			}
			if(room.chatRoomId.equals(formerRoom)){
				serverId = room.serverId;
			}
		}
		
		moveJoin.put("type", "serverchange");
		moveJoin.put("serverid", currentServerId);
		moveJoin.put("approved", "true");
		
		if (found){
			UserInfo user = new UserInfo();
			user.username = id;
			user.chatroom = newRoom;
			Server.listOfusers.add(user);
		}else{
			UserInfo user = new UserInfo();
			user.username = id;
			user.chatroom = "MainHall-"+currentServerId;
			Server.listOfusers.add(user);
		}
		
		
		return moveJoin;
	 }
	 
	 static JSONObject who(String roomId){
		 JSONObject whoList = new JSONObject();
		 JSONArray userList = new JSONArray();
		 String owner = null;
		
		for (UserInfo user : Server.listOfusers) {
			if(user.chatroom.equals(roomId)){
				userList.add(user.username);
				
			}
		}
		
		for(chatRoomInfo room : Server.listOfrooms){
			if(room.getchatRoomId().equals(roomId)){
				owner = room.getOwner();
			}
		}
		 
		 whoList.put("type", "roomcontents");
		 whoList.put("roomid", roomId);
		 whoList.put("identities", userList);
		 whoList.put("owner", owner);
		 return whoList;
	 }

	 static void releaseRoom(String roomId , String currentServerId,String approval){
		 Socket s =null;
			JSONObject releaseRoom = new JSONObject();
			
			
			releaseRoom.put("type", "releaseroomid");
			releaseRoom.put("serverid", currentServerId);
			releaseRoom.put("roomid", roomId);
			releaseRoom.put("approved", approval);
			
			for(ServerInfo server : Server.listOfservers){
				if (!(server.getServerId().equals(currentServerId))){
					
					String hostName = server.getServerAddress();
					int serverPort = server.getServersPort();
				
					try{
						
						s = new Socket(hostName, serverPort);
						
						DataOutputStream out =new DataOutputStream( s.getOutputStream());
						System.out.println("Sending data");
						 
						 out.write((releaseRoom.toJSONString() + "\n").getBytes("UTF-8"));
						 out.flush();
					}catch (UnknownHostException e) {
						 System.out.println("Socket:"+e.getMessage());
						 }catch (EOFException e){
						 System.out.println("EOF:"+e.getMessage());
						 }catch (IOException e){
						 System.out.println("readline:"+e.getMessage());
						 }
				}
			}
	 }
	 
	 static void releaseIdentity (String id ,String currentServerId){
		Socket s =null;
		JSONObject releaseIdnentity = new JSONObject();
	
		
		releaseIdnentity.put("type", "releaseidentity");
		releaseIdnentity.put("serverid", currentServerId);
		releaseIdnentity.put("identity", id);
		
		for(ServerInfo server : Server.listOfservers){
			if (!(server.getServerId().equals(currentServerId))){
				
				String hostName = server.getServerAddress();
				int serverPort = server.getServersPort();
			
				try{
					
					s = new Socket(hostName, serverPort);
					
					DataOutputStream out =new DataOutputStream( s.getOutputStream());
					System.out.println("Sending data");
					 
					 out.write((releaseIdnentity.toJSONString() + "\n").getBytes("UTF-8"));
					 out.flush();
				}catch (UnknownHostException e) {
					 System.out.println("Socket:"+e.getMessage());
					 }catch (EOFException e){
					 System.out.println("EOF:"+e.getMessage());
					 }catch (IOException e){
					 System.out.println("readline:"+e.getMessage());
					 }
			}
		}
	}
	
	
	static JSONObject changeRoomClass(String id, String chatRoom, String fRoom){
		
		String formerRoom = "";
		JSONObject roomChange = new JSONObject();
		if (fRoom.equals("")){
			for (UserInfo user:Server.listOfusers){
				if (user.getUsername().equals(id)){
					 formerRoom = user.getChatroom();
					 user.chatroom = chatRoom;
				}
			}
		}
		else{
			formerRoom = fRoom;
		}
		roomChange.put("type","roomchange");
		roomChange.put("identity",id );
		roomChange.put("former",formerRoom);
		roomChange.put("roomid",chatRoom);
		
		System.out.println("MEssage Handler: roomChange: " + roomChange);
		
		return roomChange;
		
	}
	
	
	static JSONObject newIdnetityClass(String identity , String chatRoom, String currentServerId) throws IOException{
		
		JSONObject newIdentityMessage = new JSONObject();
		JSONObject lockIdentity = new JSONObject();
		Socket s =null;
		JSONParser parser = new JSONParser();
	
		
		newIdentityMessage.put("type", "newidentity");
		boolean found = true;
		boolean sFound = false;
		String lockId = "true";
			
		if (identity.matches(("^[a-zA-Z]([a-zA-z0-9]){2,15}$"))){
		
        for(UserInfo user: Server.listOfusers){
    	    if (user.getUsername().equals(identity))
    	    {
    		    found =false;
    	  
    	    }
        }
       
		
		if (!found){
			lockId = "false";
			
			
		}else{
			lockIdentity.put("type", "lockidentity");
			lockIdentity.put("serverid", currentServerId);
			lockIdentity.put("identity", identity);
			
			for(ServerInfo server : Server.listOfservers){
				if (!(server.getServerId().equals(currentServerId))){
					
					String hostName = server.getServerAddress();
					int serverPort = server.getServersPort();
					System.out.println("The host name =" + hostName);
					System.out.println("The port =" + serverPort);
					try{
						
						s = new Socket(hostName, serverPort);
					
						DataInputStream in = new DataInputStream( s.getInputStream());
						DataOutputStream out =new DataOutputStream( s.getOutputStream());
						System.out.println("Sending data");
						 
						 out.write((lockIdentity.toJSONString() + "\n").getBytes("UTF-8"));
						 out.flush();

						 JSONObject message;
						 message = (JSONObject) parser.parse(in.readLine());
						
						 String locked = (String) message.get("locked");
						 System.out.println(locked);
						 
						 if(locked.equals("false")){
							 sFound = true;
						 }
						 
						 }catch (UnknownHostException e) {
						 System.out.println("Socket:"+e.getMessage());
						 }catch (EOFException e){
						 System.out.println("EOF:"+e.getMessage());
						 }catch (IOException e){
						 System.out.println("readline:"+e.getMessage());
						 } catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					System.out.println(sFound);
			
			
			
				}
				if(sFound){
					lockId = "false";
					
					break;
					}
				
			}
			
		}
	}else{
		lockId = "false";
	}
		newIdentityMessage.put("approved",lockId);
		//s.close();
		return newIdentityMessage;
		
	}
	
	static JSONObject createRoom(String id ,String roomId,String serverId){
		
		JSONObject newRoomMessage = new JSONObject();
		JSONObject lockRoom = new JSONObject();
		JSONObject RoomMessage;
		JSONParser parser = new JSONParser();
		boolean found = false;
		boolean sFound = false;
		String lockR = "false";
		Socket s;
		
		newRoomMessage.put("type","createroom");
		newRoomMessage.put("roomid",roomId);
		
		if (roomId.matches(("^[a-zA-Z]([a-zA-z0-9]){2,15}$"))){
		
			for(chatRoomInfo room : Server.listOfrooms){
				
				if ((room.getchatRoomId().equals(roomId))||(room.owner.equals(id)) ){
					found =true;
					break;
				}
			}
			
			if (found){
				lockR = "false";
				
			}
			else{
			 
				lockRoom.put("type", "lockroomid");
				lockRoom.put("serverid", serverId);
				lockRoom.put("roomid" , roomId);
				for(ServerInfo server : Server.listOfservers){
					if (!(server.getServerId().equals(serverId))){
						
						String hostName = server.getServerAddress();
						int serverPort = server.getServersPort();
						
						try{
							
							s = new Socket(hostName, serverPort);
						
							DataInputStream in = new DataInputStream( s.getInputStream());
							DataOutputStream out =new DataOutputStream( s.getOutputStream());
							System.out.println("Sending data");
							 
							 out.write((lockRoom.toJSONString() + "\n").getBytes("UTF-8"));
							 out.flush();
	
							
							RoomMessage = (JSONObject) parser.parse(in.readLine());
							 String locked = (String) RoomMessage.get("locked");
							 
							 if(locked.equals("true")){
								 sFound = true;
							 }
							 
							 }catch (UnknownHostException e) {
							 System.out.println("Socket:"+e.getMessage());
							 }catch (EOFException e){
							 System.out.println("EOF:"+e.getMessage());
							 }catch (IOException e){
							 System.out.println("readline:"+e.getMessage());
							 } catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					
					}
					if(sFound){
						lockR = "true";
						break;
					}
				}
		
			}
		}else{
			lockR = "false";
		}
	
		newRoomMessage.put("approved",lockR);
		
		return newRoomMessage;
		
	}
	
	static JSONObject list(){
		
		JSONObject RoomsList = new JSONObject();
		JSONArray rooms = new JSONArray();
		for (chatRoomInfo room : Server.listOfrooms){
			rooms.add(room.getchatRoomId());
		}
		RoomsList.put("type","roomlist");
		RoomsList.put("rooms",rooms);
		
		return RoomsList;
	}

	static JSONObject joinRoom(String id,String chatRoom , String newChatRoom,String serverId){
		
		JSONObject joinRoom = new JSONObject();
		JSONObject returnedJson = new JSONObject();
		JSONObject joinRoomOtherServer = new JSONObject();
		boolean found = false;
		String joinApproval=null;
		int joinCase = 1;
		
		joinRoom.put("type", "roomchange");
		joinRoom.put("identity", id);
	    joinRoom.put("former", chatRoom);
	    
	    for(chatRoomInfo room : Server.listOfrooms){
	    	if (room.owner.equals(id)){
	    		found = true;
	    		break;
	    	}
	    }
	    
	    
	    if(!found){
			for(chatRoomInfo room : Server.listOfrooms){
				
				if(room.chatRoomId.equals(newChatRoom)){
					
						if (room.serverId.equals(serverId)){
							
							if(!(room.owner.equals(id))){
								joinCase = 2;
								
								for(UserInfo user: Server.listOfusers){
									if(user.username.equals(id)){
					
										user.chatroom = newChatRoom;
									}
								}
								
								break;
							}else{
								
								joinCase = 3;
							
								joinApproval = joinRoom.toString();
								break;
							}
						}else{
							
							for (ServerInfo server : Server.listOfservers){
								if(server.getServerId().equals(room.getserverId())){
									joinCase = 4;
								
									InetAddress serverAddress = null;
									try {
										serverAddress = InetAddress.getByName(server.getServerAddress());
									} catch (UnknownHostException e) {
										
										e.printStackTrace();
									}
									String address = serverAddress.getHostAddress();
									serverAddress.getHostAddress();
									String port = Integer.toString(server.getClientsPort());
									joinRoomOtherServer.put("type","route");
									joinRoomOtherServer.put("roomid",newChatRoom);
									joinRoomOtherServer.put("host",address);
									joinRoomOtherServer.put("port",port);
									
									joinApproval = joinRoom.toString();
									break;
								}
							}
							
						}
				}
			}
	    }
		
	if (joinCase == 2){
		joinRoom.put("roomid", newChatRoom);
		returnedJson = joinRoom;
		
	}else if(joinCase == 4){
		returnedJson = joinRoomOtherServer;
	}else {
		joinRoom.put("roomid", chatRoom);
		returnedJson = joinRoom;
	}
	
		return returnedJson;
	}
}
