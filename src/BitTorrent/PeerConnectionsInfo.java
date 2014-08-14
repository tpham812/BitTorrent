package BitTorrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class holds data structure to manage peers
 * @author Amulya Uppala, Truong Pham, Jewel Lim
 *
 */
public class PeerConnectionsInfo {

	public static int uploadConnections = 0;
	public static int downloadConnections = 0;

	/** Holds a list of unchoked peers */
	public static List<Peer> unchokedPeers = new ArrayList<Peer>();
	/** Holds a list of choked peers */
	public static List<Peer> chokedPeers = new ArrayList<Peer>();
	/** Holds a list of download peers */
	public static List<Peer> downloadPeers = new ArrayList<Peer>();
	/** Holds a list of  peers from downloadPeers we will download from */
	public static List<Peer> subsetDPeers = new ArrayList<Peer>();
	/** Holds a list of all peers */
	public static HashMap<boolean[],Peer> peers = new HashMap<boolean[], Peer>();
		
}
