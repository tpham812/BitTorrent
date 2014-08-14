package BitTorrent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Control {
	boolean gotEntireBitField = false;
	/**
	 * Finds the perfect subset of peers to get the entire file with the rarest first prefered.
	 */
	public boolean startPeers() {
		findList();
		return gotEntireBitField;
	}


	/**makes a list of subset of peers with bitfields adding up to the entire file with rarest peers first*/
	private void findList() {
		List<Peer> subset= PeerConnectionsInfo.subsetDPeers;
		int numChunks = ConnectToTracker.torrentI.piece_hashes.length;
		bitFieldDS[] bfDS = new bitFieldDS[numChunks];
		bitFieldDS node = new bitFieldDS();
		/**for each chunk add up the number of peers that have that bit field to true*/
		for (int j = 0; j<numChunks; j++){ 
			for (int i = 0; i<PeerConnectionsInfo.downloadPeers.size();i++){
				if(PeerConnectionsInfo.downloadPeers.get(i).boolBitField[j]==true){
					bfDS[j]= node;
					bfDS[j].sum++;
					bfDS[j].lp.add(PeerConnectionsInfo.downloadPeers.get(i));
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
					bf[i]=true;
				}
			}
			Random rand = new Random();
			if (!inSubset(subset, bfDS[index])){
				subset.add(bfDS[index].lp.get(rand.nextInt(bfDS[index].lp.size()-0)+1)); //get first peer that has that index
			}//gets random peer in the list of peers and make sure they are not already in the peers to connect to
		}

		if (fullBitField(bf)==true){
			gotEntireBitField=true;
		}

	}

	private boolean inSubset(List<Peer> subset, bitFieldDS bfDS) {
		for (int i = 0 ; i< bfDS.lp.size(); i++){
			if (subset.contains(bfDS.lp.get(i))){
				return true;
			}
		}
		return false;
	}


	/**sees if bitfield is not full or not. if bitfield is not full then we need more chunks*/
	private boolean fullBitField(boolean[] bf){ 
		for (int i = 0; i< bf.length;i++){
			if ((FileChunks.ourBitField[i]==false) && (bf[i]==false)){
				return false;
			}
		}
		return true;
	}

	


	

	/** This method implements the optimistic unchoke functionality.
	 *  When the 30 second timer is up, it chooses the unchoked peer
	 *  with the worst throughput, unchokes it, and randomly picks a
	 *  choked peer as the optimistic choke.  
	 */

	public static void randomUnchoke(){

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


	public void makeThreads() {
		Peer temp;
		Thread thread;
		for(int i = 0; i< PeerConnectionsInfo.subsetDPeers.size(); i++){
			try {
				temp = PeerConnectionsInfo.subsetDPeers.get(i);
				temp.sendInterested();
				thread = new Thread(temp);
				thread.start();
			} catch (Exception e) {
				System.out.println("Error: Could not create Thread to run!");
			} 
		}
	}

	public boolean extractPeers(ArrayList list) throws IOException {

		HashMap peer_Map = null;
		String peerID ="", peerIP = "";
		int peerPort = 0;
		boolean found = false;
		//System.out.println(list.size());
		for(int i = 0; i < list.size(); i++){
			peer_Map = (HashMap)list.get(i);
			peerIP = new String(((ByteBuffer)peer_Map.get(ConnectToTracker.KEY_PEER_IP)).array());
			peerID = new String(((ByteBuffer)peer_Map.get(ConnectToTracker.KEY_PEER_ID)).array());
			peerPort = (int)peer_Map.get(ConnectToTracker.KEY_PEER_PORT);
			//System.out.println(peerIP);
			if((peerIP.equalsIgnoreCase("128.6.171.131")) || (peerIP.equalsIgnoreCase("128.6.171.130")) ) {

				found = true;
				Peer temp = new Peer(peerIP, ((ByteBuffer)peer_Map.get(ConnectToTracker.KEY_PEER_ID)).array(), peerPort);
				if(temp.openConnection()){
					//System.out.println("IP: "+ peerIP+ "port: "+ peerPort);
					int len = temp.is.readInt();
					byte read = temp.is.readByte();
					temp.getBitField(read); 
					if(temp.boolBitField.length==0){
						System.out.println("OH NO!! SIZE 0");
					}
					if(temp.boolBitField!=null){
						PeerConnectionsInfo.peers.put(temp.boolBitField, temp);
						PeerConnectionsInfo.downloadPeers.add(temp);
					}else{
						temp.closeConnection();
					}
				}
			}
		}
		return found;
	}

	public int getTrackerInterval(HashMap response) {

		int trackInterval;

		if(response.containsKey(ConnectToTracker.KEY_MIN_INTERVAL)){
			trackInterval= (int)response.get(ConnectToTracker.KEY_MIN_INTERVAL);
		}else{
			trackInterval= ((int)response.get(ConnectToTracker.KEY_INTERVAL))/2;
			if (trackInterval>180){/**cap at 180 seconds*/
				trackInterval=180;
			}
		}
		return trackInterval;
	}

	public void closeAllConnections() {
		for (int j = 0; j< PeerConnectionsInfo.downloadPeers.size();j++){
			PeerConnectionsInfo.downloadPeers.get(j).closeConnection();
		}
	}
}