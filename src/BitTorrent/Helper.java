package BitTorrent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;
/**
 * This class holds some helper functions that can be useful in multiple files.
 * @author Amulya Uppala, Jewel Lim, Truong Pham
 *
 */

public class Helper {

	/**
	 * Takes file and turns it into byte array
	 * @param file 
	 * @return byte array of the file
	 */
	@SuppressWarnings("resource")
	public static byte[] getBytesFromFile(File file) {

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
	 * Formats a given string into ISO-8859-1 convention and returns a string.
	 * @param s string to format
	 * @return string formatted in ISO-8859-1 standard
	 */
	public static String escape(String s){

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
	public static String generateRandomPeerID() {

		String peerID;
		String alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random rand = new Random();
		do{
			StringBuilder sb = new StringBuilder(20);
			for (int i= 0; i<20;i++){
				sb.append(alpha.charAt(rand.nextInt(alpha.length())));
			}
			peerID = sb.toString();

		}while (peerID.startsWith("RUBT"));
		return escape(peerID);
	}
	
	/**
	 * Changes the integer into a big-endian byte array. 
	 * */
	public static byte[] intToByteArray(int value) {

		byte[] returnValue = new byte[4];
		returnValue[0] = (byte) (value >> 24);
		returnValue[1] = (byte) (value >> 16);
		returnValue[2] = (byte) (value >> 8);
		returnValue[3] = (byte) (value);
		return returnValue;
	}
}