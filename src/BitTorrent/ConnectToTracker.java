package BitTorrent;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

public class ConnectToTracker {
	
	private String finalMessage;
	private HttpURLConnection connection;
	public static TorrentInfo torrentI; 
	private ByteBuffer infoHash;
	public static byte[] toSendToPeerID;
	public final static ByteBuffer KEY_PEERS = ByteBuffer.wrap(new byte[]{ 'p', 'e', 'e', 'r','s'});
	public final static ByteBuffer KEY_PEER_PORT = ByteBuffer.wrap(new byte[]{ 'p', 'o', 'r', 't'});
	public final static ByteBuffer KEY_PEER_IP = ByteBuffer.wrap(new byte[]{ 'i', 'p'});
	public final static ByteBuffer KEY_PEER_ID = ByteBuffer.wrap(new byte[]{'p', 'e', 'e', 'r', ' ', 'i', 'd'});

	public ArrayList getTrackerResponse(File torrent_file, String file) {

		HashMap trackerAnswer;
		System.out.println("Connecting to tracker. Please wait.");
		byte[] torrentFile = Helper.getBytesFromFile(torrent_file); //get byte array of file
		try { //creates torrentinfo object and stores other stuff
			torrentI = new TorrentInfo(torrentFile);
		} catch (Exception e) {
			System.out.println("Error: Could not create torrentinfo object.");
			return null;
		}

		infoHash = torrentI.info_hash; 

		try {
			sendMessageToTracker();
			trackerAnswer = getMessageFromTracker();
		} catch (Exception e) {
			System.out.println("Error: tracker message could not be obtained.");
			return null;
		}
		return (ArrayList)trackerAnswer.get(KEY_PEERS);
	}
	
	public ArrayList requestNewReponse() {
		
		HashMap trackerAnswer = null;
		try {
			sendMessageToTracker();
			trackerAnswer = getMessageFromTracker();
		} catch (Exception e) {
			System.out.println("Error: tracker message could not be obtained.");
		}
		
		return (ArrayList)trackerAnswer.get(KEY_PEERS);
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
		
		int portNumber = 6880;
		URL trackerURL = torrentI.announce_url;
		String peerID = Helper.generateRandomPeerID();
		toSendToPeerID = peerID.getBytes();
		String uploaded = "0", downloaded = "0";
		String left = "" + torrentI.file_length;
		String event = "started";

		System.out.println("Sending message to Tracker.");
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
		System.out.println("Getting response from Tracker.");
		try {
			BufferedInputStream trackerResponse = new BufferedInputStream(connection.getInputStream());
			ByteArrayOutputStream temp_output = new ByteArrayOutputStream();
			byte[] buf = new byte[1];
			while (trackerResponse.read(buf)!=-1){
				temp_output.write(buf);
			}
			byte[] trackerAnswer = temp_output.toByteArray();
			//System.out.println("Tracker Answer:"+new String(trackerAnswer));
			System.out.println("Starting decoding response.");
			tracker_decoded_response =  (HashMap)Bencoder2.decode(trackerAnswer);
			System.out.println("Finsihed decoding response.");
			//decode tracker response and return it.
		} catch (Exception e) {
			System.out.println("Error: Cannot get tracker response.");
			return null;
		}
		System.out.println("Got response.");
		return tracker_decoded_response;
	}
	
	public void disconnect() {
		
		connection.disconnect();
	}
}