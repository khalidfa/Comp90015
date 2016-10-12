package chatRoom;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class ServerHeartbeatSensor extends Thread {

    private Server server;

    public  ServerHeartbeatSensor(Server server){
        this.server = server;
        try {
            //Wait 5 seconds before checking other servers status.
            Thread.sleep(5000);
            this.start();
        } catch(Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    public void run() {
        while (true) {
            JSONObject heartbeatMessage = new JSONObject();
            heartbeatMessage.put("type", "heartbeat");

            JSONParser parser = new JSONParser();

            ArrayList<ServerInfo> serverInfos = new ArrayList<>(Server.listOfservers);
            for (ServerInfo serverInfo : serverInfos) {
                String serverId = serverInfo.getServerId();
                if (!(serverId.equals("AS")) && !(serverId.equals(server.currentServerId))){

                    String hostName = serverInfo.getServerAddress();
                    int serverPort = serverInfo.getServersPort();

                    Socket socket = null;
                    try{

                        socket = new Socket(hostName, serverPort);

                        DataInputStream in = new DataInputStream( socket.getInputStream());
                        DataOutputStream out =new DataOutputStream( socket.getOutputStream());

                        out.write((heartbeatMessage.toJSONString() + "\n").getBytes("UTF-8"));
                        out.flush();

                        socket.setSoTimeout(10000);
                        JSONObject message;
                        message = (JSONObject) parser.parse(in.readLine());

                        Boolean isAlive = Boolean.valueOf(String.valueOf(message.get("alive")));

                        if(!isAlive){
                            server.deleteServer(serverId);
                        }

                    } catch (Exception e) {
                        // Server failed
                        server.deleteServer(serverId);
                    } finally {
                        if (socket != null) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            try {
                Thread.sleep(5000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
