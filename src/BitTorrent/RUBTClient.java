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
		ConnectToTracker ct = new ConnectToTracker(torrent_file);		
	}
}