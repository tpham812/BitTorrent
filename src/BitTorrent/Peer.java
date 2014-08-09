package BitTorrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Peer { 
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//put all of the important fields betweeen upeer and dpeer here
	//shorten the giant constructors in both of them!!???
	//this should be able to close streams and initalize the streams only? 
	//what else can it do?
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
	static boolean isChoked;
	public static int port;
	public byte[] id;
	public static String ip;
	public static DataOutputStream os;
	public static DataInputStream is;
	public static byte[] ourID;
	public byte[] bitField;
	public boolean[] boolBitField;
	public TorrentInfo torrentI;
	/**Socket connection to the peer*/
	public Socket socket;
	protected final static byte[] BitProtocol = new byte[]{'B','i','t','T','o','r','r','e','n','t',' ','P','r','o','t','o','c','o','l'};
	/**Eight zeroes field to write in the handshake message*/
	protected final static  byte[] eightZeros = new byte[]{'0','0','0','0','0','0','0','0'};


	public Peer(String ip, byte[] id, int port) throws IOException {
		this.port = port;
		this.ip = ip;
		this.id = id;
		this.torrentI = ConnectToTracker.torrentI;
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
		byte[] bitField = new byte[is.available()];
		if ((read==5)||(read==4)){ /**have or bitfield message being sent. */
			is.readFully(bitField); /**get rid of bit field*/
			
			//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			//implement rare piece stuff!!!
			//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		}

	}
	/**
	 * Read in message from peer which are formatted based on message type.
	 * We need id of the message to identify what type of message was sent. 
	 * @return message byte as int is returned 
	 * @throws IOException
	 */
	static byte readMessage() throws IOException {

		/**Read in message*/
		int msgLength = is.readInt();
		/**Read in id*/
		byte id = is.readByte();

		/**keep-alive*/
		if(msgLength == 0){
			return -1;
		}
		switch(id){
		case 0://choked
			System.out.println("Choked on ip: "+ip+" on port: "+port);
			Download.gotChoked(); 
			if (isChoked==true){
				return -2;
			}
		case 1://unchoked
			isChoked=false;
			return id;
		case 7: //piece
			int index = is.readInt();
			int begin = is.readInt();	
		default: return id;
		}
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
}