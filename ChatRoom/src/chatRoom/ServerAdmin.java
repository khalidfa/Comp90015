package chatRoom;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;



public class ServerAdmin extends Thread{

	//private String currentServerId;
	private BufferedReader reader;
	private BufferedWriter writer;
	
	private String[] splitStr;

	
	
	public ServerAdmin(String currentServerId, String configFile){
		

		try {
			
			reader = new BufferedReader(new InputStreamReader(System.in));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile,true)));
			
			this.start();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			
			System.out.println("please enter information of the new server: serverid,server_address,client_port,coordination_port");
			
			String adminCommand=null;
			String outputMsg = "";
			
			while((adminCommand=reader.readLine())!=null){
				
				splitStr = adminCommand.split(",");
				
				int temp = 0;
				
				//whether the new server id is as same as the old servers
				for(ServerInfo serverInfo:Server.listOfservers){
					if(splitStr[0].equals(serverInfo.getServerId())){
						System.out.println("Sorry,the server your want to add has existed!");
						temp = temp +1;
					}
					
				}
				if(temp==0){  
					
					//add to outputMsg
					for(String piece:splitStr){
		
						outputMsg=outputMsg.concat(piece+"	");			
					}
					outputMsg=outputMsg.concat("N");
					//create json object to store the information of new server
//					JSONObject jsonObject = new JSONObject();
//					jsonObject.put("newSerId", splitStr[0]);
//					jsonObject.put("newSerAddress",splitStr[1]);
//					jsonObject.put("newSerCliPort", splitStr[2]);
//					jsonObject.put("newSerCoPort", splitStr[3]);
					
					//add the json to sever state
					//ServerState.getInstance().addNewServer(jsonObject);
					
					writer.write(outputMsg+"\n");
					writer.flush();
					System.out.println("the new server start successful");
					
				}
				
			}
			

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
	}
	
	
	
	
	
}
