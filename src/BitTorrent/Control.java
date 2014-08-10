package BitTorrent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Control {
	boolean gotEntireBitField = false;
	PeerConnectionsInfo pci;
	/**
	 * Finds the perfect subset of peers to get the entire file with the rarest first prefered.
	 * @param pci the peer connections info which has all the peers.
	 */
	public boolean startPeers(PeerConnectionsInfo pci) {
		this.pci = pci;
		findList();
		return gotEntireBitField;
	}


	/**makes a list of subset of peers with bitfields adding up to the entire file with rarest peers first*/
	private void findList() {
		List<Peer> subset= pci.subsetDPeers;
		int numChunks = ConnectToTracker.torrentI.piece_hashes.length;
		bitFieldDS[] bfDS = new bitFieldDS[numChunks];

		/**for each chunk add up the number of peers that have that bit field to true*/
		for (int j = 0; j<numChunks; j++){ 
			for (int i = 0; i<pci.downloadPeers.size();i++){
				if(pci.downloadPeers.get(i).boolBitField[j]==true){
					bfDS[j].sum++;
					bfDS[j].lp.add(pci.downloadPeers.get(i));
				}
			}
		}

		makeList(subset,bfDS);

	}

	/**Makes a subset list based on the rarest pieces first.
	 * @param pci peer connections info that holds the peers and their info
	 * @param subset list of peers needed for download
	 * @param bfDS number of peers that have each chunk at that index
	 * */
	public void makeList(List<Peer> subset, bitFieldDS[] bfDS){
		boolean[] bf = new boolean[bfDS.length];
		for (int z= 0; z< bf.length;z++){
			bf[z]=false;
		}

		for (int j = 0; j<bfDS.length;j++){ //linear search to find the smallest first
			int smallest = Integer.MAX_VALUE;
			int index = 0;
			for (int i = 0; i<bfDS.length;i++){
				if((bfDS[i].sum!=0) && (bfDS[i].sum<smallest)){
					index = i;
					smallest=bfDS[i].sum;
					bfDS[i].sum = Integer.MAX_VALUE;
				}
			}
			subset.add(bfDS[index].lp.get(0)); //get first peer that has that index
		}

		if (fullBitField(bf)==true){
			gotEntireBitField=true;
		}

	}

	/**sees if bitfield is not full or not. if bitfield is not full then we need more chunks*/
	private boolean fullBitField(boolean[] bf){ 
		for (int i = 0; i< bf.length;i++){
			if (bf[i]==false){
				return false;
			}
		}
		return true;
	}

	/**
	 * @param peer
	 * @throws IOException
	 * This method accesses the list of unchoked peer connections in order 
	 * to determine whether we can connect to upload to more peers. 
	 * It also checks if the peer has a bitfield or have
	 * message as well as calls randomUnchoke() in order to implement
	 * the optimistic unchoke function. 
	 */


	public void unchoke(Peer peer) throws IOException{


		//read message from peer to see if they are interested.
		int msgfrPeer = peer.readMessage();
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
				randomUnchoke();
				int msgfrPeer2 = peer.readMessage();  		
				if(msgfrPeer2 == Message.MSG_HAVE || msgfrPeer2 == Message.MSG_BITFIELD){
					PeerConnectionsInfo.unchokedPeers.add(peer);
					System.out.println("Peer has something to share! Peer is unchoked.");					
				}else {
					PeerConnectionsInfo.chokedPeers.add(peer);
					System.out.println("Peer has nothing to share. Peer is choked");
				}

			}
		} else{
			peer.closeConnection();
			//peer is uninterested; we did not receive an interested 



		}
	}

	/** This method implements the optimistic unchoke functionality.
	 *  When the 30 second timer is up, it chooses the unchoked peer
	 *  with the worst throughput, unchokes it, and randomly picks a
	 *  choked peer as the optimistic choke.  
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


	public void makeThreads(FileChunks fc) {
		Download temp;
		for(int i = 0; i< pci.subsetDPeers.size(); i++){
			try {
				temp = new Download(pci.subsetDPeers.get(i), fc, pci);
				temp.run();
			} catch (Exception e) {
				System.out.println("Error: Could not create Thread to run!");
			} 
		}

	}

}
