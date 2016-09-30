package chatRoom;

import java.io.BufferedReader;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ClientMessageReader extends Thread {

	private BufferedReader reader; 
	private BlockingQueue<Message> messageQueue;
	JSONParser parser;
	JSONObject clientMsg = null;
	public ClientMessageReader(BufferedReader reader, BlockingQueue<Message> messageQueue) {
		this.reader = reader;
		this.messageQueue = messageQueue;
		parser = new JSONParser();
	}
	
	@Override
	//This thread reads messages from the client's socket input stream
	public void run() {
		try {
			
			System.out.println(Thread.currentThread().getName() 
					+ " - Reading messages from client connection");
			
			
			while ((clientMsg = (JSONObject) this.parser.parse(reader.readLine())) != null) {
				System.out.println(Thread.currentThread().getName() 
						+ " - Message from client received: " + clientMsg);
				//place the message in the queue for the client connection thread to process
				Message msg = new Message(true, clientMsg);
				System.out.println("Message: " + msg.getMessage());
				messageQueue.add(msg);
			}
			
			
			//If the end of the stream was reached, the client closed the connection
			//Put the exit message in the queue to allow the client connection thread to 
			//close the socket
			JSONObject exitMessage = new JSONObject();
			exitMessage.put("type", "exit");
			Message exit = new Message(false, exitMessage);
			messageQueue.add(exit);
			reader.close();
		} catch (SocketException e) {
			//In some platforms like windows, when the end of stream is reached, instead
			//of returning null, the readLine method throws a SocketException, so 
			//do whatever you do when the while loop ends here as well
			JSONObject exitMessage = new JSONObject();
			exitMessage.put("type", "quit");
			Message exit = new Message(false, exitMessage);
			messageQueue.add(exit);		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
