package BitTorrent;


import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
/**
 * This class connects to a peer via socket and download chunks from the file requested.
 * It saves all the chunks and writes them to the output file. 
 * @author Amulya Uppala, Truong Pham
 *
 */

public class Download implements Runnable {

	/**Array of chunks to be stored*/
	private ArrayList<byte[]> chunks = new ArrayList<byte[]>();
	/**Used to avoid duplicate chunks by storing in this array*/
	private ByteBuffer[] chunksHashes; 
	boolean[] boolhaveChunks;
	boolean[] peerChunks;
	private Peer peer;
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
		this.boolhaveChunks= new boolean[FileChunks.ourBitField.length];
		for (int i = 0; i<boolhaveChunks.length;i++){
			boolhaveChunks[i] = FileChunks.ourBitField[i];
		}
		this.peerChunks = peer.boolBitField;
	}

	/**
	 * Download file from peer by sending interested message after we have verified bitfield
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void downloadFileFromPeer() throws IOException, InterruptedException {

		/**keeps track of bytes*/
		int begin = 0;
		/**Keeps track of current block number*/
		int block = 0;


		int index = peer.torrentI.piece_length; /**Length of piece*/

		Message interested = new Message(1,(byte)2); /**create interested message*/

		System.out.println("Writing message to peer.");
		peer.os.write(interested.message);
		peer.os.flush();/**push message to stream*/
		System.out.println("Finished writing message to peer.");

		int read;

		System.out.println("Reading message from peer.");
		read = peer.readMessage();
		System.out.println("Finished reading message from peer.");
		if (read==1){
			System.out.println("Unchoked. Downloading chunks.");
		}else if (read==-2){
			/**choked and timed out so destory connection*/
			peer.closeConnection(); 
			return;
		}

		this.chunksHashes= new ByteBuffer[peer.torrentI.piece_hashes.length];

		System.out.println("Started downloading chunks.");

		ConnectToTracker.sendMessageToTracker(Event.sendStartedEvent(), "started");
		for(int i = 0; i<this.boolhaveChunks.length;i++){
			if((fc.ourBitField[i]==false) && (peerChunks[i]==true)){
				block = i;

				if (block==peer.torrentI.piece_hashes.length-1){ /**LAST PIECE*/
					int left = peer.torrentI.piece_hashes.length-1;
					int lastSize = peer.torrentI.file_length - (left*peer.torrentI.piece_length);/**cuz last pieces might be irregurarly sized*/
					if (lastSize<index){
						index = lastSize;
					}else{
						index = peer.torrentI.piece_length;
					}
				}
				begin = ConnectToTracker.torrentI.piece_length*block;

				System.out.println("index, begin, block: "+index+","+begin+","+block);
				requestPiece(index,begin,block);
				if(boolhaveChunks[i]==false){
					i--; //rerequest same block!!!
				}else{
					boolhaveChunks[i]=true;
				}
			}
		}
		for (int z = 0; z<FileChunks.ourBitField.length;z++){
			FileChunks.ourBitField[z]=boolhaveChunks[z];
			//updates the filechunks bitfield based on our temporary bitfield since download is done
		}
		for (int f = 0; f< this.chunks.size();f++){
			FileChunks.chunks.add(f, this.chunks.get(f)); 
			//adds the currently downloaded chunks to the file chunks since download is done.
		}
		
		System.out.println("Finished downloading chunks.");
		fc.saveToFile();
		ConnectToTracker.sendMessageToTracker(Event.sendCompletedEvent(), "completed");
		peer.closeConnection();
		return;
	}
	/***
	 * Requests the piece
	 * @param index integer specifying the requested length
	 * @param begin integer specifying the zero-based byte offset within the piece 
	 * @param block integer specifying the zero-based piece index
	 * @throws IOException 
	 */
	private void requestPiece(int index, int begin, int block) throws IOException {
		/**Message to peer telling it that we have received the piece*/
		Message have;
		/**Message to peer for what block to send.*/
		Message askForPieces;
		/**temporary buffer to place bytes we don't use right now*/
		byte[] tempBuff;
		/**Current chunk being downloaded*/
		byte[] chunk;

		askForPieces = new Message(13,(byte)6); 
		askForPieces.setPayload(index,begin,block); /**ask for piece*/
		peer.os.write(askForPieces.message);
		peer.os.flush();


		tempBuff = new byte[4];  /**remove unnecessary bytes*/
		for (int k = 0; k<4;k++){
			tempBuff[k]=peer.is.readByte();
		}
		int id = peer.is.readInt();
		if (id == 0){ //set got chocked variable and timeout till unchocked
			peer.isChoked = true;
			while (peer.isChoked==true){
				peer.socket.setSoTimeout(60000); //take this out because of next line?????!!!!!!!!
				int read = peer.readMessage();//get stuck here till we get a message unchocking us.
				if(read==1){//got unchocked!
					peer.isChoked=false;
				}
			}
			//!!!!!!!!!!!!!we have to wait til we are unchocked!!!!! so timeout time = ??????????
		}
		chunk = new byte[index];
		for (int l = 0; l<5;l++){
			peer.is.readByte();
		}

		for (int m = 0 ; m<index;m++){
			chunk[m]=peer.is.readByte(); 
		}/**read in chunk*/

		byte[] chunkHash = null;
		if (verify(block,chunk, chunkHash)== 0){
		}else{
			/**add the chunk if correct*/
			boolhaveChunks[block]= true;
			this.chunksHashes[block] = ByteBuffer.wrap(chunkHash);
			this.chunks.add(chunk); /**add to array*/
			ConnectToTracker.updateAmounts(chunk.length);
			ConnectToTracker.sendMessageToTracker(null, null);
			have = new Message(5,(byte)4);
			have.setPayload(index, begin, block);
			writeHavetoAllPeers(have);
		}
	}

	private int verify(int block, byte[] chunk, byte[] chunkHash) {
		int verify = 1; 
		byte[] trackerHash = peer.torrentI.piece_hashes[block].array();
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error: Could not SHA-1");
			peer.closeConnection();
			return -1;
		}
		digest.update(chunk);
		chunkHash = digest.digest();

		/**verify that the sha-1 hashes match with what is in the .torrent file*/
		if (trackerHash.length!=chunkHash.length){
			System.out.println("Error: SHA-1 lengths are not same!");
			verify = 0;
		}
		for (int d = 0; d<chunkHash.length;d++){
			if (chunkHash[d]!=trackerHash[d]){
				System.out.println("Error: Hashes are not same!");
				verify = 0;
				break;
			}
		}
		if (alreadyInArray(chunkHash) == true){
			System.out.println("The chunk given is a duplicate chunk. Program will request chunk from Peer again.");
			verify = 0;
		}
		return verify;
	}

	/**sends have message to all peers when we have a chunk stored!*/
	private void writeHavetoAllPeers(Message have) {

		for (int i = 0; i<PeerConnectionsInfo.subsetDPeers.size();i++){
			try {
				PeerConnectionsInfo.subsetDPeers.get(i).os.write(have.message);
				PeerConnectionsInfo.subsetDPeers.get(i).os.flush();
			} catch (IOException e) {
				System.out.println("Error: Cannot send have message to peer");
			}
		}
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





	@Override
	public void run() {
	
		try {
			downloadFileFromPeer();
		} catch (Exception e) {
			System.out.println("Error: Could not download file from peer!");
		}
	}
}