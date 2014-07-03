package BitTorrent;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class RUBTClient {

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
		byte[] torrentFile = new byte[(int)torrent_file.length()];
		try { //converts file into byte array
			torrentFile = Files.readAllBytes(torrent_file.toPath());
		} catch (IOException e) {
			System.out.println("Error: Could not convert file into bytes.");
			return;
		}
		
		TorrentInfo torrentI; 
		try { //creates torrentinfo object
			torrentI = new TorrentInfo(torrentFile);
		} catch (BencodingException e) {
			System.out.println("Error: Could not create torrentinfo object.");
			return;
		}
		
		Map<ByteBuffer,Object> torrentmeta = torrentI.torrent_file_map;
		
		ByteBuffer trackerUrlByte = (ByteBuffer)torrentmeta.get(TorrentInfo.KEY_ANNOUNCE);
		HashMap info = (HashMap)torrentmeta.get(TorrentInfo.KEY_INFO);
		//get tracker url and info metadata 
		String trackerUrlString="";
		try {
			trackerUrlString = new String(trackerUrlByte.array(),"ASCII");
		} catch (UnsupportedEncodingException e) {
			System.out.println("Error: Could not convert Tracker's URL Byte Array into STring");
			return;
		}
		//System.out.println("Tracker's url:" +trackerUrlString);
		
		URL trackerURL;
		try {
			trackerURL = new URL(trackerUrlString);
		} catch (MalformedURLException e) {
			System.out.println("Error: Could not convert from string to tracker url");
			return;
		}
		sendMessagetoTracker(trackerURL);
		
		
		
	}

	private static void sendMessagetoTracker(URL trackerURL) {
		
		
	}

	

}