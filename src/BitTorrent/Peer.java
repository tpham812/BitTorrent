package BitTorrent;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;
/**
 * This class connects to a peer via socket and download chunks from the file requested.
 * It saves all the chunks and writes them to the output file. 
 * @author Amulya Uppala, Truong Pham
 *
 */
public class Peer {
	/**Socket connection to the peer*/
	private Socket socket;
	/**Final file that is output*/
	private FileOutputStream fileoutput;
	/**Output stream to the peer*/
	private DataOutputStream os;
	/**Input stream from the peer*/
	private DataInputStream in;
	/**Torrent Info file that comes from the .torrent file*/
	private TorrentInfo torrentInfo;
	/**Info hash of the info about the file to be downloaded from the .torrent file*/
	private byte[] infoHash;
	/**IP address of peer*/
	private String IP;
	/**Port number of peer*/
	private int port;
	/**ID of the peer*/
	private byte[] ID;
	/**RUBT Client's id that was sent to tracker and must be sent to peer to identify us*/
	private byte[] ourID;
	/**Final name of the output file as given as the second argument to the program*/
	private String fileOutArg;
	/**Array of chunks to be stored*/
	private ArrayList<byte[]> chunks = new ArrayList<byte[]>();
	/**Protocol name to write in the handshake message*/
	private final static byte[] BitProtocol = new byte[]{'B','i','t','T','o','r','r','e','n','t',' ','P','r','o','t','o','c','o','l'};
	/**Eight zeroes field to write in the handshake message*/
	private final static  byte[] eightZeros = new byte[]{'0','0','0','0','0','0','0','0'};
	/**Used to avoid duplicate chunks by storing in this array*/
	private ByteBuffer[] chunksHashes; 

	/**
	 * Constructor that sets fields
	 * @param ip peer IP
	 * @param id peer ID
	 * @param port peer port
	 * @param fileOutArg file to save chunks to
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Peer(String ip, byte[] id, int port, String fileOutArg) throws IOException, InterruptedException{

		this.IP = ip;
		this.ID = id;
		this.port = port;
		this.torrentInfo = ConnectToTracker.torrentI;
		this.infoHash = torrentInfo.info_hash.array();
		this.fileOutArg = fileOutArg;
		this.ourID = ConnectToTracker.toSendToPeerID;
	}

	/**
	 * Download file from peer
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void downloadFileFromPeer() throws IOException, InterruptedException {

		/**Current chunk being downloaded*/
		byte[] chunk;
		/**temporary buffer to place bytes we don't use right now*/
		byte[] tempBuff;
		/**last chunk*/
		int left;
		/**last chunk's size*/
		int lastSize;
		/**keeps track of bytes*/
		int begin = 0;
		/**Keeps track of current block number*/
		int block = 0;
		/**Message to peer for what block to send.*/
		Message askForPieces;
		/**Message to peer telling it that we have received the piece*/
		Message have;
		int index = torrentInfo.piece_length; /**Length of piece*/

		/**Start handshaking with peer*/
		handShake(); 
		Message interested = new Message(1,(byte)2); /**create interested message*/

		System.out.println("Reading message from peer.");
		int read = readMessage();
		System.out.println("Finished reading message from peer.");
		byte[] bitField = new byte[in.available()];
		if ((read==5)||(read==4)){ /**have or bitfield message being sent. */
			/**part 1 = only have bit field message due to one peer with everything only. */
			in.readFully(bitField); /**get rid of bit field*/
		}

		System.out.println("Writing message to peer.");
		os.write(interested.message);
		os.flush();/**push message to stream*/
		System.out.println("Finished writing message to peer.");

		System.out.println("Reading message from peer.");
		read = readMessage();
		System.out.println("Finished reading message from peer.");
		if (read==1){
			System.out.println("Unchoked. Downloading chunks.");
		}

		left = torrentInfo.piece_hashes.length-1;
		lastSize = torrentInfo.file_length - (left*torrentInfo.piece_length);/**cuz last pieces might be irregurarly sized*/
		this.chunksHashes= new ByteBuffer[torrentInfo.piece_hashes.length];

		System.out.println("Started downloading chunks.");

		ConnectToTracker.sendMessageToTracker(Event.sendStartedEvent(), "started");
		while (block!=torrentInfo.piece_hashes.length){
			System.out.println("index, begin, block: "+index+","+begin+","+block);
			if (block==torrentInfo.piece_hashes.length-1){ /**LAST PIECE*/
				askForPieces = new Message(13,(byte)6); 
				if (lastSize<index){
					index = lastSize;
				}else{
					index = torrentInfo.piece_length;
				}
				lastSize = lastSize-index; 
				askForPieces.setPayload(index,begin,block); /**ask for piece*/
				os.write(askForPieces.message);
				os.flush();
				tempBuff = new byte[4];  /**remove unnecessary bytes*/
				for (int k = 0; k<4;k++){
					tempBuff[k]=in.readByte();
				}
				chunk = new byte[index];
				for (int l = 0; l<9;l++){
					in.readByte();
				}

				for (int m = 0 ; m<index;m++){
					chunk[m]=in.readByte(); 
				}/**read in chunk*/
				byte[] trackerHash = torrentInfo.piece_hashes[block].array();
				MessageDigest digest = null;
				try {
					digest = MessageDigest.getInstance("SHA-1");
				} catch (NoSuchAlgorithmException e) {
					System.out.println("Error: Could not SHA-1");
					finishConnection();
					return;
				}
				digest.update(chunk);
				byte[] chunkHash = digest.digest();
				boolean verify = true; 
				/**verify that the sha-1 hashes match with what is in the .torrent file*/
				if (trackerHash.length!=chunkHash.length){
					System.out.println("Error: SHA-1 lengths are not same!");
					verify = false;
				}
				for (int d = 0; d<chunkHash.length;d++){
					if (chunkHash[d]!=trackerHash[d]){
						System.out.println("Error: Hashes are not same!");
						verify = false;
						break;
					}
				}
				if (alreadyInArray(chunkHash) == true){
					System.out.println("The chunk given is a duplicate chunk. Program will request chunk from Peer again.");
					verify = false;
				}

				if (verify == false){
					block --;
				}else{
					/**add the chunk and write to the file if correct*/
					this.chunksHashes[block] = ByteBuffer.wrap(chunkHash);
					this.chunks.add(chunk); /**add to array*/
					ConnectToTracker.updateAmounts(chunk.length);
					ConnectToTracker.sendMessageToTracker(null, null);
					have = new Message(5,(byte)4);
					have.setPayload(index, begin, block);
					os.write(have.message);
					os.flush();/**push message to stream*/
				}
				block++;

			}else{ /**still have more pieces left!*/
				/**ask for the piece*/
				askForPieces = new Message(13,(byte)6); 
				askForPieces.setPayload(index,begin,block);

				os.write(askForPieces.message);
				os.flush(); /**push to output stream.*/

				tempBuff = new byte[4];  /**read in first four bytes which we don't use*/
				for (int k = 0; k<4;k++){
					tempBuff[k]=in.readByte();
				}

				chunk = new byte[index]; /**create piece length size chunk*/
				for (int l = 0; l<9;l++){
					in.readByte();
				}
				for (int m = 0 ; m<index;m++){
					chunk[m]=in.readByte(); 
				}/**read in chunk*/

				byte[] trackerHash = torrentInfo.piece_hashes[block].array();
				MessageDigest digest = null;
				try {
					digest = MessageDigest.getInstance("SHA-1");
				} catch (Exception e) {
					System.out.println("Error: Could not SHA-1");
					finishConnection();
					return;
				}
				digest.update(chunk);
				byte[] chunkHash = digest.digest();
				boolean verify = true;
				/**verify that the hashes are correct*/
				if (trackerHash.length!=chunkHash.length){
					System.out.println("Error: SHA-1 lengths are not same!");
					verify = false;
				}
				for (int d = 0; d<chunkHash.length;d++){
					if (chunkHash[d]!=trackerHash[d]){
						System.out.println("Error: Hashes are not same!");
						verify = false;
						break;
					}
				}
				if (alreadyInArray(chunkHash) == true){
					System.out.println("The chunk given is a duplicate chunk. Program will request chunk from Peer again.");
					verify = false;
				}

				if (verify == false){
					/**do not change block because we need to request again.*/
				}else{
					/**add chunk to array and write to file and send have message*/
					this.chunksHashes[block] = ByteBuffer.wrap(chunkHash);
					this.chunks.add(chunk); /**add to array*/
					ConnectToTracker.updateAmounts(chunk.length);
					ConnectToTracker.sendMessageToTracker(null, null);
					have = new Message(5,(byte)4);
					have.setPayload(index, begin, block);
					os.write(have.message);
					os.flush();/**push message to stream*/
					if (begin+index==torrentInfo.piece_length){
						block++;
						begin = 0;
					}else{
						begin +=index;
					}	
				}
			}			
		}
		System.out.println("Finished downloading chunks.");
		saveToFile();
		ConnectToTracker.sendMessageToTracker(Event.sendCompletedEvent(), "completed");
		finishConnection();
		return;
	}
	/**
	 * Makes sure the chunk given is not already present in the Hashes array 
	 * This helps avoid duplicate chunks.
	 * */
	private boolean alreadyInArray(byte[] chunkHash) {
		for (int i = 0; i<chunksHashes.length; i++){
			if ((chunksHashes[i]!=null)&&(areEqual(chunksHashes[i].array(), chunkHash)==true)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the byte arrays are equal and returns true if they are.
	 * */
	private boolean areEqual(byte[] array, byte[] chunkHash) {
		if (array.length!=chunkHash.length){
			return false;
		}
		for (int j = 0; j<chunkHash.length; j++){
			if (array[j]!=chunkHash[j]){
				return false;
			}
		}
		return true;
	}

	/**
	 * Saves the chunks downloaded to the output file specified by the user's argument.
	 * */
	private void saveToFile() throws IOException {
		System.out.println("Writing to File.");
		try {
			fileoutput = new FileOutputStream(new File(this.fileOutArg));
		} catch (FileNotFoundException e) {
			System.out.println("Error: could not open file to save data to.");
			socket.close();
			in.close();
			os.close();
		}
		for (int i = 0; i<chunks.size();i++){ /**writes all chunks to file*/
			fileoutput.write(chunks.get(i));
		}
		System.out.println("Done Writing to File.");
	}

	/**
	 * Handshake with peer by sending hand shake message and receiving handshake message back from peer. 
	 * Verify if the info hash peer sends back is the same and if the their id is the same as tracker given id
	 */
	private void handShake(){

		boolean peerInfoGood = true;
		/**Construct message to send to peer*/
		byte[] message = new byte[68];
		message[0] = (byte)19;
		System.arraycopy(BitProtocol, 0,message,1,19);
		System.arraycopy(eightZeros, 0, message, 20, 8);
		System.arraycopy(infoHash,0, message, 28, 20);
		System.arraycopy(ourID, 0, message, 48, 20);

		try {
			System.out.println("Connecting to Peer.");

			/**Open connection by using a socket*/
			socket = new Socket(IP,port);
			System.out.println("Connected.");
			/**Create input and output stream*/
			os = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
		} catch (Exception e) {
			System.out.println("Error: Could not Open Socket to Peer.");
		} 

		if (socket==null){ /**bad host name given.*/
			System.out.println("Error: Peer Socket was unable to be created due to bad hostname/IP address or bad port number given. Please try again.");
			finishConnection();
			return;
		}
		System.out.println("Starting handshake.");
		try {
			/**Initiate handshake by sending message to peer*/
			os.write(message); 
			os.flush(); 

			/**Get reply from peer*/
			byte[] peerAns = new byte[68];
			in.readFully(peerAns);

			byte[] peerInfoHash = Arrays.copyOfRange(peerAns, 28, 48); 

			/**checks if peer's info hash returned is same as info has we have from tracker*/
			for (int i = 0; i<20;i++){
				if (peerInfoHash[i]!=infoHash[i]){
					System.out.println("Error: Peer's info hash returned from handshake is not same.");
					finishConnection();
					peerInfoGood = false;
					break;
				}		
			}
			/**If peer info given does not match as tracker's given, exit (already closed connections before)*/
			if (peerInfoGood==false){
				return;
			}
			/**Check if peer id is same as the tracker given peer id*/
			byte[] peerIDCheck = Arrays.copyOfRange(peerAns, 48, 68);
			for (int n = 0; n <20;n++){
				if (ID[n]!=peerIDCheck[n]){
					System.out.println("Error: Peer id is not same as given from tracker.");
					finishConnection();
					peerInfoGood = false;
					break;
				}
			}
			/**If peer info given does not match as tracker's given, exit (already closed connections before)*/
			if (peerInfoGood==false){
				return;
			}
		} catch (Exception e) {
			System.out.println("Error: Could not handshake with peer.");
			finishConnection();
			return;
		}
		System.out.println("finished handshake.");
	}

	/**
	 * Read in message from peer which are formatted based on message type.
	 * We need id of the message to identify what type of message was sent. 
	 * @return message byte as int is returned 
	 * @throws IOException
	 */
	private byte readMessage() throws IOException {

		/**Read in message*/
		int msgLength = in.readInt();
		/**Read in id*/
		byte id = in.readByte();

		/**keep-alive*/
		if(msgLength == 0){
			return -1;
		}
		switch(id){
		case 7: 
			int index = in.readInt();
			int begin = in.readInt();	
		default: return id;
		}
	}

	/** 
	 * Close connection and streams and output file.
	 */
	private void finishConnection() {
		System.out.println("Closing socket and data streams.");
		try {
			/**Close socket and streams*/
			socket.close();
			in.close();
			os.close();
			fileoutput.close();
		} catch (Exception e) {
			System.out.println("Error: Could not close data streams!");
			return;
		}
	}
}