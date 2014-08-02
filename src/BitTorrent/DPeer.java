package BitTorrent;

import java.io.IOException;
/**
 * This class is the situation where we download from other peers. 
 * Most of the stuff from Peer class go here. 
 * Split up some methods from peer class into smaller methods because the large method is too big
 * */
public class DPeer extends Peer{

	public DPeer(String ip, byte[] id, int port, String fileOutArg)
			throws IOException, InterruptedException {
		super(ip, id, port, fileOutArg);
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
