package BitTorrent;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class RUBTClient {

	static TorrentInfo torrentI; 
	static ByteBuffer infoHash;
	public final static ByteBuffer KEY_PEERS = ByteBuffer.wrap(new byte[]{ 'p', 'e', 'e', 'r','s'});
	public final static ByteBuffer KEY_PEER_PORT = ByteBuffer.wrap(new byte[]{ 'p', 'o', 'r', 't'});
	public final static ByteBuffer KEY_PEER_IP = ByteBuffer.wrap(new byte[]{ 'i', 'p'});

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
		File torrent_file = new File(args[0].trim());
		if(!torrent_file.exists()) {
			System.out.println("Error: File not found.");
			return;
		}
		byte[] torrentFile = getBytesFromFile(torrent_file); //get byte array of file

		try { //creates torrentinfo object and stores other stuff
			torrentI = new TorrentInfo(torrentFile);
		} catch (BencodingException e) {
			System.out.println("Error: Could not create torrentinfo object.");
			return;
		}

		Map<ByteBuffer,Object> torrentmeta = torrentI.torrent_file_map; //gets the map from the object


		Map<ByteBuffer,Object> info = (Map<ByteBuffer,Object>)torrentmeta.get(TorrentInfo.KEY_INFO);
		//get tracker url and info metadata 


		String fileName = torrentI.file_name;

		infoHash = torrentI.info_hash; 
		//System.out.println(escape(new String(infoHash.array(),"ISO-8859-1")));

		HashMap trackerAnswer;
		try {
			trackerAnswer = sendMessagetoTracker();
		} catch (UnsupportedEncodingException e) {
			System.out.println("Error: tracker message could not be obtained.");
			return;
		}

		ArrayList l = (ArrayList)trackerAnswer.get(KEY_PEERS);
		for(int i=0;i<l.size();i++){
			HashMap peer_map = (HashMap)l.get(i);
			System.out.println(new String(((ByteBuffer)peer_map.get(KEY_PEER_IP)).array()));

		}

	}
	/**
	 * Takes file and turns it into byte array
	 * @param file 
	 * @return
	 */
	@SuppressWarnings("resource")
	private static byte[] getBytesFromFile(File file) {
		InputStream file_stream;
		byte[] b = new byte[(int)file.length()];
		try {
			file_stream=new FileInputStream(file);
			file_stream.read(b);
		} catch (IOException e) {
			System.out.println("ERROR: bad function fixed");
		}
		return b;
	}
	/**
	 * Sends formatted URL message to tracker, connects to tracker and returns the decoded answer of tracker
	 * @param trackerURL URL of the tracker to use in message to send to tracker
	 * @return Decoded message of Tracker as hashmap
	 * @throws UnsupportedEncodingException
	 * @throws BencodingException
	 * @throws NoSuchAlgorithmException
	 */
	private static HashMap sendMessagetoTracker() throws UnsupportedEncodingException, BencodingException, NoSuchAlgorithmException {
		//tracker URL = URL of tracker obtained from announce in torrent metadata.
		//peerID = 20 string length of alphanumeric = randomized each time
		//port is 6881 -> 6889 
		//0 bytes uploaded and downloaded.
		//left = length of file
		//event = starting = first request must hav this. stopped = if client shutting down, completed = if have complete download
		URL trackerURL = torrentI.announce_url;
		String peerID = generateRandomPeerID();//GEenrate random peer id for every time!!!!!!!!!!!!!!!!!!!!!!!!!!
		String port = "6881";
		String uploaded = "0", downloaded = "0";
		String left = "" + torrentI.file_length;
		String event = "started";

		//final formatted message to tracker sent. 
		String finalMessage = trackerURL+"?info_hash="+escape(new String(infoHash.array(),"ISO-8859-1"))+"&peer_id="+peerID+"&port="+port+"&uploaded="
				+uploaded+"&downloaded="+downloaded+"&left="+left+"&event="+event; 
		//System.out.println("Final Message:"+finalMessage);

		//create Http connection to tracker
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(finalMessage).openConnection();
		} catch (IOException e) {
			System.out.println("Error: Could not connect to tracker");
			return null;
		}

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
		} catch (IOException e) {
			System.out.println("Error: Cannot get tracker response.");
			return null;
		}
		return tracker_decoded_response;

	}
	/**
	 * Formats a given string into ISO-8859-1 convention and returns a string.
	 * @param s string to format
	 * @return string formatted in ISO-8859-1 standard
	 */
	private static String escape(String s){
		String ret;

		try {
			ret = URLEncoder.encode(s,"ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			System.out.println("Error: Cannot convert to iso standard;");
			return null;
		}
		return ret;

	}
	/**
	 * Generates random ISO formatted, 20 length, alphanumeric string
	 * @return ISO formatted peer ID that does not start with RUBT
	 */
	private static String generateRandomPeerID() {
		String peerID;
		//peer id cannot start with RUBT
		String alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random rand = new Random();
		do{
			StringBuilder sb = new StringBuilder(20); //length 20
			for (int i= 0; i<20;i++){
				sb.append(alpha.charAt(rand.nextInt(alpha.length())));
			}
			peerID = sb.toString();

		}while (peerID.startsWith("RUBT"));
		return escape(peerID);

	}



}