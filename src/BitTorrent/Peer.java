package BitTorrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class Peer implements Runnable{ 

	private boolean stopThread;
	boolean isChoked;
	public double throughput;
	public int port;
	public byte[] id;
	public String ip;
	public DataOutputStream os;
	public DataInputStream is;
	public byte[] ourID;
	public byte[] bitField;
	public boolean[] boolBitField;
	public TorrentInfo torrentI;
	/**Socket connection to the peer*/
	public Socket socket;
	protected final byte[] BitProtocol = new byte[]{'B','i','t','T','o','r','r','e','n','t',' ','P','r','o','t','o','c','o','l'};
	/**Eight zeroes field to write in the handshake message*/
	protected final byte[] eightZeros = new byte[]{'0','0','0','0','0','0','0','0'};


	public Peer(String ip, byte[] id, int port) throws IOException {
		this.port = port;
		this.ip = ip;
		this.id = id;
		this.torrentI = ConnectToTracker.torrentI;
		this.stopThread = false;
	}

	/**
	 * Handshake with peer by sending hand shake message and receiving handshake message back from peer. 
	 * Verify if the info hash peer sends back is the same and if the their id is the same as tracker given id
	 */
	public boolean openConnection(){

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
			System.out.println("Connected.");
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
		System.out.println("finished handshake.");
		return true;
	}

	/** Get bitfield from peer */
	/** If wrong bit field size make bitfield null 
	 * @throws IOException */
	public void getBitField() throws IOException {
		System.out.println("Reading message from peer.");
		int read = readMessage();
		System.out.println("Finished reading message from peer.");
		bitField = new byte[is.available()];
		if ((read==5)||(read==4)){ /**have or bitfield message being sent. */
			if (read==5){
				is.readFully(bitField); /**get rid of bit field*/
				if(ConnectToTracker.torrentI.piece_hashes.length != bitField.length){
					/**the number of chunks = the length of the bitField!!*/
					System.out.println("The peer is not sending us the correct length bitfield");
					this.boolBitField=null;
				}
			}else{
				System.out.println("Oh no peer sent have message after handshake instead of bitfield.");
				this.boolBitField=null;
			}
		}

		this.boolBitField = toBooleanArray(bitField);

	}


	/**
	 * Read in message from peer which are formatted based on message type.
	 * We need id of the message to identify what type of message was sent. 
	 * @return message byte as int is returned 
	 * @throws IOException
	 */
	public byte readMsg() throws IOException {

		/**Read in message*/
		int msgLength = is.readInt();
		/**Read in id*/
		byte id = is.readByte();

		/**keep-alive*/
		if(msgLength == 0){ //do nothing!!!!  but keep this here!
			return -1;
		}
		switch(id){
		case 0://choked = download peer handles gotChocked
			System.out.println("Choked on ip: "+ip+" on port: "+port);
			gotChoked(); 
			if (isChoked==true){
				return -2;
			}
		case 1://unchoked  = then we can request a piece if we unchoke
			isChoked=false;
			return id;
		case 2: //intereseted / go to upload peer for choke or unchoke 

			return id;

		case 3: //not interested = destroy connection via upload peer. 
			stopThread = true;
			closeConnection();
			return id;

		case 4: //have = do nothing
			return id;
		case 5: //bitfield = do nothing
			//upload peer desides to choke or unchoke
			return id;
		case 6: //request = //go to upload peer's upload piece
			
			return id;
		case 7: //piece // go to download peer's download piece
			int index = is.readInt();
			int begin = is.readInt();
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
		socket.setSoTimeout(120000); /**time out for 1 minute to get unchoked else destroy connection*/
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
	 * */
	public static boolean[] toBooleanArray(byte[] B) {
		boolean[] bool = new boolean[B.length*8];

		for(int i = 0; i<B.length*8; i++){
			if ((B[i/8] & (1<<(7-(i%8)))) > 0){
				bool[i] = true; /**if the shifting of the bit creates a whole multiple of 2 => true*/
			}
		}
		return bool;
	}

	public boolean Determinechoke() throws IOException{

		//read message from peer to see if they are interested.

		System.out.println("Peer is interested.");
		//If we have less than 3 downloading peers, let this peer connect.			
		if(PeerConnectionsInfo.unchokedPeers.size() < 3){
			PeerConnectionsInfo.unchokedPeers.add(this);
			System.out.println("Peer is unchoked.");
			return true;
			//If we are already connected to six people who are uploading from us, keep peer choked.
		} else if (PeerConnectionsInfo.uploadConnections > 6){
			PeerConnectionsInfo.chokedPeers.add(this);
			System.out.println("Peer is choked.");
			Control.randomUnchoke();
			return false;
			//read message to see if they are have messages.
		} else {
			/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
			Control.randomUnchoke();
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


		@Override
		public void run() {

			while(!stopThread) {
				try {
					readMsg();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}