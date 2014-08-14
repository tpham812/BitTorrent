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

	private Peer peer;

	private FileChunks fc;


	public Upload(Peer peer, FileChunks fc) throws IOException, InterruptedException {

		this.peer = peer;
		this.fc = fc;
	}

	public void receiveHandshake(byte[] info_hash) throws IOException{
		//read handshake from download peer
		byte[] receiveHandshake =  new byte[68];
		peer.is.readFully(receiveHandshake);

		//send handshake back
		byte[] returnShake = new byte[68];
		returnShake[0] = (byte)19;
		System.arraycopy(peer.BitProtocol, 0,returnShake,1,19);
		System.arraycopy(peer.eightZeros, 0, returnShake, 20, 8);
		System.arraycopy(info_hash,0, returnShake, 28, 20);
		System.arraycopy(ConnectToTracker.ourPeerID, 0, returnShake, 48, 20);

		if(receiveHandshake[0] != (byte) 19){
			System.out.println("Not a Bit Torrent Protocol.");
		} else{
			peer.os.write(returnShake);
			peer.os.flush();
		}

		//send bitfield message before unchoke
		Message bitfieldMsg = new Message(1, (byte) 5);
		System.out.println("Sending bitfield message to peer.");
		peer.os.write(bitfieldMsg.message);
		peer.os.flush();/**push message to stream*/
		System.out.println("Finished writing message to peer.");


		//does not upload if not unchoked
		if(PeerConnectionsInfo.unchokedPeers.contains(peer)){
			Message unchokeMsg = new Message(1,(byte)1); /**create unchoke message*/
			System.out.println("Writing unchoke message to peer.");
			peer.os.write(unchokeMsg.message);
			peer.os.flush();/**push message to stream*/
			System.out.println("Finished unchoke writing message to peer.");


		}else{
			//choke msg 
			Message chokeMsg = new Message(1,(byte)0); /**create choke message*/
			System.out.println("Writing choke message to peer.");
			peer.os.write(chokeMsg.message);
			peer.os.flush();/**push message to stream*/
			System.out.println("Finished writing message to peer.");
		}
	}


	/**
	 * reads in request messages and sends the piece
	 * reads in have messages and does nothing
	 * @throws IOException 
	 * */
	

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
