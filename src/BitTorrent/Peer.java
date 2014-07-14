package BitTorrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Peer {

	private Socket socket;
	private DataOutputStream os;
	private DataInputStream in;
	private OutputStream output;
	TorrentInfo torrentInfo;
	private InputStream input;
	byte[] infoHash;
	String IP;
	int port;
	String fileOutArg;
	byte[] peerID;
	byte[] ourID;
	FileOutputStream fileoutput;
	ArrayList<byte[]> chunks = new ArrayList<byte[]>();
	final byte[] BitProtocol = new byte[]{'B','i','t','T','o','r','r','e','n','t',' ','P','r','o','t','o','c','o','l'};
	final byte[] eightZeros = new byte[]{'0','0','0','0','0','0','0','0'};

	public Peer(String ip, int port, byte[] id, String fileOutArg) throws IOException, InterruptedException{
		this.IP = ip;
		this.port = port;
		this.torrentInfo = ConnectToTracker.torrentI;
		this.peerID = id;
		this.infoHash = torrentInfo.info_hash.array();
		this.fileOutArg = fileOutArg;
		this.ourID=ConnectToTracker.toSendToPeerID;
		handShake();
		downloadFileFromPeer();
		finishConnection();
	}

	private void downloadFileFromPeer() throws IOException, InterruptedException {
		
		Message interested = new Message(1,(byte)2); //create message that you are interested 
		byte[] chunk;
		byte[] tempBuff;
		int block = 0;
		Message askForPieces; //ask for more chunks
		int index = torrentInfo.piece_length; //typical length of piece
		int left;
		int lastSize;
		int begin = 0;
		
		int read = readMessage();
		byte[] bitField = new byte[in.available()];
		if ((read==5)||(read==4)){ //have or bitfield message being sent. 
			//part 1 = only have bit field message due to one peer with everything only. 
			in.readFully(bitField); //get rid of bit field
		}

		os.write(interested.message);
		os.flush();//push message to stream

		read = readMessage();
		if (read == 1){
			in.readByte();	//unchoked so proceed
			//System.out.println("not chocked: "+read);
		}
		//System.out.println("Read " +read);

		left = torrentInfo.piece_hashes.length-1;
		lastSize = torrentInfo.file_length - (left*torrentInfo.piece_length);//cuz last pieces might be irregurarly sized
		fileoutput = new FileOutputStream(new File(this.fileOutArg));

		//System.out.println("Lenght: "+torrentInfo.piece_hashes.length);
		while (block!=torrentInfo.piece_hashes.length){
			System.out.println("index, begin, block: "+index+","+begin+","+block);
			if (block==torrentInfo.piece_hashes.length-1){ //LAST PIECES
				System.out.println("GETTING TO LAST BLOCK!!!");
				askForPieces = new Message(13,(byte)6); 

				if (lastSize<index){
					index = lastSize;
				}else{
					index = torrentInfo.piece_length;
				}
				lastSize = lastSize-index; 
				askForPieces.setPayload(index,begin,block);
				os.write(askForPieces.message);
				os.flush();
				tempBuff = new byte[4];
				for (int k = 0; k<4;k++){
					tempBuff[k]=in.readByte();
				}
				chunk = new byte[index];
				for (int l = 0; l<9;l++){
					in.readByte();
				}

				for (int m = 0 ; m<index;m++){
					chunk[m]=in.readByte(); 
				}//read in chunk

				this.chunks.add(chunk); //add to array
				fileoutput.write(chunk); //write to file
				block++;

			}else{ //still have more pieces left!
				askForPieces = new Message(13,(byte)6); 
				askForPieces.setPayload(index,begin,block);

				os.write(askForPieces.message);
				os.flush(); //push to output stream.

				//	System.out.println("Avail in ln159: "+in.available()); //number of bytes available to download
				tempBuff = new byte[4];  //read in first four bytes which we don't use
				for (int k = 0; k<4;k++){
					tempBuff[k]=in.readByte();
				}

			//	System.out.println("Get here in line 165.");
				chunk = new byte[index]; //create piece length size chunk
				for (int l = 0; l<9;l++){
					in.readByte();
				}
				for (int m = 0 ; m<index;m++){
					chunk[m]=in.readByte(); 
				}//read in chunk
				this.chunks.add(chunk); //add to array
				fileoutput.write(chunk); //write to file
				if (begin+index==torrentInfo.piece_length){
					block++;
					begin = 0;
					//break;
				}else{
					begin +=index;
				}	
			}			
		}
	}

	private void handShake(){
		
		//construct message to send to peer.
		byte[] message = new byte[68];
		message[0] = (byte)19;
		System.arraycopy(BitProtocol, 0,message,1,19);
		System.arraycopy(eightZeros, 0, message, 20, 8);
		System.arraycopy(infoHash,0, message, 28, 20);
		System.arraycopy(ourID, 0, message, 48, 20);

		try {//open connections
			socket = new Socket(IP,port);
			os = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("Error: Could not Open Socket to Peer.");
		} //tries to create socket

		if (socket==null){ //bad host name given.
			System.out.println("Error: Peer Socket was unable to be created due to bad hostname/IP address or bad port number given. Please try again.");
			finishConnection();
		}

		try { //initiate handshake and get reply
			os.write(message); 
			os.flush(); //writes message out to stream 

			byte[] peerAns = new byte[68];   //get peer reply
			in.readFully(peerAns); //has to read in 68 bytes else error thrown + stores into peerAns

			byte[] peerInfoHash = Arrays.copyOfRange(peerAns, 28, 48); 
			//checks if peer's info hash returned is same as info has we have from tracker
			boolean peerInfoGood = false;
			for (int i = 0; i<20;i++){
				if (peerInfoHash[i]!=infoHash[i]){
					System.out.println("Error: Peer's info hash returned from handshake is not same.");
					finishConnection();
					peerInfoGood = false;
					break;
				}		
			}
			//check if peer id is same as the tracker given peer id!!!
			byte[] peerIDCheck = Arrays.copyOfRange(peerAns, 48, 68);
			for (int n = 0; n <20;n++){
				if (peerID[n]!=peerIDCheck[n]){
					System.out.println("Error: Peer id is not same as given from tracker.");
					finishConnection();
					peerInfoGood = false;
					break;
				}
			}
			//if peer info given does not match as tracker's given, exit (already closed connections before).
			if (peerInfoGood==false){
				return;
			}

		} catch (IOException e) {
			System.out.println("Error: Could not handshake with peer.");
		}
	}

	private byte readMessage() throws IOException {
		
		int msgLength = in.readInt();
		byte id = in.readByte();

		//keep-alive
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
	
	private void finishConnection() {
		
		try {
			socket.close();
			in.close();
			os.close();
			fileoutput.close();
		} catch (Exception e) {
			System.out.println("Error: Could not close data streams!");
			return;
		}
	}
}