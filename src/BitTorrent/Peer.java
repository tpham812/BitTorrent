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

	public Socket socket;
	public DataOutputStream os;
	public DataInputStream in;
	public OutputStream output;
	TorrentInfo torrentInfo;
	public InputStream input;
	byte[] infoHash;
	String IP;
	int port;
	String fileOutArg;
	byte[] peerID;
	byte[] ourID;
	FileOutputStream fileoutput;
	ArrayList<byte[]> chunks = new ArrayList<byte[]>();
	static byte[] BitProtocol = new byte[]{'B','i','t','T','o','r','r','e','n','t',' ','P','r','o','t','o','c','o','l'};
	static byte[] eightZeros = new byte[]{'0','0','0','0','0','0','0','0'};

	//constructor 
	public Peer(int port, String IP){ 
		this.port = port;
		this.IP = IP;


	} 
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

	private void downloadFileFromPeer() throws IOException, InterruptedException {
		Message interested = new Message(1,(byte)2); //create message that you are interested 
		byte[] chunk;
		byte[] tempBuff;
		int numChunks = 0;
		Message askForPieces; //ask for more chunks
		int requestIndex = torrentInfo.piece_length; //typical length of piece
		int left;
		int lastSize;
		int curr = 0;

		int read = readMessage();
		if ((read==5)||(read==4)){
			System.out.println("YAY = get bitfield message! line: 95 in peer.java");
		}
		System.out.println("readMessage: "+read);

		os.write(interested.message);
		os.flush();//push message to stream
		
		read = readMessage();
		if (read == 1){
			in.readByte();	//unchoked so proceed
			System.out.println("choking or not: "+read);
		}

		left = torrentInfo.piece_hashes.length-1;
		lastSize = torrentInfo.file_length - (left*torrentInfo.piece_length);//cuz last pieces might be irregurarly sized
		fileoutput = new FileOutputStream(new File(this.fileOutArg));


		while (numChunks!=torrentInfo.piece_hashes.length){//loop till we have all pieces
			//askForPieces = new Message(13,(byte)6); //13 is for requesting for pieces = First thing =length
			//id = 6 and then we need index, begin and length
			//message = new byte[13+4] for other 4 things
			//System.arrayCopy(intToByteArray(13),0,message,0,4);
			//this method takes value and shifts >> 24, 16, and 8 for each byte. and returns byte[4]
			//used for big endian hex value.
			//message[4] = (byte)6;

			if (numChunks+1 ==torrentInfo.piece_hashes.length){ //LAST PIECES
				askForPieces = new Message(13,(byte)6); 

				if (lastSize<requestIndex){
					requestIndex = lastSize;
				}else{
					requestIndex = torrentInfo.piece_length;
				}
				lastSize = lastSize-requestIndex; 
				askForPieces.setPayload(requestIndex,curr,numChunks);
				os.write(askForPieces.message);
				os.flush();
				tempBuff = new byte[4];
				for (int k = 0; k<4;k++){
					tempBuff[k]=in.readByte();
				}
				chunk = new byte[requestIndex];
				for (int l = 0; l<9;l++){
					in.readByte();
				}

				for (int m = 0 ; m<requestIndex;m++){
					chunk[m]=in.readByte(); 
				}//read in chunk

				this.chunks.add(chunk); //add to array
				fileoutput.write(chunk); //write to file

			}else{ //still have more pieces left!
				askForPieces = new Message(13,(byte)6); 
				askForPieces.setPayload(requestIndex,curr,numChunks);

				//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!WHAT IS MESSAGE RETURNING?@@@
				os.write(askForPieces.message);
				os.flush(); //push to output stream.


				System.out.println("Avail in ln159: "+in.available()); //number of bytes available to download
				tempBuff = new byte[4];  //read in first four bytes which we don't use
				for (int k = 0; k<4;k++){
					tempBuff[k]=in.readByte();
				}

				System.out.println("Get here in line 165.");

				chunk = new byte[requestIndex]; //create piece length size chunk
				for (int l = 0; l<9;l++){
					System.out.println(in.readByte());
				}
				for (int m = 0 ; m<requestIndex;m++){
					chunk[m]=in.readByte(); 
				}//read in chunk
				this.chunks.add(chunk); //add to array
				fileoutput.write(chunk); //write to file
				if (curr+requestIndex==torrentInfo.piece_length){
					numChunks++;
					curr = 0;
					break;
				}else{
					curr +=requestIndex;
				}	
			}			
		}
	}

	public void handShake(){
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

	public byte readMessage() throws IOException{
		int msgLength = in.readInt();
		byte id = in.readByte();

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


	//prints out peer information
	public String toString(){
		String peerInfo = "Peer Information:" + peerID +  ", " + IP + ", " + port; 
		return peerInfo;
	}



}
