package BitTorrent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class FileChunks {

	/**Final file that is output*/
	private static FileOutputStream fileoutput;
	/**Final name of the output file as given as the second argument to the program*/
	private static String fileOutArg;
	/**Array of chunks to be stored*/
	public static ArrayList<byte[]> chunks = new ArrayList<byte[]>();
	/**Boolean version of our bitfield which will be the most up to date by the download peer*/
	public static boolean[] ourBitField;
	
	
	public FileChunks(String fileName) {
		this.fileOutArg = fileName;
	}

	
	/**
	 * COnverts the boolean bit field to a byte array for upload peer.
	 * */
	public static byte[] booleanToByteBitField(boolean [] bf){
		byte[] ba= new byte[bf.length/8];
		
		for (int i = 0; i<ba.length; i++){
			ba[i]=0;
		}
		int count = 0;
		int sum=0;
		for (int i = 0; i<bf.length; i++){ //bf.length = bitfield*8
			//byte[] bits = new byte[]({byte) (bf[i]?1:0));
			count++;
			if(bf[i] == true){
				sum= sum + (int)Math.pow(2,7-(i%8));
				if(count == 8) {
					ba[i/8] = (byte)sum;
					System.out.println("Sum: "+ sum);
					sum = 0;
					count = 1;
				}
			}
		}
		return ba;
	}
	
	/**
	 * Saves the chunks downloaded to the output file specified by the user's argument.
	 * */
	public static void saveToFile() throws IOException {
		System.out.println("Writing to File.");
		try {
			fileoutput = new FileOutputStream(new File(fileOutArg));
		} catch (FileNotFoundException e) {
			System.out.println("Error: could not open file to save data to.");
		}
		for (int i = 0; i<chunks.size();i++){ /**writes all chunks to file*/
			fileoutput.write(chunks.get(i));
		}
		fileoutput.close();
		System.out.println("Done Writing to File.");
	}
}