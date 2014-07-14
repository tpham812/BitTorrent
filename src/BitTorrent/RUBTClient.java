package BitTorrent;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

public class RUBTClient {

	public static void main(String[] args) throws NoSuchAlgorithmException, BencodingException, UnsupportedEncodingException, MalformedURLException {

		if (args.length!=2){
			System.out.println("Error: Need 2 arguments; the name of the torrent file and the name of the file to save the data to.");
			return;
		} //have both arguments
		else if (args[0].trim().length()==0){
			System.out.println("Error: Invalid torrent file name.");
			return;
		}else if (args[1].trim().length()==0){
			System.out.println("Error: Invalid name of file you wish to store data to.");
			return;
		}
		//open torrent file and parse data using torrentInfo.java...
		File torrent_File = new File(args[0].trim());
		if(!torrent_File.exists()) {
			System.out.println("Error: File not found.");
			return;
		}
		
		System.out.println("Starting download.");
		startDownload(torrent_File, args[1]);	
		System.out.println("Finished download.");
	}
	
	public static void startDownload(File torrent_File, String fileName) {
		
		HashMap peer_Map = null;
		ArrayList list = null;
		String peerID ="", peerIP = "";
		boolean found = false;
		int peerPort = 0;
		
		ConnectToTracker ct = new ConnectToTracker();
		list = ct.getTrackerResponse(torrent_File, fileName);
		do {
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
			if(!found) {
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
					System.out.println("Error: Thread is unable to sleep for 5 secs");
					ct.disconnect();
					return;
				}
				System.out.println("Getting new list");
				try {
					list = ct.requestNewReponse();
				} catch (Exception e) {
					System.out.println("Error: tracker message could not be obtained.");
					return;
				}
			}
		}while(!found);
		ct.disconnect();
		try {
			Peer peer = new Peer(peerIP, ((ByteBuffer)peer_Map.get(ConnectToTracker.KEY_PEER_ID)).array(), peerPort, fileName);
			peer.downloadFileFromPeer();
		} catch (Exception e) {
			System.out.println("Error: Cannot create Peer.");
		}
	}
}