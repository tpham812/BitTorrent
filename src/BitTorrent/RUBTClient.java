package BitTorrent;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class starts up the Bit Torrent client and begins download
 * @author Truong Pham
 *
 */
public class RUBTClient {

	/**
	 * Main method
	 * @param args
	 * @throws NoSuchAlgorithmException
	 * @throws BencodingException
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException, BencodingException, UnsupportedEncodingException, MalformedURLException {

		/**Check if user entered in 2 arguments*/
		if (args.length!=2){
			System.out.println("Error: Need 2 arguments; the name of the torrent file and the name of the file to save the data to.");
			return;
		} 
		/**Check if user entered in torrent file*/
		else if (args[0].trim().length()==0){
			System.out.println("Error: Invalid torrent file name.");
			return;
		/**Check if user entered in file name to store picture in*/
		}else if (args[1].trim().length()==0){
			System.out.println("Error: Invalid name of file you wish to store data to.");
			return;
		}
		/**Open torrent file and parse data using torrentInfo.java*/
		File torrent_File = new File(args[0].trim());
		if(!torrent_File.exists()) {
			System.out.println("Error: File not found.");
			return;
		}
		
		System.out.println("Starting download.");
		/**Start download */
		startDownload(torrent_File, args[1]);	
		System.out.println("Finished download.");
	}
	
	/**
	 * Connect to tracker and get a list of peers. Find the right peer and connects to it to begin download
	 * @param torrent_File Torrent file
	 * @param fileName File name to store 
	 */
	public static void startDownload(File torrent_File, String fileName) {
		
		HashMap peer_Map = null;
		ArrayList list = null;
		String peerID ="", peerIP = "";
		boolean found = false;
		int peerPort = 0;
		
		ConnectToTracker ct = new ConnectToTracker();
		list = ct.getTrackerResponse(torrent_File, fileName); /**Get tracker response*/
		
		/**Request new tracker response if peer RU1103 is not found*/
		do {
			/**Look for peer RU1103 from list*/
			for(int i = 0; i < list.size(); i++){
				peer_Map = (HashMap)list.get(i);
				peerIP = new String(((ByteBuffer)peer_Map.get(ConnectToTracker.KEY_PEER_IP)).array());
				peerID = new String(((ByteBuffer)peer_Map.get(ConnectToTracker.KEY_PEER_ID)).array());
				peerPort = (int)peer_Map.get(ConnectToTracker.KEY_PEER_PORT);	
				if(peerID.contains("RU1103")) {
					found = true;
					break;
				}
			}
			
			/**If peer can't be found, sleep and then request a new response from tracker*/
			if(!found) {
				try {
					Thread.sleep(5000); /**Sleep*/
				} catch (Exception e) {
					System.out.println("Error: Thread is unable to sleep for 5 secs");
					ct.disconnect();
					return;
				}
				System.out.println("Getting new list");
				try {
					list = ct.requestNewReponse(); /**Request new response*/
				} catch (Exception e) {
					System.out.println("Error: tracker message could not be obtained.");
					return;
				}
			}
		}while(!found);
		ct.disconnect(); /**Disconnect from tracker*/
		try {
			/**Connect to peer*/
			Peer peer = new Peer(peerIP, ((ByteBuffer)peer_Map.get(ConnectToTracker.KEY_PEER_ID)).array(), peerPort, fileName);
			/**Download file from peer*/
			peer.downloadFileFromPeer();
		} catch (Exception e) {
			System.out.println("Error: Cannot create Peer.");
		}
	}
}