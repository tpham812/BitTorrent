package BitTorrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * This class handles peers we are uploading to => peers who want to download from us
 * we have to receive handshakes, interested and unchoke messages
 * then run method will upload what we have and close connection
 * See method called upload()
 * */

//Jewel's Notes: How do we randomly select who is optimistically unchoked?


public class UPeer implements Runnable{
	
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
	/**Protocol name to write in the handshake message*/
	private final static byte[] BitProtocol = new byte[]{'B','i','t','T','o','r','r','e','n','t',' ','P','r','o','t','o','c','o','l'};
	/**Eight zeroes field to write in the handshake message*/
	private final static  byte[] eightZeros = new byte[]{'0','0','0','0','0','0','0','0'};
	
	
	public UPeer(String ip, byte[] id, int port)
			throws IOException, InterruptedException {
		
		this.IP = ip;
		this.ID = id;
		this.port = port;
		//this.torrentInfo = ConnectToTracker.torrentI;
		this.infoHash = torrentInfo.info_hash.array();
		this.ourID = ConnectToTracker.toSendToPeerID;
		
	}
	
	
	public void receiveHandshake() throws IOException{
		//send handshake back
		byte[] message = new byte[68];
		message[0] = (byte)19;
		System.arraycopy(BitProtocol, 0,message,1,19);
		System.arraycopy(eightZeros, 0, message, 20, 8);
		System.arraycopy(infoHash,0, message, 28, 20);
		System.arraycopy(ourID, 0, message, 48, 20);
		
		//send bitfield message
		Message bitfieldMsg = new Message(1, (byte) 5);
		System.out.println("Sending bitfield message to peer.");
		os.write(bitfieldMsg.message);
		os.flush();/**push message to stream*/
		System.out.println("Finished writing message to peer.");

		upload();
		
	}
	
	
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
	
	//if user has bitfield or have messages and is interested, unchoke.
	//This does not account for the optimistically unchoked. 
  	public boolean unchoke() throws IOException{
  		
		int msgIDfrPeer = readMessage();
  		if (msgIDfrPeer == 2){
  			System.out.println("Peer is interested.");	
  			int msgIDfrPeer2 = readMessage();  			
  			//byte[] bitFieldOrHaveMsg = new byte[in.available()];
  			//in.readFully(bitFieldOrHaveMsg); /**get rid of bit field*/  			
  			if(msgIDfrPeer2 == 4 || msgIDfrPeer2 == 5){
  				System.out.println("Peer has something to share!");
  				return true;	
  			}
  			return false;
  		}
  		return false;
  	}
	
	/**
	 * reads in request messages and sends the piece
	 * reads in have messages and does nothing
	 * @throws IOException 
	 * */
	public void upload() throws IOException{
		
		boolean isUnchoked = unchoke();
		
		if(isUnchoked){
			Message unchokeMsg = new Message(1,(byte)1); /**create unchoke message*/
			System.out.println("Writing unchoke message to peer.");
			os.write(unchokeMsg.message);
			os.flush();/**push message to stream*/
			System.out.println("Finished unchoke writing message to peer.");
			
			//upload
			/*When we, the downloading peer, have the complete file,
			 * we let the tracker know and automatically upload the file to be shared.
			 * we become an upload peer. How do implement this? */			
			
			
			
		}else{
			//choke msg (and close connection?)
			Message chokeMsg = new Message(1,(byte)0); /**create choke message*/
			System.out.println("Writing choke message to peer.");
			os.write(chokeMsg.message);
			os.flush();/**push message to stream*/
			System.out.println("Finished writing message to peer.");
			//finishConnection();
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
	
	// will upload what we have and close the connection 
	public void run() {
		
		
		
	}

}
