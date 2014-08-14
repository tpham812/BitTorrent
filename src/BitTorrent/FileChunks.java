package BitTorrent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * This class holds chunk information and can save chunks to file when download has completed
 * @author Amulya Uppala, Truong Pham, Jewel Lim
 *
 */
public class FileChunks {


	/**Final name of the output file as given as the second argument to the program*/
	private static String fileOutArg = ConnectToTracker.fileoutArg;
	/**Array of chunks to be stored*/
	public static ArrayList<byte[]> chunks = new ArrayList<byte[]>();
	/**Boolean version of our bitfield which will be the most up to date by the download peer*/
	public static boolean[] ourBitField = new boolean[ConnectToTracker.torrentI.piece_hashes.length];

	
	/**
	 * Converts the boolean bit field to a byte array for upload peer.
	 * @param bf bit filed array to be converted to boolean array
	 * @return returns byte array representation of the boolean array
	 **/
	public static byte[] booleanToByteBitField(boolean [] bf){
		BitSet bits = new BitSet(bf.length);
		
		byte[] ba = new byte[(bits.length() + 7) / 8];
		
		for (int i = 0; i<bf.length; i++){
			bits.clear(i); //makes the bitset to 0
		}
		for (int i=0; i<bits.length(); i++) {
	        if (bits.get(i)) {
	            ba[ba.length-i/8-1] |= 1<<(i%8);
	        }
	    }
		return ba;
	}
	
	/**
	 * Saves the chunks downloaded to the output file specified by the user's argument.
	 * @throws IOException
	 * */
	public static void saveToFile() throws IOException {
		/**Final file that is output*/
		FileOutputStream  fileoutput;
		System.out.println("Writing to File.");
		try {
			fileoutput = new FileOutputStream(new File(fileOutArg));
			for (int i = 0; i<chunks.size();i++){ /**writes all chunks to file*/
				fileoutput.write(chunks.get(i));
			}
			fileoutput.close();
			System.out.println("Done Writing to File.");
		} catch (FileNotFoundException e) {
			System.out.println("Error: could not open file to save data to.");
		}
	}
}