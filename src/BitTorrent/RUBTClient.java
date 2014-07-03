package BitTorrent;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
		byte[] torrentFile = new byte[(int)torrent_file.length()];
		try { //converts file into byte array
			torrentFile = Files.readAllBytes(torrent_file.toPath());
		} catch (IOException e) {
			System.out.println("Error: Could not convert file into bytes.");
			return;
		}
		
		
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
		//System.out.println("Tracker's url:" +trackerUrlString);
		
		URL trackerURL;
		try {
			trackerURL = new URL(trackerURLString);
		} catch (MalformedURLException e) {
			System.out.println("Error: Could not convert from string to tracker url.");
			return;
		}
		sendMessagetoTracker();
	}

	/* ????????????????????????????????????????????????????????????????????????????????????????????????????????????????*/
	private static Object sendMessagetoTracker() {
		
		
		
		String sentURL = torrentI.announce_url.toString();
		String infoHash;
		String peerID;
		String port = "6881";
		String uploaded = "0", downloaded = "0";
		String left = "" + torrentI.file_length;
		
		
		
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(sentURL).openConnection();
		} catch (IOException e) {
			System.out.println("Error: Could not connect to tracker");
			return null;
		}
		try {
			DataInputStream trackerResponse = new DataInputStream(connection.getInputStream());
		} catch (IOException e) {
			System.out.println("Error: Cannot get tracker response.");
			return null;
		}
		
	}

	

}