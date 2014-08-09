package BitTorrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PeerConnectionsInfo {

	public static int uploadConnections = 0;
	public static int downloadConnections = 0;

	public static List<Peer> unchokedPeers = new ArrayList<Peer>();
	public static List<Peer> chokedPeers = new ArrayList<Peer>();
	public static List<Peer> downloadPeers = new ArrayList<Peer>();
	
	public static HashMap<byte[],Peer> peers = new HashMap<byte[], Peer>();
}
