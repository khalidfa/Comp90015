package chatRoom.chatClient;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class Client {

	public static SSLSocket sslSocket;
	public static void main(String[] args) throws IOException, ParseException {
		System.setProperty("javax.net.ssl.trustStore","C:/UniMelb/dsassignment2/SSLDemo/clienttrust.jks");
		
		System.setProperty("javax.net.ssl.trustStorePassword","client");
		
		// Enable debugging to view the handshake and communication which happens between the SSLClient and the SSLServer
		System.setProperty("javax.net.debug","all");
		
		SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		String identity = null;
		boolean debug = false;
		try {
			//load command line args
			ComLineValues values = new ComLineValues();
			CmdLineParser parser = new CmdLineParser(values);
			try {
				parser.parseArgument(args);
				String hostname = values.getHost();
				identity = values.getIdeneity();
				int port = values.getPort();
				debug = values.isDebug();
				sslSocket = (SSLSocket) sslsocketfactory.createSocket(hostname, port);
			} catch (CmdLineException e) {
				e.printStackTrace();
			}
			
			State state = new State(identity, "","","");
			
			// start sending thread
			MessageSendThread messageSendThread = new MessageSendThread(sslSocket, state, debug);
			Thread sendThread = new Thread(messageSendThread);
			sendThread.start();
			
			// start receiving thread
			Thread receiveThread = new Thread(new MessageReceiveThread(sslSocket, state, messageSendThread, debug));
			receiveThread.start();
			
		} catch (UnknownHostException e) {
			System.out.println("Unknown host");
		} catch (IOException e) {
			System.out.println("Communication Error: " + e.getMessage());
		}
	}
}
