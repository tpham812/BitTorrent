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
/**
 * This class connects to tracker and returns a decoded tracker response 
 * @author Amulya Uppala, Truong Pham, Jewel Lim
 *
 */

public class ConnectToTracker {

	/**Stores port number*/
	private static int portNumber;
	/**Stores generated peerID*/
	private static String peerID;
	/**Stores how much uploaded*/
	private static int uploaded = 0;
	/**Stores how much downloaded*/
	private static int downloaded = 0;
	/**Stores how much more need to be downloaded*/
	private static int left;
	/**Stores tracker URL*/
	private static URL trackerURL;
	/**Stores message to be sent to tracker*/
	private static String finalMessage;
	/**Connection to connect to tracker*/
	private static HttpURLConnection connection;
	/**TorrentInfo to access torrent information*/
	public static TorrentInfo torrentI; 
	/**Stores infohash*/
	private static ByteBuffer infoHash;
	/**Generated peer ID*/
	public static byte[] toSendToPeerID;
	/**Key used to retrieve peer list*/
	public final static ByteBuffer KEY_PEERS = ByteBuffer.wrap(new byte[]{ 'p', 'e', 'e', 'r','s'});
	/**Key used to retrieve peer port number*/
	public final static ByteBuffer KEY_PEER_PORT = ByteBuffer.wrap(new byte[]{ 'p', 'o', 'r', 't'});
	/**Key used to retrieve peer IP*/
	public final static ByteBuffer KEY_PEER_IP = ByteBuffer.wrap(new byte[]{ 'i', 'p'});
	/**Key used to retrieve peer ID*/
	public final static ByteBuffer KEY_PEER_ID = ByteBuffer.wrap(new byte[]{'p', 'e', 'e', 'r', ' ', 'i', 'd'});

	/**
	 * Connect to tracker and receive a tracker response
	 * @param torrent_file Torrent file
	 * @param file File to save data to
	 * @return response of the tracker in an array list
	 */
	public HashMap getTrackerResponse(File torrent_file, String fileName) {

		HashMap trackerAnswer = null;
		System.out.println("Connecting to tracker. Please wait.");
		/**Get byte array of torrent file*/
		byte[] torrentFile = Helper.getBytesFromFile(torrent_file); 
		try { 
			/**Creates torrentinfo object*/
			torrentI = new TorrentInfo(torrentFile);
		} catch (Exception e) {
			System.out.println("Error: Could not create torrentinfo object.");
			return null;
		}

		/**Infohash*/
		infoHash = torrentI.info_hash; 
		try {
			/**Send message to tracker*/
			sendMessageToTracker();
			/**Get tracker response*/
			trackerAnswer = getMessageFromTracker(); 
		} catch (Exception e) {
			System.out.println("Error: tracker message could not be obtained.");
			return null;
		}
		return trackerAnswer;
	}

	/**
	 * Request a new response from tracker. Sends message to tracker and receive a decoded response
	 * @return returns a new reponse from tracker in an array list
	 */
	public ArrayList requestNewReponse() {

		HashMap trackerAnswer = null;
		try {
			/**Send message to tracker */
			sendMessageToTracker();
			/**Decoded response from tracker */
			trackerAnswer = getMessageFromTracker();
		} catch (Exception e) {
			System.out.println("Error: tracker message could not be obtained.");
		}
		return (ArrayList)trackerAnswer.get(KEY_PEERS);
	}

	/**
	 * Sends formatted URL message to tracker, connects to tracker and returns the decoded answer of tracker
	 * @return Decoded message of Tracker as hashmap
	 * @throws UnsupportedEncodingException
	 * @throws BencodingException
	 * @throws NoSuchAlgorithmException
	 */
	private void sendMessageToTracker() throws UnsupportedEncodingException, BencodingException, NoSuchAlgorithmException {

		portNumber = 6880;
		trackerURL = torrentI.announce_url;
		peerID = Helper.generateRandomPeerID();
		toSendToPeerID = peerID.getBytes();
		left = torrentI.file_length;

		System.out.println("Sending message to Tracker.");
		do
		{
			portNumber++;
			/**Message to send to tracker*/
			finalMessage = trackerURL+"?info_hash="+Helper.escape(new String(infoHash.array(),"ISO-8859-1"))+"&peer_id="+peerID+"&port="+portNumber+"&uploaded="
					+uploaded+"&downloaded="+downloaded+"&left="+left;
			try {
				/**Open up connection to tracker*/
				connection = (HttpURLConnection) new URL(finalMessage).openConnection();
			} catch (Exception e) {
				System.out.println("Error: Could not send message to tracker");
				return;
			}
		}while(connection == null);
		connection.disconnect(); /**Disconnect from tracker */
	}
	
	/**
	 * Send even message or regular message to tracker
	 * @param event Event message to concatenate for GET request
	 * @param nameEvent Name of the event
	 * @throws UnsupportedEncodingException
	 */
	public static void sendMessageToTracker(String event, String nameEvent) throws UnsupportedEncodingException {

		if(event == null) {
			System.out.println("Sending message to Tracker.");
			try {
				/**Open up connection to tracker*/
				connection = (HttpURLConnection) new URL(finalMessage).openConnection();
			} catch (Exception e) {
				System.out.println("Error: Could not send message to tracker");
				return;
			}
			connection.disconnect(); /**Disconnect from tracker*/
		}
		else {
			System.out.println("Sending " + nameEvent + " event message to Tracker.");
			try {
				/**Open up connection to tracker*/
				connection = (HttpURLConnection) new URL(finalMessage + event).openConnection();
			} catch (Exception e) {
				System.out.println("Error: Could not send message to tracker");
				return;
			}
			connection.disconnect(); /**Disconnect from tracker*/
		}
	}

	/**
	 * Receive a message from tracker and return a decoded response
	 * @return Decoded response in a hash map
	 */
	public HashMap getMessageFromTracker() {

		//get tracker response, decode it and extract list of peers and their ids.
		HashMap tracker_decoded_response = null;
		System.out.println("Getting response from Tracker.");
		try {
			BufferedInputStream trackerResponse = new BufferedInputStream(connection.getInputStream());
			ByteArrayOutputStream temp_output = new ByteArrayOutputStream();
			byte[] buf = new byte[1];

			/**Read in tracker response*/
			while (trackerResponse.read(buf)!=-1){
				temp_output.write(buf);
			}

			/**Store tracker response in byte array*/
			byte[] trackerAnswer = temp_output.toByteArray();
			System.out.println("Starting decoding response.");

			/**Decode tracker response*/
			tracker_decoded_response =  (HashMap)Bencoder2.decode(trackerAnswer);
			System.out.println("Finsihed decoding response.");
		} catch (Exception e) {
			System.out.println("Error: Cannot get tracker response.");
			return null;
		}
		System.out.println("Got response.");
		return tracker_decoded_response;
	}

	/**
	 * Update downloaded and left
	 * @param amount Amount just downloaded
	 */
	public static void updateAmounts(int amount) {

		downloaded = downloaded + amount;
		left = left - downloaded;
	}
}