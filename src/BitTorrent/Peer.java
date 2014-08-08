package BitTorrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Peer { 
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//put all of the important fields betweeen upeer and dpeer here
	//shorten the giant constructors in both of them!!???
	//this should be able to close streams and initalize the streams only? 
	//what else can it do?
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
	boolean isChoked;
	public int port;
	public byte[] id;
	public String ip;
	public static DataOutputStream os;
	public static DataInputStream is;
	public static byte[] ourID;
	public byte[] bitField;
	public boolean[] boolBitField;
	/**Socket connection to the peer*/
	public Socket socket;
	protected final static byte[] BitProtocol = new byte[]{'B','i','t','T','o','r','r','e','n','t',' ','P','r','o','t','o','c','o','l'};
	/**Eight zeroes field to write in the handshake message*/
	protected final static  byte[] eightZeros = new byte[]{'0','0','0','0','0','0','0','0'};


	public Peer(String ip, byte[] id, int port) {
		this.port = port;
		this.ip = ip;
	}
	
	/** Handshake to peer */
	public void openConnection(Socket socket) throws Exception {
		//open connection to peers
	}
	
	/** Get bitfield from peer */
	/** If wrong bit field size make bitfield null */
	public void getBitField() {
		
	}
	
	/** Converts bitfield byte array to boolean array */
	public void toBooleanArray() {
		
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