package BitTorrent;

import java.io.IOException;
import java.net.ServerSocket;

public class PeerListener implements Runnable {

	ServerSocket ss;
	
	public PeerListener() {
		
		try {
			ss = new ServerSocket();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void run() {
		
		while(true) {
			
		}
	}

}
