package BitTorrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PeerConnectionsInfo {

	public static int uploadConnections;
	public static int downloadConnections;

	public static List<Peer> uploadPeers;
	public static List<Peer> downloadPeers;
	
	public static HashMap<byte[],Peer> peers = new HashMap<byte[], Peer>();
	
	byte[] bitfield; 
	public PeerConnectionsInfo() {
		uploadConnections = 0;
		downloadConnections = 0;
		uploadPeers = new ArrayList<Peer>();
		downloadPeers = new ArrayList<Peer>();
	}
	
	
}
