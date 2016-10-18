package chatRoom;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class  ClientConnection extends Thread {

	private SSLSocket SSLclientsocket;
	private BufferedReader reader;
	private BufferedWriter writer;
	//This queue holds messages sent by the client or messages intended for the client from other threads
	private BlockingQueue<Message> messageQueue;
	private String currentServerId;
	JSONObject msgJObj = null;
	String identity = null;
	String chatRoom = null;
	String roomId = null;
	String loginUsername = null;
	String username = null;
	public ClientConnection(SSLSocket clientSocket, String currentServerId) {
		try {
			this.SSLclientsocket = clientSocket;
			reader = new BufferedReader(new InputStreamReader(SSLclientsocket.getInputStream(), "UTF-8"));
			writer = new BufferedWriter(new OutputStreamWriter(SSLclientsocket.getOutputStream(), "UTF-8"));	
			messageQueue = new LinkedBlockingQueue<Message>();
			this.currentServerId = currentServerId;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		try {
			
			ClientMessageReader messageReader = new ClientMessageReader(reader, messageQueue);
			messageReader.setName(this.getName() + "Reader");
			messageReader.start();
			
//			System.out.println(Thread.currentThread().getName()
//					+ " - Processing client " + currentServerId + "  messages");
			
			
			//Monitor the queue to process any incoming messages (consumer)
			while(true) {
				
				Message msg = messageQueue.take();
				 msgJObj = new JSONObject();
				 msgJObj = msg.getMessage();
				
				if(!msg.isFromClient() && msg.getMessage().equals("quit")) {
					Iterator<UserInfo> iter = Server.listOfusers.iterator();
					while (iter.hasNext()) {
					    UserInfo str = iter.next();
					    if(str.username.equals(this.identity))
					        iter.remove();
					}
					break;
				}
				
				if(msg.isFromClient()) {
				
			    	String s = (String) msgJObj.get("type");
			    	boolean authentication = true;
			    	
			    	if (s.equals("login")){
					    username = (String) msgJObj.get("username");
						String password = (String) msgJObj.get("password");
						
						JSONObject authenticated =MessageHandler.login(username,password);
						String auth =(String) authenticated.get("authenticated");

						if(auth != null && auth.equals("false")){
							authentication = false;
						}
						
						Message msgAuthApproval = new Message(false,authenticated);
						messageQueue.add(msgAuthApproval);
						if(!authentication){
							ServerState.getInstance().DisconnectClient(this);
						}else{
							loginUsername = username;
						}
					}
			    	
					if(s.equals("newidentity")){
						
			    		identity = (String)msgJObj.get("identity");
			    	
			    		chatRoom = "MainHall-"+ currentServerId;
			    		
			    		JSONObject approved = MessageHandler.newIdnetityClass(identity, chatRoom, currentServerId);
			    		Message msgIdApproved = new Message(false,approved);
						//messageQueue.add(msgIdApproved);
						write(msgIdApproved.getMessage().toJSONString()+"\n");
						String idApproval = (String) approved.get("approved");
					
						List<ClientConnection> connectedClients = ServerState.getInstance().getConnectedClients();
						
						if (idApproval.equals("true")){
							
							JSONObject changeRoom = MessageHandler.changeRoomClass(identity,chatRoom,"");
				    		Message msgChangeRoom =new Message(false,changeRoom);
				    		//messageQueue.add(msgChangeRoom);
				    		
				    		//connectedClients = ServerState.getInstance().getConnectedClients();
				    		for(ClientConnection client : connectedClients){
				    			if(client.chatRoom.equals(this.chatRoom)){
				    				
				    				client.getMessageQueue().add(msgChangeRoom);
				    			}
				    		}
							
				    		UserInfo username = new UserInfo();
							username.username = identity;
							username.chatroom = chatRoom;
							Server.listOfusers.add(username);

							MessageHandler.updateClient(identity, loginUsername, currentServerId);
				    		
						
						}else{
							removeAuthUser(this.username);

							ServerState.getInstance().DisconnectClient(this);
							break;
						}
						
						
						MessageHandler.releaseIdentity(identity,currentServerId);
					
					}
			    	
					if(s.equals("createroom")){
			    		String tempChatRoom = (String) msgJObj.get("roomid");
			    		String formerRoom = this.chatRoom;
			    		JSONObject createroom = MessageHandler.createRoom(this.identity , tempChatRoom,currentServerId);
			    		Message msgCreateRoom = new Message (false,createroom);
			    		messageQueue.add(msgCreateRoom);
			    		String approval = String.valueOf(createroom.get("approved"));
						Boolean approved = Boolean.valueOf(approval);

						if (approved) {
			    			
			    			chatRoom = tempChatRoom;
			    			chatRoomInfo room = new chatRoomInfo();
			    			room.chatRoomId =chatRoom;
			    			room.owner = identity;
			    			room.serverId = currentServerId;
			    			Server.listOfrooms.add(room);
				    		JSONObject changeRoom1 = MessageHandler.changeRoomClass(this.identity,chatRoom,"");
				    		Message msgChangeRoom1 =new Message(false,changeRoom1);
				    		messageQueue.add(msgChangeRoom1);
				    		
				    		List<ClientConnection> connectedClients = ServerState.getInstance().getConnectedClients();
				    		for(ClientConnection client : connectedClients){
				    			if((client.chatRoom.equals(formerRoom))&&!(client.identity.equals(this.identity))){
				    				client.getMessageQueue().add(msgChangeRoom1);
				    			}
				    		}
				    		
			    		}
			    		
			    		MessageHandler.releaseRoom(chatRoom,currentServerId,approval);
			    		
					}	
					
					
		    		if(s.equals("message")){
			    		
			    		JSONObject newMessage = new JSONObject();
			    		String content = (String)msgJObj.get("content");
			    		
			    		newMessage.put("type","message");
			    		newMessage.put("identity",this.identity);
			    		newMessage.put("content",content);
			    	
			    		msg = new Message(false, newMessage);
			    		
			    		List<ClientConnection> connectedClients = ServerState.getInstance().getConnectedClients();
						for(ClientConnection client : connectedClients) {
							//Place the message on the client's queue
							if(client.chatRoom.equals(this.chatRoom) && (!client.equals(this))){
								
								client.getMessageQueue().add(msg);
							}
							
						}
						
		    		}
			    
		    		
		    		if(s.equals("list")){
			    		JSONObject list = MessageHandler.list();
			    		msg = new Message (false,list);
			    	
			    		messageQueue.add(msg);
			    	
		    		}	
			    	
			    	if(s.equals("join")){	 
			    		String newChatRoom = (String)msgJObj.get("roomid");
			    		JSONObject joinRoom = MessageHandler.joinRoom(identity,chatRoom,newChatRoom,currentServerId);
			    		JSONObject changeRoom = MessageHandler.changeRoomClass(identity, newChatRoom, chatRoom);
			    		Message changeRoomMsg = new Message(false,changeRoom);
			    		msg = new Message (false,joinRoom);
			    		
			    		List<ClientConnection> connectedClients = ServerState.getInstance().getConnectedClients();
			    		for(ClientConnection client : connectedClients){
			    			if((client.chatRoom.equals(this.chatRoom)) && !(client.identity.equals(this.identity))){
			    				client.getMessageQueue().add(changeRoomMsg);
			    			}
			    		}
			    		
			    		String roomid = (String) joinRoom.get("roomid");
			    		chatRoom = roomid;
			    	
			    		messageQueue.add(msg);
			    		connectedClients = ServerState.getInstance().getConnectedClients();
			    		for(ClientConnection client : connectedClients){
			    			if(client.chatRoom.equals(this.chatRoom)){
			    				client.getMessageQueue().add(msg);
			    			}
			    		}
			    		
				    	if(joinRoom.get("type").equals("route")){
				    		Iterator<UserInfo> iter = Server.listOfusers.iterator();
							while (iter.hasNext()) {
							    UserInfo str = iter.next();
							    if(str.username.equals(this.identity))
							        iter.remove();
							}
							ServerState.getInstance().clientDisconnected(this);
							
				    	}
				    	
				    	
			    	}
			    	
		    		if(s.equals("who")){
			    		JSONObject who = MessageHandler.who(chatRoom);
			    		msg = new Message(false,who);
			    		messageQueue.add(msg);
			    	
		    		}
			    	
		    		
		    		if(s.equals("movejoin")){
		    			
			    		identity = (String) msgJObj.get("identity");
			    		String newChatRoom = (String)msgJObj.get("roomid");
			    		String formerRoom = (String) msgJObj.get("former");
			    		JSONObject moveJoin = MessageHandler.moveJoin(identity, newChatRoom,formerRoom , currentServerId);
			    		msg = new Message (false,moveJoin);
			    		String chatRoom = null;
			    		messageQueue.add(msg);

						MessageHandler.updateClient(identity, loginUsername, currentServerId);
			    		
			    		
			    		for(UserInfo user : Server.listOfusers){
			    			if (user.username.equals(identity)){
			    				this.chatRoom = user.chatroom;
			    				
			    			}
			    		}
			    		
			    		JSONObject changRoom = MessageHandler.changeRoomClass(identity, this.chatRoom, formerRoom);
			    		msg = new Message(false,changRoom);
			    		
			    		
			    		List<ClientConnection> connectedClients = ServerState.getInstance().getConnectedClients();
			    		for (ClientConnection client : connectedClients){
			    			if (client.chatRoom.equals(this.chatRoom)){
			    				client.getMessageQueue().add(msg);
			    			}
			    			
			    		}
			    	
		    		}
			    	
		    		if(s.equals("deleteroom")){
		    			
			    		String roomid = (String) msgJObj.get("roomid");
			    		JSONObject deleteRoom = MessageHandler.deleteRoom(identity,roomid,currentServerId);
			    		Message msgChangeRoom = new Message(true,null);
			    		if(deleteRoom.get("approved").equals("true")){
			    	
			    			List<ClientConnection> connectedClients = ServerState.getInstance().getConnectedClients();
							boolean sent = true;
				    		for(ClientConnection client : connectedClients) {
								
								System.out.println("Loop" + client.chatRoom);
								if(client.chatRoom.equals(roomid) ){
									client.chatRoom = "MainHall-"+currentServerId;
					    		    JSONObject changeRoom = MessageHandler.changeRoomClass(client.identity,chatRoom,"");
						    	    msgChangeRoom =new Message(false,changeRoom);
								
									sent = false;
									
								}	
								
								if(!sent){
									for(ClientConnection clientB : connectedClients){
										if ((clientB.chatRoom.equals("MainHall-"+currentServerId))){
											clientB.getMessageQueue().add(msgChangeRoom);
											sent = true;
										}
									}
								}	
							}
				    		Iterator<chatRoomInfo> iter = Server.listOfrooms.iterator();
							while (iter.hasNext()) {
							    chatRoomInfo str = iter.next();
							    if(str.chatRoomId.equals(roomid))
							        iter.remove();
							}
			    		}
			    		
			    		msg = new Message(false,deleteRoom);
			    		messageQueue.add(msg);
			    	    
			    }
		    	
	    		if(s.equals("quit")){
	    			boolean found = false;
	    			String chatRoom = null;
	    			if(identity!=null){
	    				
		    			for(chatRoomInfo room :Server.listOfrooms){
		    				if(this.identity.equals(room.owner)){
		    					found = true;
		    					chatRoom = room.chatRoomId;
		    				}
		    			}
	    		
		    			if (found){
		    					JSONObject deleteRoom = MessageHandler.deleteRoom(this.identity,chatRoom, currentServerId);
					    	   
					    			List<ClientConnection> connectedClients = ServerState.getInstance().getConnectedClients();
									boolean sent = true;
						    		for(ClientConnection client : connectedClients) {
										
										if(client.chatRoom.equals(this.chatRoom)&&!(client.identity.equals(this.identity)) ){
											client.chatRoom = "MainHall-"+currentServerId;
							    		    JSONObject changeRoom = MessageHandler.changeRoomClass(client.identity,client.chatRoom,"");
								    	    msg =new Message(false,changeRoom);
										
											sent = false;
											
										}	
										
										if(!sent){
											for(ClientConnection clientB : connectedClients){
												if ((clientB.chatRoom.equals("MainHall-"+currentServerId))){
													clientB.getMessageQueue().add(msg);
													sent = true;
												}
											}
										}	
									}
						    		
						    		Iterator<chatRoomInfo> iter = Server.listOfrooms.iterator();
									while (iter.hasNext()) {
									    chatRoomInfo str1 = iter.next();
									    if(str1.chatRoomId.equals(this.chatRoom))
									        iter.remove();
									}
									
									
									msg =new Message(false,deleteRoom);
							    	messageQueue.add(msg);
							    	JSONObject changeRoom = MessageHandler.changeRoomClass(this.identity,"","");
							    	msg = new Message(false,changeRoom);
							    	messageQueue.add(msg);
									ServerState.getInstance().DisconnectClient(this);
		    			}else{
		    				JSONObject changeRoom = MessageHandler.changeRoomClass(this.identity,"","");
					    	msg = new Message(false,changeRoom);
					    	write(msg.getMessage().toJSONString()+"\n");
					    	
		    			}
	    		 
	    			Iterator<UserInfo> itr = Server.listOfusers.iterator();
					while (itr.hasNext()) {
					    UserInfo str = itr.next();
					    if(str.username.equals(identity))
					        itr.remove();
					}
					removeAuthUser(loginUsername);
	    		}
					ServerState.getInstance().DisconnectClient(this);
					
					
					
		    		break;
	    		}
		    	
				} else {
					
					write(msg.getMessage().toJSONString()+"\n");
					
				}
			}
			
			SSLclientsocket.close();
			//ServerState.getInstance().clientDisconnected(this);
			
			
//			System.out.println(Thread.currentThread().getName()
//					+ " - Client " +" disconnected");

		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(SSLclientsocket != null) {
				try {
					reader.close();
					writer.close();
					SSLclientsocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	

	public BlockingQueue<Message> getMessageQueue() {
		return messageQueue;
	}

	public void removeAuthUser() {
		removeAuthUser(loginUsername);
	}

	public void removeAuthUser(String username){
		 JSONObject releaseLoginUsername = new JSONObject();
		 SSLSocket s;
		 JSONParser parser = new JSONParser();
		 String AuthApproval = "true";
		 String authenticated = null;
		 releaseLoginUsername.put("type", "releaseusername");
		 releaseLoginUsername.put("username", username);
		
		 for(ServerInfo server : Server.listOfservers){
			
				if (server.getServerId().equals("AS")){
					
					String hostName = server.getServerAddress();
					int serverPort = server.getServersPort();
				
					try{
						SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
						s = (SSLSocket) sslsocketfactory.createSocket(hostName, serverPort);
						DataOutputStream out =new DataOutputStream( s.getOutputStream());
						
//						System.out.println("Sending data");
						out.write((releaseLoginUsername.toJSONString() + "\n").getBytes("UTF-8"));
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
	
	public void write(String msg) {
		try {
			writer.write(msg);
			writer.flush();
//			System.out.println(Thread.currentThread().getName() + " - Message sent to client " );
		} catch (IOException e) {
			System.out.println(identity + " has closed the connection.");
		}
	}
}
