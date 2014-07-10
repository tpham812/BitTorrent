package BitTorrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Peer {

	public Socket connectionSocket;
	public DataOutputStream os;
	public DataInputStream in;
	public OutputStream output;
	public InputStream input;
	
	String IP;
	int port;
	String peerID;

	
	//constructor 
	public Peer(int port, String IP){
		this.port = port;
		this.IP = IP;
		
		
	} 
	//sets Peer ID to to a random string
	public String setPeerID(){
		peerID = Helper.generateRandomPeerID();
		return peerID;
	}
	
	
	//prints out peer information
	public String toString(){
		String peerInfo = "Peer Information:" + peerID +  ", " + IP + ", " + port; 
		return peerInfo;
	}
	
	
	public byte readMessage() throws IOException{
		byte id = in.readByte();
		int msgLength = in.readInt();
		
		//keep-alive
		if(msgLength == 0){
			return -1;
		}
		
		switch(id){
			//0: choke
			//1: unchoke
			//2: interested
			//3: not interested
			//4: have 
			//5: bitfield
			//6: request
			case 0-6: return id;
			//7: piece
			case 7: 
				int index = in.readInt();
				int begin = in.readInt();
			//8: cancel	
			case 8: return id;	
			default: break;
		}
		return 0;
		
	}
	
	
	
}
