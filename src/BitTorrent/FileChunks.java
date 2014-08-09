package BitTorrent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class FileChunks {

	/**Final file that is output*/
	private FileOutputStream fileoutput;
	/**Final name of the output file as given as the second argument to the program*/
	private String fileOutArg;
	/**Array of chunks to be stored*/
	private ArrayList<byte[]> chunks = new ArrayList<byte[]>();
	/**Boolean version of our bitfield which will be the most up to date by the download peer*/
	public static boolean[] ourBitField;
	
	
	public FileChunks(String fileName) {
		this.fileOutArg = fileName;
	}

	
	/**
	 * COnverts the boolean bit field to a byte array for upload peer.
	 * */
	public byte[] booleanToByteBitField(boolean [] bf){
		byte[] bitfield= new byte[bf.length/8];
		for (int i = 0; i<bf.length; i++){ //bf.length = bitfield*8
			if(bf[i] == true){
				bitfield[i/8] = (byte)1;
			}else{
				bitfield[i/8] = (byte)0;
			}
		}
		return bitfield;
	}
	
	/**
	 * Saves the chunks downloaded to the output file specified by the user's argument.
	 * */
	public void saveToFile() throws IOException {
		System.out.println("Writing to File.");
		try {
			fileoutput = new FileOutputStream(new File(this.fileOutArg));
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