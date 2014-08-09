package BitTorrent;

import java.util.ArrayList;
import java.util.List;

public class ConnectionCount {

	public static int uploadConnections;
	public static int downloadConnections;

	public static List<Peer> uploadPeers;
	public static List<Peer> downloadPeers;

	public ConnectionCount() {
		uploadConnections = 0;
		downloadConnections = 0;
		uploadPeers = new ArrayList<Peer>();
		downloadPeers = new ArrayList<Peer>();
	}
	
	
}
