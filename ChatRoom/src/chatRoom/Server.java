package chatRoom;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;



public class Server {
	static BufferedReader br = null;
	static List<UserInfo> listOflockedIds;
	static List<ServerInfo> listOfservers;
	static List<UserInfo> listOfusers;
	static List<chatRoomInfo> listOfrooms;
	static List<chatRoomInfo> listOfLockedRooms;
	
	public Server() {
		listOflockedIds = new ArrayList<UserInfo>();
		listOfservers = new ArrayList<ServerInfo>();
		listOfusers = new ArrayList<UserInfo>();
		listOfrooms = new ArrayList<chatRoomInfo>();
		listOfLockedRooms = new ArrayList<chatRoomInfo>();
		
	}
	
	public static void main(String[] args) {
		
		String currentServerId= null;
	    String configFile= null;
	   // String keyfile = "/Users/Khalid/Desktop/comp90015/keystore.jks";
	    //System.setProperty("javax.net.ssl.keyStore",keyfile);
	    //System.setProperty("javax.net.ssl.keyStorePassword","changeit");
	    //System.setProperty("javax.net.debug","all");
		System.out.println("reading command line options");
		
		Options options = new Options();
		options.addOption("n",true,"configuration file path");
		options.addOption("l",true,"server id");
		
		CommandLineParser parser = new DefaultParser();
		
		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args);
		} catch (org.apache.commons.cli.ParseException e) {
			
			e.printStackTrace();
		}
		
		if(cmd.hasOption("n")){
			configFile = cmd.getOptionValue("n");
		}
		
		if(cmd.hasOption("l")){
			currentServerId = cmd.getOptionValue("l");
		}
		Server server = new Server();
		server.execute(currentServerId, configFile);
	}
	
	
	public void execute(String currentServerId, String configFile) {
		ServerInfo s;
		int serverPort = 4444;
		
		
		
		try{
			
			br = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), "UTF-8"));
			String line;
	        while ((line = br.readLine()) != null)
	        {   
	        	String [] tokens = line.split("\\n");
	        	
	        	for (int i=0 ; i< tokens.length ; i++){
	        		
	        		
	        		String [] newTokens = line.split("\\t");
	        			 s = new ServerInfo(newTokens[0],newTokens[1],Integer.parseInt(newTokens[2]),Integer.parseInt(newTokens[3]));
	        			listOfservers.add(s);
	        	}
	        
	        }
	        
	        for (ServerInfo server: listOfservers){
				
				chatRoomInfo room = new chatRoomInfo();
				room.chatRoomId ="MainHall-"+ server.getServerId();
				room.owner = "";
				room.serverId = server.getServerId();
				listOfrooms.add(room);
			}
	        
			}catch (Exception e){
				System.out.println("configuration file is not found");
				e.printStackTrace();
			}
			
			//serverInfo(listOfservers);
			int serverPort2 = 0;
			for (ServerInfo server : listOfservers ){
				if (server.getServerId().equals(currentServerId)){
	    			serverPort = server.getClientsPort();
	    			serverPort2 = server.getServersPort();
	    		}
	        	
	        }
			
			
			
			//Accept client connection
			
			
			//SSLServerSocket listeningSocket = null;
			ServerSocket listeningSocket = null;
			ServerSocket listeningServerSocket = null;
			try {
			
				// Create a server socket listening on port 4444
				//SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
				//listeningSocket = (SSLServerSocket)  sslserversocketfactory.createServerSocket(serverPort);
				listeningSocket = new ServerSocket(serverPort);
				System.out.println(Thread.currentThread().getName() + 
						" - Server listening on port" + serverPort);
				
				listeningServerSocket = new ServerSocket(serverPort2);
				System.out.println("Server is listenting on port " + serverPort2);
				
				ServerConnection serverConnection = new ServerConnection(listeningServerSocket, currentServerId);
				
				//Listen for incoming connections for ever
				while (true) {
					
					
					
					//Accept an incoming client connection request
					//SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();
					//SSLSocket clientSocket = (SSLSocket)listeningSocket.accept();
					Socket clientSocket = listeningSocket.accept();
					System.out.println(Thread.currentThread().getName() 
							+ " - Client conection accepted");
					

					//Create one thread per connection, each thread will be
					//responsible for listening for messages from the client, placing them
					//in a queue, and creating another thread that processes the messages
					//placed in the queue
					ClientConnection clientConnection = new ClientConnection(clientSocket, currentServerId);
					clientConnection.setName("Thread" + currentServerId);
					clientConnection.start();
					
					
					//Register the new connection with the client manager
					ServerState.getInstance().clientConnected(clientConnection);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(listeningSocket != null) {
					try {
						listeningSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
	}
		 
	public void serverInfo(List<ServerInfo> s){
		Server.listOfservers = s;
	}
	public List<ServerInfo> getServerInfo() {
		return listOfservers;
	}

	
}
