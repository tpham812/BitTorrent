package BitTorrent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConnectToTracker {

	
	private String finalMessage;
	private HttpURLConnection connection;
	public static TorrentInfo torrentI; 
	private ByteBuffer infoHash;
	BufferedInputStream trackerResponse;
	ByteArrayOutputStream temp_output;
	public final static ByteBuffer KEY_PEERS = ByteBuffer.wrap(new byte[]{ 'p', 'e', 'e', 'r','s'});
	public final static ByteBuffer KEY_PEER_PORT = ByteBuffer.wrap(new byte[]{ 'p', 'o', 'r', 't'});
	public final static ByteBuffer KEY_PEER_IP = ByteBuffer.wrap(new byte[]{ 'i', 'p'});
	public final static ByteBuffer KEY_PEER_ID = ByteBuffer.wrap(new byte[]{'p', 'e', 'e', 'r', ' ', 'i', 'd'});

	public ConnectToTracker(File torrent_file, String file) {

		connection = null;
		torrentI = null;
		infoHash = null;
		getTrackerResponse(torrent_file, file);
	}
	public void getTrackerResponse(File torrent_file, String file) {

		HashMap peer_map = null;
		HashMap trackerAnswer;
		String peerIP = "", peerID = "";
		boolean found = false;
		int peerPort = 0;

		byte[] torrentFile = Helper.getBytesFromFile(torrent_file); //get byte array of file
		try { //creates torrentinfo object and stores other stuff
			torrentI = new TorrentInfo(torrentFile);
		} catch (Exception e) {
			System.out.println("Error: Could not create torrentinfo object.");
			return;
		}

		Map<ByteBuffer,Object> torrentmeta = torrentI.torrent_file_map; //gets the map from the object
		Map<ByteBuffer,Object> info = (Map<ByteBuffer,Object>)torrentmeta.get(TorrentInfo.KEY_INFO);

		infoHash = torrentI.info_hash; 

		try {
			sendMessageToTracker();
			trackerAnswer = getMessageFromTracker();
		} catch (Exception e) {
			System.out.println("Error: tracker message could not be obtained.");
			return;
		}
		do {
			ArrayList list = (ArrayList)trackerAnswer.get(KEY_PEERS);

			
			for(int i = 0; i < list.size(); i++){
				peer_map = (HashMap)list.get(i);
				peerIP = new String(((ByteBuffer)peer_map.get(KEY_PEER_IP)).array());
				peerID = new String(((ByteBuffer)peer_map.get(KEY_PEER_ID)).array());
				peerPort = (int)peer_map.get(KEY_PEER_PORT);	
				System.out.println(peerID);
				System.out.println(peerIP);
				/*if(peerIP.equals("128.6.171.130") && peerID.contains("RU1103")) {
					found = true;
					break;
				}*/
			}
			if(!found) {
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
					System.out.println("Error: Thread is unable to sleep for 5 secs");
					connection.disconnect();
					return;
				}
				System.out.println("Getting new list");
				try {
					sendMessageToTracker();
					trackerAnswer = getMessageFromTracker();
				} catch (Exception e) {
					System.out.println("Error: tracker message could not be obtained.");
					return;
				}
			}
		}while(!found);
		connection.disconnect();
		
		Peer peer = new Peer(peerIP, peerPort, ((ByteBuffer)peer_map.get(KEY_PEER_ID)).array(), file);
	}

	/**
	 * Sends formatted URL message to tracker, connects to tracker and returns the decoded answer of tracker
	 * @param trackerURL URL of the tracker to use in message to send to tracker
	 * @return Decoded message of Tracker as hashmap
	 * @throws UnsupportedEncodingException
	 * @throws BencodingException
	 * @throws NoSuchAlgorithmException
	 */
	private void sendMessageToTracker() throws UnsupportedEncodingException, BencodingException, NoSuchAlgorithmException {
		//tracker URL = URL of tracker obtained from announce in torrent metadata.
		//peerID = 20 string length of alphanumeric = randomized each time
		//port is 6881 -> 6889 
		//0 bytes uploaded and downloaded.
		//left = length of file
		//event = starting = first request must hav this. stopped = if client shutting down, completed = if have complete download\
		int portNumber = 6880;
		URL trackerURL = torrentI.announce_url;
		String peerID = Helper.generateRandomPeerID();
		
		String uploaded = "0", downloaded = "0";
		String left = "" + torrentI.file_length;
		String event = "started";

		do
		{
			finalMessage = trackerURL+"?info_hash="+Helper.escape(new String(infoHash.array(),"ISO-8859-1"))+"&peer_id="+peerID+"&port="+Integer.toString(portNumber+1)+"&uploaded="
					+uploaded+"&downloaded="+downloaded+"&left="+left+"&event="+event; 
			try {
				connection = (HttpURLConnection) new URL(finalMessage).openConnection();
			} catch (Exception e) {
				System.out.println("Error: Could not connect to tracker");
				return;
			}
		}while(connection == null);
	}

	
	public HashMap getMessageFromTracker() {

		//get tracker response, decode it and extract list of peers and their ids.
		HashMap tracker_decoded_response = null;
		try {
			BufferedInputStream trackerResponse = new BufferedInputStream(connection.getInputStream());
			ByteArrayOutputStream temp_output = new ByteArrayOutputStream();
			byte[] buf = new byte[1];
			while (trackerResponse.read(buf)!=-1){
				temp_output.write(buf);
			}
			byte[] trackerAnswer = temp_output.toByteArray();
			//System.out.println("Tracker Answer:"+new String(trackerAnswer));
			tracker_decoded_response =  (HashMap)Bencoder2.decode(trackerAnswer);
			//decode tracker response and return it.
		} catch (Exception e) {
			System.out.println("Error: Cannot get tracker response.");
			return null;
		}
		return tracker_decoded_response;
	}
}