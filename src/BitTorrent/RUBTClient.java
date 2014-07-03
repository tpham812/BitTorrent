package BitTorrent;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class RUBTClient {

	static TorrentInfo torrentI; 

	public static void main(String[] args) {

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
		byte[] torrentFile = getBytesFromFile(torrent_file); 
		//= new byte[(int)torrent_file.length()];
		/*try { //converts file into byte array
			torrentFile = Files.readAllBytes(torrent_file.toPath());
		} catch (IOException e) {
			System.out.println("Error: Could not convert file into bytes.");
			return;
		}*/


		try { //creates torrentinfo object
			torrentI = new TorrentInfo(torrentFile);
		} catch (BencodingException e) {
			System.out.println("Error: Could not create torrentinfo object.");
			return;
		}

		Map<ByteBuffer,Object> torrentmeta = torrentI.torrent_file_map;

		ByteBuffer trackerURLByte = (ByteBuffer)torrentmeta.get(TorrentInfo.KEY_ANNOUNCE);
		HashMap info = (HashMap)torrentmeta.get(TorrentInfo.KEY_INFO);
		//get tracker url and info metadata 
		String trackerURLString="";
		try {
			trackerURLString = new String(trackerURLByte.array(),"ASCII");

		} catch (UnsupportedEncodingException e) {
			System.out.println("Error: Could not convert Tracker's URL Byte Array into String.");
			return;
		}
		//System.out.println("Tracker's url:" +trackerURLString);

		URL trackerURL;
		try {
			trackerURL = new URL(trackerURLString);
		} catch (MalformedURLException e) {
			System.out.println("Error: Could not convert from string to tracker url.");
			return;
		}
		try {
			sendMessagetoTracker(trackerURL);
		} catch (UnsupportedEncodingException e) {
			System.out.println("Error: tracker message could not be obtained.");
			return;
		}
		/*if (trackerReply == null){ //error messages printed already so quit.
			return;
		}*/

	}

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

	private static Object sendMessagetoTracker(URL trackerURL) throws UnsupportedEncodingException {

		String sentURL = torrentI.announce_url.toString();
		//System.out.println("Announce URL:"+sentURL);
		ByteBuffer infoHashMap = (ByteBuffer)torrentI.info_hash;
		String infoHash;
		infoHash= escape(new String(infoHashMap.array()));
		System.out.println("infoHash:"+infoHash);
		String peerID = generateRandomPeerID();//GEenrate random peer id for every time!!!!!!!!!!!!!!!!!!!!!!!!!!
		int i = sentURL.indexOf("//");
		String temp = sentURL.substring(i+2);
		int j = temp.indexOf(":");
		int k = temp.indexOf("/");
		String port = "6881";
		//System.out.println("Port:"+port);
		String uploaded = "0", downloaded = "0";
		String left = "" + torrentI.file_length;
		//System.out.println("Left:"+left);
		String event = "started";

		//sentURL +="?info_hash="+infoHash+"&peer_id="+peerID+"&port="+port+"&uploaded="
		//	+uploaded+"&downloaded="+downloaded+"&left="+left+"&event="+event;
		String finalMessage = trackerURL+"?info_hash="+infoHash+"&peer_id="+peerID+"&port="+port+"&uploaded="
				+uploaded+"&downloaded="+downloaded+"&left="+left+"&event="+event; 
		System.out.println("Final Message:"+finalMessage);
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(finalMessage).openConnection();
		} catch (IOException e) {
			System.out.println("Error: Could not connect to tracker");
			return null;
		}
		try {
			BufferedInputStream trackerResponse = new BufferedInputStream(connection.getInputStream());
			ByteArrayOutputStream temp_output = new ByteArrayOutputStream();
			byte[] buf = new byte[1];
			while (trackerResponse.read(buf)!=-1){
				temp_output.write(buf);
			}
			byte[] trackerAnswer = temp_output.toByteArray();
			System.out.println("Tracker Answer:"+new String(trackerAnswer));

		} catch (IOException e) {
			System.out.println("Error: Cannot get tracker response.");
			return null;
		}
		return null;

	}
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
	private static String generateRandomPeerID() {
		return escape("GvdxnngWwbBpHRpCkrNP");

	}



}