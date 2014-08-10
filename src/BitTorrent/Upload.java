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


//!!!!!!!!!Jewel's Notes: Need to implement optimistically unchoked!!!!!
//!!!!!!!!!AND how do we keep tracked of choked/unchoked peers? !!!!


public class Upload implements Runnable{

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

	private Peer peer;

	private FileChunks fc;


	public Upload(Peer peer, FileChunks fc) throws IOException, InterruptedException {

		this.peer = peer;
		this.fc = fc;
	}

	public void receiveHandshake(byte[] info_hash) throws IOException{
		//read handshake from download peer
		byte[] receiveHandshake =  new byte[68];
		in.readFully(receiveHandshake);

		//send handshake back
		byte[] returnShake = new byte[68];
		returnShake[0] = (byte)19;
		System.arraycopy(peer.BitProtocol, 0,returnShake,1,19);
		System.arraycopy(peer.eightZeros, 0, returnShake, 20, 8);
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
		if(PeerConnectionsInfo.unchokedPeers.contains(peer)){
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


	/**
	 * reads in request messages and sends the piece
	 * reads in have messages and does nothing
	 * @throws IOException 
	 * */
	public void upload() throws IOException{
		Message pieceMsg;
		byte[] block;
		int index, begin, length;
		long startTime;
		long endTime;
		int fileLength = ConnectToTracker.torrentI.piece_hashes.length;
		int pieceLength = ConnectToTracker.torrentI.piece_length;
		int lastPieceLength = pieceLength - ((fileLength - 1)*pieceLength); //Is this correct?! 
		int lastPieceIndex = fileLength - 1;
		
		while(true){
			int msg = Peer.readMessage();
			if(msg == Message.MSG_REQUEST){

				index = in.readInt();
				begin = in.readInt();
				length = in.readInt();

				if(FileChunks.ourBitField[index] == true){
					block = new byte[length];
					System.arraycopy((FileChunks.booleanToByteBitField(fc.ourBitField))[index], begin, block, 0, length);
					pieceMsg = new Message(9 + length, (byte) 7);
					pieceMsg.setPayload(index, begin, length);
					startTime = System.nanoTime();
					os.write(pieceMsg.message);
					os.flush();

					int msg2 = Peer.readMessage();
					if(msg2 == Message.MSG_HAVE){
						endTime = System.nanoTime();					
						if(index == lastPieceIndex){
							peer.throughput = (double)lastPieceLength / ((endTime - startTime)/1000000000.0);						} 
						else {
							peer.throughput = (double)pieceLength / ((endTime -startTime)/1000000000.0);						
						}
					}else{
						System.out.println("No Have message received.");
						//need to end timer					
					}

				}else {
					System.out.println("I don't have this piece " + index);
				}
			}else if (msg == Message.MSG_HAVE){
					try {
						// read the next request
						msg = Peer.readMessage(); 
					} catch (Exception e) {
						// no following messages
						return;
					}

			}else {
					// other messages received
				}

		}
	}

	public void isStopped() throws IOException{
		//threads? msg from tracker?
		//put condition here
		//finishConnection();
	}


	public void run() {

		System.out.println("Uploading to some peer on new thread.");
		try {
			upload();
			//finishConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}
