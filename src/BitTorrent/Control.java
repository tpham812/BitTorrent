package BitTorrent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Control {

	/**
	 * Connect to tracker and get a list of peers. Find the right peer and connects to it to begin download
	 * @param list the list of peers.
	 */
	public void startPeers(ArrayList list) {
		try {
			/**Connect to peer*/
			//Download peer = new Download(peerIP, ((ByteBuffer)peer_Map.get(ConnectToTracker.KEY_PEER_ID)).array(), peerPort, fileName);
			/**Download file from peer*/
			//peer.downloadFileFromPeer();
		} catch (Exception e) {
			System.out.println("Error: Cannot create Peer.");
		}

	}



	/**
	 * @param peer
	 * @throws IOException
	 * This method accesses the list of unchoked peer connections
	 * in order to determine whether we can connect to upload to 
	 * more peers. It also checks if the peer has a bitfield or have
	 * message as well as calls randomUnchoke() in order to implement
	 * the optimistic unchoke function.
	 */
	public void unchoke(Peer peer) throws IOException{
		

		//read message from peer to see if they are interested.
		int msgfrPeer = Peer.readMessage();
		if (msgfrPeer == Message.MSG_INTERESTED){
			System.out.println("Peer is interested.");
			//If we have less than 3 downloading peers, let this peer connect.			
			if(PeerConnectionsInfo.unchokedPeers.size() < 3){
				PeerConnectionsInfo.unchokedPeers.add(peer);
				System.out.println("Peer is unchoked.");
				//If we are already connected to six people who are uploading from us, keep peer choked.
			} else if (PeerConnectionsInfo.uploadConnections > 6){
				PeerConnectionsInfo.chokedPeers.add(peer);
				System.out.println("Peer is choked.");
				randomUnchoke();
				//read message to see if they are have messages.
			} else {	
				int msgfrPeer2 = Peer.readMessage();  		
					if(msgfrPeer2 == Message.MSG_HAVE || msgfrPeer2 == Message.MSG_BITFIELD){
						PeerConnectionsInfo.unchokedPeers.add(peer);
						System.out.println("Peer has something to share! Peer is unchoked.");					
					}	
					randomUnchoke(); 
			} 
		} else{
			peer.closeConnection();
			//peer is uninterested; we did not receive an interested message. 
			


		}
	}
	
	/**
	 * This method implements the optimistic unchoke functionality. 
	 * When the 30 second timer is up, it chooses the unchoked peer
	 * with the worst throughput, unchokes it, and randomly picks a
	 * choked peer as the optimistic choke.  
	 */
	public void randomUnchoke(){
		
		Peer optimisticUnchoke;
		Peer chokedPeer = null;
		double currTP = 0.0;
		double leastTP = 999999999999999999999.0; //some very big number			
		//IF timer is up, PLACE THIS HERE!!!!!
		

		for (int i = 0; i < PeerConnectionsInfo.unchokedPeers.size(); i++){
			//compare throughput, lowest: PeersConnectionsInfo.chokedPeers.add(peer);
			currTP = PeerConnectionsInfo.unchokedPeers.get(i).throughput;
			if(currTP < leastTP || currTP == leastTP){
				leastTP = currTP;
				chokedPeer = PeerConnectionsInfo.unchokedPeers.get(i);
			}
		}
		
		PeerConnectionsInfo.unchokedPeers.remove(chokedPeer);
		PeerConnectionsInfo.chokedPeers.add(chokedPeer);
		
		
		//randomly select a peer from PeerConnectionInfo.chokedPeers
		Random r = new Random();
		optimisticUnchoke = PeerConnectionsInfo.chokedPeers.get(r.nextInt(PeerConnectionsInfo.chokedPeers.size()));
		PeerConnectionsInfo.unchokedPeers.add(optimisticUnchoke);
		PeerConnectionsInfo.chokedPeers.remove(optimisticUnchoke);
	}
}
