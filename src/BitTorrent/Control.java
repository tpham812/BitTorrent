package BitTorrent;

import java.io.IOException;

public class Control {

	/**
	 * Connect to tracker and get a list of peers. Find the right peer and connects to it to begin download
	 * @param torrent_File Torrent file
	 * @param fileName File name to store 
	 */
	public static void startPeers() {
		try {
			/**Connect to peer*/
			//Download peer = new Download(peerIP, ((ByteBuffer)peer_Map.get(ConnectToTracker.KEY_PEER_ID)).array(), peerPort, fileName);
			/**Download file from peer*/
			//peer.downloadFileFromPeer();
		} catch (Exception e) {
			System.out.println("Error: Cannot create Peer.");
		}
		
	}
	
	
	
	//Keep track of uploaded peers, call ConnectionCount
	//If Upload is already 6, then we cannot unchoke anyone
	//If less than 3 download peers and peer is interested, let connect
	//If 30 second timer wakes, choke worst peer and unchoke randomly. 
	public static boolean unchoke() throws IOException{

		//read message from peer to see if they are interested.
		int msgIDfrPeer = Peer.readMessage();
		if (msgIDfrPeer == Message.MSG_INTERESTED){
			System.out.println("Peer is interested.");
			//If we have less than 3 downloading peers, let this peer connect.			
			if(ConnectionCount.uploadConnections < 3){
				return true;
			//If we are already connected to six people who are uploading from us, keep peer choked.
			} else if (ConnectionCount.uploadConnections > 6){
				//if timer is awake, then evaluate worst peer and unchoke random peer
				return false; 
			//read message to see if they are have messages.
			} else {	
				int msgIDfrPeer2 = Peer.readMessage();  		
				//byte[] bitFieldOrHaveMsg = new byte[in.available()];
				//in.readFully(bitFieldOrHaveMsg); /**get rid of bit field*/  			
				if(msgIDfrPeer2 == Message.MSG_HAVE || msgIDfrPeer2 == Message.MSG_BITFIELD){
					System.out.println("Peer has something to share! Do not choke.");
					return true;
				}	
				return false;
			}
		} else{
			//Peer.closeConnection(); because peer is not interested
			return false;
			
		}
	}
}
