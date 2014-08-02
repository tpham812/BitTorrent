package BitTorrent;

import java.io.IOException;

/**
 * This class handles peers we are uploading to => peers who want to download from us
 * we have to recieve handshakes, interested and unchoke messages
 * then run method will upload what we have and close connection
 * See method called upload()
 * */
public class UPeer extends DPeer{

	public UPeer(String ip, byte[] id, int port, String fileOutArg)
			throws IOException, InterruptedException {
		super(ip, id, port, fileOutArg);
		
	}
	
	/**
	 * reads in request messages and sends the piece
	 * reads in have messages and does nothing
	 * */
	public void upload(){
		
	}
	
	public void run() {
		
		
	}

}
