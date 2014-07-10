package BitTorrent;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
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

	static TorrentInfo torrentI; 
	static ByteBuffer infoHash;
	public final static ByteBuffer KEY_PEERS = ByteBuffer.wrap(new byte[]{ 'p', 'e', 'e', 'r','s'});
	public final static ByteBuffer KEY_PEER_PORT = ByteBuffer.wrap(new byte[]{ 'p', 'o', 'r', 't'});
	public final static ByteBuffer KEY_PEER_IP = ByteBuffer.wrap(new byte[]{ 'i', 'p'});
	public final static ByteBuffer KEY_PEER_ID = ByteBuffer.wrap(new byte[]{'p', 'e', 'e', 'r', ' ', 'i', 'd'});

	public ConnectToTracker(File torrent_file) {

		getTrackerResponse(torrent_file);
	}
	public void getTrackerResponse(File torrent_file) {

		HashMap trackerAnswer;
		HttpURLConnection connection;

		byte[] torrentFile = Helper.getBytesFromFile(torrent_file); //get byte array of file
		try { //creates torrentinfo object and stores other stuff
			torrentI = new TorrentInfo(torrentFile);
		} catch (BencodingException e) {
			System.out.println("Error: Could not create torrentinfo object.");
			return;
		}

		Map<ByteBuffer,Object> torrentmeta = torrentI.torrent_file_map; //gets the map from the object
		Map<ByteBuffer,Object> info = (Map<ByteBuffer,Object>)torrentmeta.get(TorrentInfo.KEY_INFO);

		infoHash = torrentI.info_hash; 

		try {
			connection = sendMessagetoTracker();
			trackerAnswer = getMessageFromTracker(connection);
		} catch (Exception e) {
			System.out.println("Error: tracker message could not be obtained.");
			return;
		}

		ArrayList l = (ArrayList)trackerAnswer.get(KEY_PEERS);
		for(int i=0;i<l.size();i++){
			HashMap peer_map = (HashMap)l.get(i);
			System.out.println(new String(((ByteBuffer)peer_map.get(KEY_PEER_IP)).array()));
			System.out.println(new String(((ByteBuffer)peer_map.get(KEY_PEER_ID)).array()));
		}


	}

	/**
	 * Sends formatted URL message to tracker, connects to tracker and returns the decoded answer of tracker
	 * @param trackerURL URL of the tracker to use in message to send to tracker
	 * @return Decoded message of Tracker as hashmap
	 * @throws UnsupportedEncodingException
	 * @throws BencodingException
	 * @throws NoSuchAlgorithmException
	 */
	private HttpURLConnection sendMessagetoTracker() throws UnsupportedEncodingException, BencodingException, NoSuchAlgorithmException {
		//tracker URL = URL of tracker obtained from announce in torrent metadata.
		//peerID = 20 string length of alphanumeric = randomized each time
		//port is 6881 -> 6889 
		//0 bytes uploaded and downloaded.
		//left = length of file
		//event = starting = first request must hav this. stopped = if client shutting down, completed = if have complete download\
		HttpURLConnection connection = null;
		int portNumber = 6880;
		URL trackerURL = torrentI.announce_url;
		String peerID = Helper.generateRandomPeerID();
		String finalMessage;
		String uploaded = "0", downloaded = "0";
		String left = "" + torrentI.file_length;
		String event = "started";

		do
		{
			finalMessage = trackerURL+"?info_hash="+Helper.escape(new String(infoHash.array(),"ISO-8859-1"))+"&peer_id="+peerID+"&port="+Integer.toString(portNumber+1)+"&uploaded="
					+uploaded+"&downloaded="+downloaded+"&left="+left+"&event="+event; 
			try {
				connection = (HttpURLConnection) new URL(finalMessage).openConnection();
			} catch (IOException e) {
				System.out.println("Error: Could not connect to tracker");
				return null;
			}
		}while(connection == null);

		return connection;
		
	}
	
	private HashMap getMessageFromTracker(HttpURLConnection connection) {
		
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
