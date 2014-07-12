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
	String ourID;
	FileOutputStream fileoutput;
	ArrayList<byte[]> chunks = new ArrayList<byte[]>();
	static byte[] BitProtocol = new byte[]{'B','i','t','T','o','r','r','e','n','t',' ','P','r','o','t','o','c','o','l'};
	static byte[] eightZeros = new byte[]{'0','0','0','0','0','0','0','0'};

	//constructor 
	public Peer(int port, String IP){ 
		this.port = port;
		this.IP = IP;


	} 
	public Peer(String ip, int port, byte[] id, String fileOutArg) throws IOException{
		this.IP = ip;
		this.port = port;
		this.torrentInfo = ConnectToTracker.torrentI;
		this.peerID = id;
		this.infoHash = torrentInfo.info_hash.array();
		this.fileOutArg = fileOutArg;
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
	
	private void downloadFileFromPeer() throws IOException {
		Message interested = new Message(1,(byte)2);
		byte[] chunk;
		byte[] tempBuff;
		int numChunks = 0;
		Message askForPieces;
		int requestIndex = 16384; //typical length asked
		int left;
		int lastSize;
		int curr = 0;
		/*Three things hav payload = have, piece and request
		 * byte[] pieceBlock, int pieceBegin, pieceIndex, requesting Length,
		 * requesting Begin, requesting Index, havePayload
		 * if have then we need to arraycopy inttoByteArray(have payload), 0, message, 5,4
		 * if it's piece then copy the inttobytearray(pieceIndex) to message from 5,4)
		 * then do same for piece begin from 9 to 4.
		 * then add the piece block from 13 to the length -9; (see peer lenghts at beginnig of string)
		 * if request then 
		 * intobytearray(requestIndex) from 5 to 4
		 * same (requesting begin) from 9 to 4
		 * same(requesting lenght) from 13,4)
		 * 
		 * */
		
		for (int i = 0; i<6;i++){
			in.readByte();
		}//get rid of
		
		os.write(interested.message);
		os.flush();//clear output
		
		for (int j =0; j<5; j++ ){
			if (j == 4){
				if (in.readByte()==1){ //not being chocked
					break;
				}
			}
			in.readByte();
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
						requestIndex = 16384;
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
		System.arraycopy(peerID, 0, message, 48, 20);

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
			os.flush();

			byte[] peerAns = new byte[68];
			in.readFully(peerAns);
			byte[] peerInfoHash = Arrays.copyOfRange(peerAns, 28, 48);
			for (int i = 0; i<20;i++){
				if (peerInfoHash[i]!=infoHash[i]){
					System.out.println("Error: Peer's info hash returned from handshake is not same.");
					finishConnection();
					break;
				}		
			}
			

		} catch (IOException e) {
			System.out.println("Error: Could not handshake with peer.");
		}





	}
	//sets Peer ID to to a random string
	public String setOurID(){
		ourID = Helper.generateRandomPeerID();
		return ourID;
	}


	//prints out peer information
	public String toString(){
		String peerInfo = "Peer Information:" + peerID +  ", " + IP + ", " + port; 
		return peerInfo;
	}



}
