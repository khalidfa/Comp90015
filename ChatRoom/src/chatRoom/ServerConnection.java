package chatRoom;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ServerConnection extends Thread {
	private SSLServerSocket listeningServerSocket;
	private SSLSocket serverSocket;
	
	private BufferedReader reader;
	private BufferedWriter writer;
	private String currentServerId;
	JSONObject msgJObj = null;
	
	

	public  ServerConnection(SSLServerSocket listeningServerSocket , String currentServerId){
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
			
			
			
			while(true) {
				
				serverSocket = (SSLSocket) listeningServerSocket.accept();
				
				reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream(), "UTF-8"));
				writer = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream(), "UTF-8"));
				msgJObj = (JSONObject) parser.parse(reader.readLine());
				
				String s = (String) msgJObj.get("type");
				JSONObject lockIdentity = new JSONObject();
				
				if(s.equals("releaseusername")){
					String username = (String)msgJObj.get("username");
					for(LoginInfo login :Server.listOfAuthUsers){
						if(login.loginUsername.equals(username)){
							login.loggedin = false;
						}
					}
				}
				
				if(s.equals("login")){
					JSONObject AuthUser = new JSONObject();
					String username = (String) msgJObj.get("username");
					String password = (String) msgJObj.get("password");
					
					AuthUser = login(username,password);
				
					writer.write(AuthUser.toJSONString() + "\n");	
					writer.flush();
				}
				
				if(s.equals("newServer")){
					String newServerId = (String)msgJObj.get("serverId");
					String newServerAdd = (String)msgJObj.get("serverAdd");
					int newCPort = Integer.parseInt((String)msgJObj.get("serverPort"));
					int newSPort = Integer.parseInt((String)msgJObj.get("serverPort2"));

					if (currentServerId.equals("AS")) {
						for (ServerInfo server : Server.listOfservers){
							if (!(server.getServerId().equals("AS"))){

								String hostName = server.getServerAddress();
								int otherServerPort = server.getServersPort();

								SSLSocket SSLsocket = null;
								try{
									SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
									SSLsocket = (SSLSocket) sslsocketfactory.createSocket(hostName, otherServerPort);

									DataInputStream in = new DataInputStream( SSLsocket.getInputStream());
									DataOutputStream out =new DataOutputStream( SSLsocket.getOutputStream());

									out.write((msgJObj.toJSONString() + "\n").getBytes("UTF-8"));
									out.flush();
								} catch (Exception e) {
									// Server failed
									System.out.println("Error communicating with server: " + server.getServerId());
								} finally {
									if (SSLsocket != null) {
										try {
											SSLsocket.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								}
							}
						}

						JSONObject serverListMessage = new JSONObject();
						serverListMessage.put("type", "newServerResponse");

						ArrayList<Map<String, Object>> servers = new ArrayList<>();
						for (ServerInfo serverInfo : Server.listOfservers) {
							if (serverInfo.getServerId().equals("AS")) {
								continue;
							}
							Map<String, Object> info = new HashMap<>();
							info.put("serverId", serverInfo.getServerId());
							info.put("serverAdd", serverInfo.getServerAddress());
							info.put("serverPort", serverInfo.getClientsPort());
							info.put("serverPort2", serverInfo.getServersPort());
							servers.add(info);
						}

						serverListMessage.put("servers", servers);

						writer.write(serverListMessage.toJSONString() + "\n");
						writer.flush();
					}

					Server.addServer(newServerId, newServerAdd, newCPort, newSPort);
				}
				
				if(s.equals("lockidentity")){	
					userIdentity= (String) msgJObj.get("identity");
					
					boolean foundInLockeIds = false;
					boolean found = false;
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
					boolean found = false;
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

				if(s.equals("heartbeat")){
					JSONObject heartbeat = new JSONObject();
					heartbeat.put("type", "heartbeat");
					heartbeat.put("alive", true);

					writer.write(heartbeat.toJSONString() + "\n");
					writer.flush();
				}

				if(!s.equals("heartbeat")){
					System.out.println("Message: " + msgJObj);
				}
			
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
		
	}
	public synchronized JSONObject  login (String username,String password){
		JSONObject AuthUser = new JSONObject();
		String authApproval = "false";
		
		for(LoginInfo login : Server.listOfAuthUsers){
			if(login.loginUsername.equals(username) && login.loginPassword.equals(password)&&!(login.loggedin)){
				authApproval = "true";
				login.loggedin=true;
				System.out.println(login.loginUsername);
				System.out.println(login.loginPassword);
				break;
			}
			
		}
		AuthUser.put("type","login");
		AuthUser.put("username",username);
		AuthUser.put("password", password);
		AuthUser.put("approval", authApproval);
		return AuthUser;
	}
}



