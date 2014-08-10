package BitTorrent;

import java.io.IOException;

public class KeepAliveThread implements Runnable {

	private boolean end;
	
	public KeepAliveThread() {
		
		end = false;
	}
	
	public void EndThread () {
		
		end = true;
	}
	
	public void run() {
		
		while(!end) {
			try {
				Thread.sleep(1500);
				Message keepAlive = new Message(0,(byte)-1);
				for (int i = 0; i< PeerConnectionsInfo.downloadPeers.size(); i++){
					PeerConnectionsInfo.downloadPeers.get(i).os.write(keepAlive.message);;
					PeerConnectionsInfo.downloadPeers.get(i).os.flush();
				}
			} catch (Exception e) {
				System.out.println("Could not send keep alive message to peers");
			} 
		}
	}
}