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


public class UPeer extends Peer implements Runnable{
	
	/**Socket connection to the peer*/
	private Socket socket;
	/**Output stream to the peer*/
	private DataOutputStream os;
	/**Input stream from the peer*/
	private DataInputStream in;
	/**IP address of peer*/
	private String IP;
	/**Port number of peer*/
	private int port;
	/**ID of the peer*/
	private byte[] ID;
	/**Our ID sent to tracker when we were a download peer*/
	private byte[] ourID;
	/**Torrent Info file that comes from the .torrent file*/
	private TorrentInfo torrentInfo;
	
	
	public UPeer(String ip, byte[] id, int port)
			throws IOException, InterruptedException {
		
		super(ip, port);
		this.ID = id;
		
		//initiate socket connection somewhere here
		
	}
	
	
	public void receiveHandshake(byte[] info_hash) throws IOException{
		//read handshake from download peer
		byte[] receiveHandshake =  new byte[68];
		in.readFully(receiveHandshake);
			
		//send handshake back
		byte[] returnShake = new byte[68];
		returnShake[0] = (byte)19;
		System.arraycopy(BitProtocol, 0,returnShake,1,19);
		System.arraycopy(eightZeros, 0, returnShake, 20, 8);
		System.arraycopy(info_hash,0, returnShake, 28, 20);
		System.arraycopy(ourID, 0, returnShake, 48, 20);
		
		if(receiveHandshake[0] != (byte) 19){
			System.out.println("Not a Bit Torrent Protocol.");
		} else{
			os.write(returnShake);
			os.flush();
		}
		
		//send bitfield message before unchoke
		Message bitfieldMsg = new Message(1, (byte) 5);
		System.out.println("Sending bitfield message to peer.");
		os.write(bitfieldMsg.message);
		os.flush();/**push message to stream*/
		System.out.println("Finished writing message to peer.");

		
		//does not upload if not unchoked
		boolean isUnchoked = unchoke();
		if(isUnchoked){
			Message unchokeMsg = new Message(1,(byte)1); /**create unchoke message*/
			System.out.println("Writing unchoke message to peer.");
			os.write(unchokeMsg.message);
			os.flush();/**push message to stream*/
			System.out.println("Finished unchoke writing message to peer.");
			upload();
			
		}else{
			//choke msg 
			Message chokeMsg = new Message(1,(byte)0); /**create choke message*/
			System.out.println("Writing choke message to peer.");
			os.write(chokeMsg.message);
			os.flush();/**push message to stream*/
			System.out.println("Finished writing message to peer.");
 		}
		
		
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
	//This does not account for the optimistically unchoked. Implement 30sec thread!
  	public boolean unchoke() throws IOException{
  		
		int msgIDfrPeer = readMessage();
  		if (msgIDfrPeer == Message.MSG_INTERESTED){
  			System.out.println("Peer is interested.");	
  			int msgIDfrPeer2 = readMessage();  			
  			//byte[] bitFieldOrHaveMsg = new byte[in.available()];
  			//in.readFully(bitFieldOrHaveMsg); /**get rid of bit field*/  			
  			if(msgIDfrPeer2 == Message.MSG_HAVE || msgIDfrPeer2 == Message.MSG_BITFIELD){
  				System.out.println("Peer has something to share! Do not choke.");
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
		Message pieceMsg;
		byte[] block;
		int index, begin, length;
		
		int msg = readMessage();
		if(msg == Message.MSG_HAVE || msg == Message.MSG_REQUEST){
			
			if(msg == Message.MSG_HAVE){
			try {
				// read the next request
				msg = readMessage(); 
			} catch (Exception e) {
				// no following messages
				return;
				}
			}
		} else {
			// other messages received
		}
		
		index = in.readInt();
		begin = in.readInt();
		length = in.readInt();
		
		
	
	}
	
	
	public void isStopped() throws IOException{
		//threads? msg from tracker?
		//put condition here
		finishConnection();
	}
	

	public void run() {
		
		System.out.println("Uploading to some peer on new thread.");
		try {
			upload();
			finishConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

}
