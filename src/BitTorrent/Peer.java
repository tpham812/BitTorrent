package BitTorrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class Peer implements Runnable{ 
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//put all of the important fields betweeen upeer and dpeer here
	//shorten the giant constructors in both of them!!???
	//this should be able to close streams and initalize the streams only? 
	//what else can it do?
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
	public int port;
	public String ip;
	public DataOutputStream os = null;
	public DataInputStream in = null;
//	public static TorrentInfo torrentInfo = null;
//	public OutputStream os = null;
//	public InputStream in = null;
	/**Socket connection to the peer*/
	private Socket socket;
	protected final static byte[] BitProtocol = new byte[]{'B','i','t','T','o','r','r','e','n','t',' ','P','r','o','t','o','c','o','l'};
	/**Eight zeroes field to write in the handshake message*/
	protected final static  byte[] eightZeros = new byte[]{'0','0','0','0','0','0','0','0'};


	public Peer(String ip, int port) {
		this.port = port;
		this.ip = ip;
	}
	
	public void openConnection(Socket socket) throws Exception {
		//open connection to peers
	}
	
	
	
	
	
	/** 
	 * Close connection and streams and output file.
	 */
	protected void finishConnection() {

		System.out.println("Closing socket and data streams.");
		try {
			/**Close socket and streams*/
			socket.close();
			in.close();
			os.close();
		} catch (Exception e) {
			System.out.println("Error: Could not close data streams!");
			return;
		}
	}
	
	
	
	

}
