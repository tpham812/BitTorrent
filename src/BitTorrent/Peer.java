package BitTorrent;

public class Peer {

	String peerID;
	String IP;
	int port;
	
	public Peer(int port, String IP){
		this.peerID = peerID;
		this.port = port;
		this.IP = IP;
		
	}
	
	public String toString(){
		String peerInfo = "Peer Information:" + peerID +  ", " + IP + ", " + port; 
		return peerInfo;
	}
	
}
