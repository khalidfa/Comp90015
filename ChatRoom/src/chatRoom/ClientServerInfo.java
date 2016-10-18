package chatRoom;

public class ClientServerInfo {
    String username = "";
    String serverid = "";

    public ClientServerInfo(String username, String serverid) {
        this.username = username;
        this.serverid = serverid;
    }

    public String getServerid() {
        return serverid;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
