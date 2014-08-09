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
import java.net.SocketException;
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

public class Download implements Runnable {
	
	/**Final file that is output*/
	private FileOutputStream fileoutput;
	/**Final name of the output file as given as the second argument to the program*/
	private String fileOutArg;
	/**Array of chunks to be stored*/
	private ArrayList<byte[]> chunks = new ArrayList<byte[]>();
	/**Used to avoid duplicate chunks by storing in this array*/
	private ByteBuffer[] chunksHashes; 
	
	private static Peer peer;
	
	private FileChunks fc;	

	/**
	 * Constructor that sets fields
	 * @param ip peer IP
	 * @param id peer ID
	 * @param port peer port
	 * @param fileOutArg file to save chunks to
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Download(Peer peer, FileChunks fc) throws IOException, InterruptedException{

		this.peer = peer;
		this.fc = fc;
	}

	/**
	 * Download file from peer by sending interested message after we have verified bitfield
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
		int index = peer.torrentI.piece_length; /**Length of piece*/
		
		Message interested = new Message(1,(byte)2); /**create interested message*/

		System.out.println("Writing message to peer.");
		peer.os.write(interested.message);
		peer.os.flush();/**push message to stream*/
		System.out.println("Finished writing message to peer.");
		
		int read;
		
		System.out.println("Reading message from peer.");
		read = readMessage();
		System.out.println("Finished reading message from peer.");
		if (read==1){
			System.out.println("Unchoked. Downloading chunks.");
		}else if (read==-2){
			/**choked and timed out so destory connection*/
			peer.closeConnection(); //send stopped to tracker??!!
			return;
			//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		}

		left = peer.torrentI.piece_hashes.length-1;
		lastSize = peer.torrentI.file_length - (left*peer.torrentI.piece_length);/**cuz last pieces might be irregurarly sized*/
		this.chunksHashes= new ByteBuffer[peer.torrentI.piece_hashes.length];

		System.out.println("Started downloading chunks.");

		ConnectToTracker.sendMessageToTracker(Event.sendStartedEvent(), "started");
		while (block!=peer.torrentI.piece_hashes.length){
			System.out.println("index, begin, block: "+index+","+begin+","+block);
			if (block==peer.torrentI.piece_hashes.length-1){ /**LAST PIECE*/
				askForPieces = new Message(13,(byte)6); 
				if (lastSize<index){
					index = lastSize;
				}else{
					index = peer.torrentI.piece_length;
				}
				lastSize = lastSize-index; 
				askForPieces.setPayload(index,begin,block); /**ask for piece*/
				peer.os.write(askForPieces.message);
				peer.os.flush();
				
				//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				tempBuff = new byte[4];  /**remove unnecessary bytes*/
				for (int k = 0; k<4;k++){
					tempBuff[k]=peer.is.readByte();
				}
				chunk = new byte[index];
				for (int l = 0; l<9;l++){
					peer.is.readByte();
				}

				for (int m = 0 ; m<index;m++){
					chunk[m]=peer.is.readByte(); 
				}/**read in chunk*/
				byte[] trackerHash = peer.torrentI.piece_hashes[block].array();
				MessageDigest digest = null;
				try {
					digest = MessageDigest.getInstance("SHA-1");
				} catch (NoSuchAlgorithmException e) {
					System.out.println("Error: Could not SHA-1");
					peer.closeConnection();
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
					fc.ourBitField[block]=true;
					this.chunksHashes[block] = ByteBuffer.wrap(chunkHash);
					this.chunks.add(chunk); /**add to array*/
					ConnectToTracker.updateAmounts(chunk.length);
					ConnectToTracker.sendMessageToTracker(null, null);
					have = new Message(5,(byte)4);
					have.setPayload(index, begin, block);
					peer.os.write(have.message);
					peer.os.flush();/**push message to stream*/
				}
				block++;

			}else{ /**still have more pieces left!*/
				/**ask for the piece*/
				askForPieces = new Message(13,(byte)6); 
				askForPieces.setPayload(index,begin,block);

				peer.os.write(askForPieces.message);
				peer.os.flush(); /**push to output stream.*/

				//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				tempBuff = new byte[4];  /**read in first four bytes which we don't use*/
				for (int k = 0; k<4;k++){
					tempBuff[k]=peer.is.readByte();
				}

				chunk = new byte[index]; /**create piece length size chunk*/
				for (int l = 0; l<9;l++){
					peer.is.readByte();
				}
				for (int m = 0 ; m<index;m++){
					chunk[m]=peer.is.readByte(); 
				}/**read in chunk*/

				byte[] trackerHash = peer.torrentI.piece_hashes[block].array();
				MessageDigest digest = null;
				try {
					digest = MessageDigest.getInstance("SHA-1");
				} catch (Exception e) {
					System.out.println("Error: Could not SHA-1");
					peer.closeConnection();
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
					fc.ourBitField[block]=true;
					this.chunksHashes[block] = ByteBuffer.wrap(chunkHash);
					this.chunks.add(chunk); /**add to array*/
					ConnectToTracker.updateAmounts(chunk.length);
					ConnectToTracker.sendMessageToTracker(null, null);
					have = new Message(5,(byte)4);
					have.setPayload(index, begin, block);
					peer.os.write(have.message);
					peer.os.flush();/**push message to stream*/
					if (begin+index==peer.torrentI.piece_length){
						block++;
						begin = 0;
					}else{
						begin +=index;
					}	
				}
			}			
		}
		System.out.println("Finished downloading chunks.");
		fc.saveToFile();
		ConnectToTracker.sendMessageToTracker(Event.sendCompletedEvent(), "completed");
		peer.closeConnection();
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
	 * Deals with choking if peer chokes us. It waits till we get unchoked or will terminate.
	 * @throws SocketException
	 */
	private void gotChoked() throws SocketException{
		peer.isChoked = true;
		peer.socket.setSoTimeout(60000); /**time out for 1 minute to get unchoked else destroy connection*/
		do{
			try {
				if (readMessage()==1){
					System.out.println("Got unchoked before time interval ended.");
					peer.isChoked=false;
					return;
				}
			} catch (IOException e) {
				System.out.println("Timed out waiting to be unchoked! Finishing Connection.");
				return;
			}
		}while (peer.isChoked==true);
	}
	/**
	 * Read in message from peer which are formatted based on message type.
	 * We need id of the message to identify what type of message was sent. 
	 * @return message byte as int is returned 
	 * @throws IOException
	 */
	private byte readMessage() throws IOException {

		/**Read in message*/
		int msgLength = peer.is.readInt();
		/**Read in id*/
		byte id = peer.is.readByte();

		/**keep-alive*/
		if(msgLength == 0){
			return -1;
		}
		switch(id){
		case 0://choked
			System.out.println("Choked on ip: "+peer.ip+" on port: "+peer.port);
			gotChoked(); 
			if (peer.isChoked==true){
				return -2;
			}
		case 1://unchoked
			peer.isChoked=false;
			return id;
		case 7: //piece
			int index = peer.is.readInt();
			int begin = peer.is.readInt();	
		default: return id;
		}
	}


	@Override
	public void run() {
		try {
			downloadFileFromPeer();
		} catch (Exception e) {
			System.out.println("Error: Could not download file from peer!");
		}
	}
}