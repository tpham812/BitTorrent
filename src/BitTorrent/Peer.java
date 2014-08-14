package BitTorrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class creates a peer thread for peers we are connected to for upload an download.
 * @author Amulya Uppala, Truong Pham, Jewel Lim
 *
 */
public class Peer implements Runnable{ 

	/** Boolean to stop thread */
	private boolean stopThread; 
	/** Boolean to see if peer choked us or not */
	boolean isChoked;
	/** Value of throughput of the peer */
	public double throughput; 
	/** Peer port number */
	public int port; 
	/** Peer ID */
	public byte[] id; 
	/** Peer IP */
	public String ip; 
	/** current index of the piece we are downloading */
	int currIndex; 
	/** beginning byte index of the piece we are downloading */
	int currBegin; 
	/** block we are downloading */
	int currBlock; 
	/** boolean to determine if we are done with downloading all pieces */
	boolean done; 
	/** output stream to write to the peer */
	public DataOutputStream os; 
	/** input stream coming to us */
	public DataInputStream is; 
	/** Our generated ID */
	public byte[] ourID; 
	/** Bitfield of the peer */
	public byte[] bitField; 
	/** Boolean array of the chunks the peer has */
	public boolean[] boolBitField; 
	/** TorrentI file corresponding to the file */
	public TorrentInfo torrentI; 
	/**Array of chunks to be stored*/ 
	private ArrayList<byte[]> chunks = new ArrayList<byte[]>();
	/** Used to avoid duplicate chunks by storing in this array*/
	private ByteBuffer[] chunksHashes; 
	/** Chunks we have acquired already but keep temporary so we can modify */
	boolean[] boolhaveChunks; 		
	/** Chunks the peer has but we keep temporarily so we can modify */
	boolean[] peerChunks; 				
	/**Socket connection to the peer*/
	public Socket socket;
	protected final byte[] BitProtocol = new byte[]{'B','i','t','T','o','r','r','e','n','t',' ','P','r','o','t','o','c','o','l'};
	/**Eight zeroes field to write in the handshake message*/
	protected final byte[] eightZeros = new byte[]{'0','0','0','0','0','0','0','0'};


	/**
	 * Constructor
	 * @param ip ip address of peer
	 * @param id id of peer
	 * @param port port number of peer
	 * @throws IOException
	 */
	public Peer(String ip, byte[] id, int port) throws IOException {
		this.port = port;
		this.ip = ip;
		this.id = id;
		this.isChoked = true;
		this.ourID = ConnectToTracker.ourPeerID;
		this.torrentI = ConnectToTracker.torrentI;
		this.stopThread = false;
		this.boolhaveChunks= new boolean[FileChunks.ourBitField.length];
		for (int i = 0; i<boolhaveChunks.length;i++){
			boolhaveChunks[i] = FileChunks.ourBitField[i];
		}

		done=false;
		this.chunksHashes= new ByteBuffer[torrentI.piece_hashes.length];
	}

	/**
	 * Upload a piece to peer after a request message.
	 * @param length requeeted length
	 * @param index piece index 
	 * @param begin byte offset to begin
	 */
	public void upload(int length, int index, int begin) throws IOException{

		Message pieceMsg;
		byte[] block;
		long startTime;
		long endTime;
		int fileLength = ConnectToTracker.torrentI.piece_hashes.length;
		int pieceLength = ConnectToTracker.torrentI.piece_length;
		int lastPieceLength = pieceLength - ((fileLength - 1)*pieceLength); 
		int lastPieceIndex = fileLength - 1;

		if(FileChunks.ourBitField[index] == true){
			block = new byte[length];
			System.arraycopy((FileChunks.booleanToByteBitField(FileChunks.ourBitField))[index], begin, block, 0, length);
			pieceMsg = new Message(9 + length, (byte) 7);
			pieceMsg.setPayload(index, begin, length, null);
			startTime = System.nanoTime();
			os.write(pieceMsg.message);
			os.flush();

			int msg2 = readMsg();
			if(msg2 == Message.MSG_HAVE){
				endTime = System.nanoTime();					
				if(index == lastPieceIndex){
					throughput = (double)lastPieceLength / ((endTime - startTime)/1000000000.0);						} 
				else {
					throughput = (double)pieceLength / ((endTime -startTime)/1000000000.0);						
				}
			}else{
				System.out.println("No Have message received after upload.");
				//need to end timer					
			}
		}
	}
	/**
	 * Request the piece from the peer by looping through the array of 
	 * things we don't have and the things they have. then we can create the message and send it to them.
	 * */
	public void requestPiece() throws IOException, InterruptedException {
		System.out.println("Requesting Pieces!!!");
		/**keeps track of bytes*/
		int begin = 0;
		/**Keeps track of current block number*/
		int block = 0;
		int index = torrentI.piece_length; /**Length of piece*/

		int read;

		System.out.println("Reading message from peer.");
		read = readMsg();  /** Check if we get chocked or unchoked */  
		System.out.println("Finished reading message from peer.");
		if (read==1){
			System.out.println("Unchoked. Downloading chunks.");
		}else if (read==-2){
			/** Choked and timed out so destroy connection*/
			closeConnection(); 
			return;
		}



		System.out.println("downloading chunks.");
		/**Message to  for what block to send.*/
		Message askForPieces;
		ConnectToTracker.sendMessageToTracker(Event.sendStartedEvent(), "started");
		for(int i = 0; i<this.boolhaveChunks.length;i++){
			if((FileChunks.ourBitField[i]==false) && (peerChunks[i]==true)){
				block = i;

				if (block==torrentI.piece_hashes.length-1){ /**LAST PIECE*/
					int left = torrentI.piece_hashes.length-1;
					int lastSize = torrentI.file_length - (left*torrentI.piece_length);/**cuz last pieces might be irregurarly sized*/
					if (lastSize<index){
						index = lastSize;
					}else{
						index = torrentI.piece_length;
					}
				}//last piece may have different size so calculate that. 
				begin = ConnectToTracker.torrentI.piece_length*block;

				System.out.println("index, begin, block: "+index+","+begin+","+block);
				askForPieces = new Message(13,(byte)6); 
				askForPieces.setPayload(index,begin,block, null); /**ask for piece*/
				os.write(askForPieces.message);
				os.flush();
				this.currIndex = index;
				this.currBegin = begin;
				this.currBlock = block;

				askForPieces = new Message(13,(byte)6); 
				askForPieces.setPayload(index,begin,block, null); /**ask for piece*/
				os.write(askForPieces.message);
				os.flush();

				if(boolhaveChunks[i]==false){
					i--; //rerequest same block!!!
				}else{
					boolhaveChunks[i]=true;
				}

				for (int z = 0; z<FileChunks.ourBitField.length;z++){
					FileChunks.ourBitField[z]=boolhaveChunks[z];
					/** Updates the filechunks bitfield based on our temporary bitfield since download is done */
				}
				for (int f = 0; f< this.chunks.size();f++){
					FileChunks.chunks.add(f, this.chunks.get(f)); 
					/** Adds the currently downloaded chunks to the file chunks since download is done. */
				}
				break; 
			}
		}
		this.done = true;
		return;
	}
	/**
	 * Sends interested message to send to peer so that we can get unchoked.
	 **/
	public void sendInterested() throws IOException{
		Message interested = new Message(1,(byte)2); /**create interested message*/
		System.out.println("Writing message to peer.");
		os.write(interested.message);
		os.flush();/**push message to stream*/
		System.out.println("Finished writing message to peer.");
	}


	/**
	 * Downloads the piece we have requested
	 * @throws IOException 
	 **/
	private void downloadPiece() throws IOException {
		/**Message to peer telling it that we have received the piece*/
		Message have;
		/**Current chunk being downloaded*/
		byte[] chunk;

		int index = is.readInt(); //the index of the piece and the begin byte index of the piece 
		int tempBegin = is.readInt();

		if ((index!=this.currBlock)||(tempBegin!=this.currBegin)){
			System.out.println("Error: upload send a differnt piece than was requested.");
			return;
		}
		chunk = new byte[this.currIndex];

		/** Chunk being read in */
		for (int m = 0 ; m<index;m++){ 
			chunk[m]=is.readByte(); 
		}/**read in chunk*/

		byte[] chunkHash = null;
		if (verify(currBlock,chunk, chunkHash)== 0){
			done = true;
			/**************************************************************************************/
			/**ISSUES HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
		}else{
			/**add the chunk if correct*/
			boolhaveChunks[currBlock]= true; //update our information and update the tracker
			this.chunksHashes[currBlock] = ByteBuffer.wrap(chunkHash);
			this.chunks.add(chunk); /**add to array*/
			ConnectToTracker.updateAmounts(chunk.length);
			ConnectToTracker.sendMessageToTracker(null, null);
			have = new Message(5,(byte)4); //send have message to all peers.
			have.setPayload(index, currBegin, currBlock, null);
			writeHavetoAllPeers(have);
		}
	}
	/**
	 * make sure the SHa-1 hashes match and that is it not already in the array before. 
	 * */
	private int verify(int block, byte[] chunk, byte[] chunkHash) {
		int verify = 1; 
		byte[] trackerHash = torrentI.piece_hashes[block].array();
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (Exception e) {
			System.out.println("Error: Could not SHA-1");
			closeConnection();
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
				PeerConnectionsInfo.subsetDPeers.get(i).os.write(have.message); //the current peers we are downloading from and have threads wil receive have messages
				PeerConnectionsInfo.subsetDPeers.get(i).os.flush();
			} catch (IOException e) {
				System.out.println("Error: Cannot send have message to peer");
			}
		}
	}

	/**
	 * Makes sure the chunk given is not already present in the Hashes array 
	 * This helps avoid duplicate chunks.
	 * @param chunkHash chunks
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
	 * @param array chunks we have
	 * @param chunkHash chunks we temporary have
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
	 * Handshake and make sure correct responses. then send the bitfield we have.
	 * */
	public boolean openConnection() throws IOException{

		boolean peerInfoGood = true;
		/**Construct message to send to peer*/
		byte[] message = new byte[68];
		message[0] = (byte)19;
		System.arraycopy(BitProtocol, 0,message,1,19);
		System.arraycopy(eightZeros, 0, message, 20, 8);
		System.arraycopy(torrentI.info_hash.array(),0, message, 28, 20);
		System.arraycopy(ourID, 0, message, 48, 20);

		try {
			System.out.println("Connecting to Peer.");

			/**Open connection by using a socket*/
			socket = new Socket(ip,port);
			System.out.println("Connected to Peer: . "+ ip);
			/**Create input and output stream*/
			os = new DataOutputStream(socket.getOutputStream());
			is = new DataInputStream(socket.getInputStream());
		} catch (Exception e) {
			System.out.println("Error: Could not Open Socket to Peer.");
		} 

		if (socket==null){ /**bad host name given.*/
			System.out.println("Error: Peer Socket was unable to be created due to bad hostname/IP address or bad port number given. Please try again.");
			closeConnection();
			return false;
		}
		System.out.println("Starting handshake.");
		try {
			/**Initiate handshake by sending message to peer*/
			os.write(message); 
			os.flush(); 

			/**Get reply from peer*/
			byte[] peerAns = new byte[68];
			is.readFully(peerAns);

			byte[] peerInfoHash = Arrays.copyOfRange(peerAns, 28, 48); 

			/**checks if peer's info hash returned is same as info has we have from tracker*/
			for (int i = 0; i<20;i++){
				if (peerInfoHash[i]!=torrentI.info_hash.array()[i]){
					System.out.println("Error: Peer's info hash returned from handshake is not same.");
					closeConnection();
					peerInfoGood = false;
					break;
				}		
			}
			/**If peer info given does not match as tracker's given, exit (already closed connections before)*/
			if (peerInfoGood==false){
				return false;
			}
			/**Check if peer id is same as the tracker given peer id*/
			byte[] peerIDCheck = Arrays.copyOfRange(peerAns, 48, 68);
			for (int n = 0; n <20;n++){
				if (id[n]!=peerIDCheck[n]){
					System.out.println("Error: Peer id is not same as given from tracker.");
					closeConnection();
					peerInfoGood = false;
					break;
				}
			}
			/**If peer info given does not match as tracker's given, exit (already closed connections before)*/
			if (peerInfoGood==false){
				return false;
			}
		} catch (Exception e) {
			System.out.println("Error: Could not handshake with peer.");
			closeConnection();
			return false;
		}
		/** Send our bitfield as message to this peer. */
		Message bitfieldMsg = new Message(1, (byte) 5);
		byte[] temp = new byte[FileChunks.booleanToByteBitField(FileChunks.ourBitField).length];
		for (int as = 0 ; as< temp.length; as++){
			System.out.println(temp[as]);
		}
		bitfieldMsg.setPayload(-1, -1, -1, FileChunks.booleanToByteBitField(FileChunks.ourBitField));
		System.out.println("Sending bitfield message to peer.");
		os.write(bitfieldMsg.message);
		os.flush();/**push message to stream*/

		System.out.println("finished handshake with Peer: ." + ip);
		return true;
	}

	/** Get bitfield from peer. 
	 *	If wrong bit field size make bitfield null
	 *	@param read message ID 
	 * 	@throws IOException 
	 **/
	public void getBitField(byte read) throws IOException {
		System.out.println("Reading message from peer.");
		System.out.println("Finished reading message from peer.");
		bitField = new byte[is.available()];
		if ((read==5)||(read==4)){ /**have or bitfield message being sent. */
			if (read==5){
				is.readFully(bitField); /**get rid of bit field*/
				System.out.println("bitfield length: "+bitField.length);
				byte[] ba = new byte[(ConnectToTracker.torrentI.piece_hashes.length + 7) / 8];
				/**the number of chunks = the length of the bitField!!*/
				if(ba.length != bitField.length){
					System.out.println("The peer is not sending us the correct length bitfield");
					this.boolBitField=null;
					return;
				}
			}else{ 
				System.out.println("Oh no peer sent have message after handshake instead of bitfield.");
				this.boolBitField=null;
				return;
			}
		}
		/** copy the array into the intial stuff we have for calculating the peer bitfields and ours. */
		boolean[] tempBool = toBooleanArray(bitField);
		this.boolBitField = new boolean[tempBool.length];
		for (int i = 0; i<tempBool.length; i++){
			this.boolBitField[i]=tempBool[i];
		}
		this.peerChunks = new boolean[boolBitField.length];
		for(int z = 0; z<boolBitField.length; z++){
			this.peerChunks[z]=boolBitField[z];
		}
	}


	/**
	 * Read in message from peer which are formatted based on message type.
	 * We need id of the message to identify what type of message was sent. 
	 * Handles messages by calling methods based on message 
	 * @return message byte as int is returned 
	 * @throws IOException
	 */
	public byte readMsg() throws IOException {

		int length, begin, index;
		byte [] b = new byte[4];
		is.readFully(b);
		/**Read in message*/
		int msgLength = ByteBuffer.wrap(b).getInt();
		/**Read in id*/
		byte id = is.readByte();

		/**keep-alive*/
		if(msgLength == 0){ 
			return -1;
		}
		switch(id){
		case 0:		/** Choked */
			System.out.println("Choked on ip: "+ip+" on port: "+port);
			gotChoked(); 
			if (isChoked==true){
				return -2;
			}
		case 1:		/** Unchoked */
			isChoked=false;
			return id;
		case 2: 	/** Interested */
			Determinechoke();
			return id;
		case 3: 	/** Not Interested */
			stopThread = true;
			closeConnection();
			return id;
		case 4: 	/** Have */
			return id;
		case 5:		/** Bit Field */
			getBitField(id);
			return id;
		case 6: 	/** Request */
			index = is.readInt();
			begin = is.readInt();
			length = is.readInt();
			upload(length, index, begin);
			return id;
		case 7:  	/** Piece */
			downloadPiece();
			return id;
		default: return id;
		}	
	}


	/**
	 * Deals with choking if peer chokes us. It waits till we get unchoked or will terminate.
	 * @throws SocketException
	 */
	public void gotChoked() throws SocketException{
		isChoked = true;
		socket.setSoTimeout(5000000); /**time out for 1 minute to get unchoked else destroy connection*/
		do{
			try {
				if (readMsg()==1){
					System.out.println("Got unchoked before time interval ended.");
					isChoked=false;
					return;
				}
			} catch (IOException e) {
				System.out.println("Timed out waiting to be unchoked! Finishing Connection.");
				return;
			}
		}while (isChoked==true);
	}

	/** 
	 * Converts bitfield byte array to boolean array 
	 * @param B bitfield array to convert to boolean array
	 * @return boolean array
	 **/
	public static boolean[] toBooleanArray(byte[] B) {
		boolean[] bool = new boolean[B.length*8];

		for(int i = 0; i<B.length*8; i++){ 
			if ((B[i/8] & (1<<(7-(i%8)))) > 0){
				bool[i] = true; /**if the shifting of the bit creates a whole multiple of 2 => true*/
			}
		}
		return bool;
	}

	/**
	 * Determine which if peer is to be choked or unchoked.
	 * @return returns true if peer is unchoked, or returns true if peer is choked
	 * @throws IOException
	 */
	public boolean Determinechoke() throws IOException{

		System.out.println("Peer is interested.");
		/** If we have less than 3 downloading peers, let this peer connect. */		
		if(PeerConnectionsInfo.unchokedPeers.size() < 3){
			PeerConnectionsInfo.unchokedPeers.add(this);
			System.out.println("Peer is unchoked.");
			return true;
			/** If we are already connected to six people who are uploading from us, keep peer choked. */
		} else if (PeerConnectionsInfo.uploadConnections > 6){
			PeerConnectionsInfo.chokedPeers.add(this);
			System.out.println("Peer is choked.");
			/** Control.randomUnchoke(); */
			return false;
			/** Read message to see if they are have messages. */
		} else {
			/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
			/** Control.randomUnchoke(); */
			int msgfrPeer2 = readMsg();  		
			if(msgfrPeer2 == Message.MSG_HAVE || msgfrPeer2 == Message.MSG_BITFIELD){
				PeerConnectionsInfo.unchokedPeers.add(this);
				System.out.println("Peer has something to share! Peer is unchoked.");	
				return true;
			}else {
				PeerConnectionsInfo.chokedPeers.add(this);
				System.out.println("Peer has nothing to share. Peer is choked");
				return false;
			}
		}
	}
	
	/**
	 * closes the socket and streams.
	 **/
	public void closeConnection() {

		System.out.println("Closing socket and data streams.");
		try {
			/**Close socket and streams*/
			socket.close();
			is.close();
			os.close();
		} catch (Exception e) {
			System.out.println("Error: Could not close data streams!");
			return;
		}	
	}


	/**
	 * thread run to do everything.
	 * */
	public void run() {

		while(!stopThread) {
			try {
				if((!done) && (!isChoked)) {
					requestPiece();
					if(readMsg() != 7) {
						Thread.sleep(30000);
					}
				}
				else {
					readMsg();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}