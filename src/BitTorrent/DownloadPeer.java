package BitTorrent;

public class DownloadPeer extends Peer{

	public DownloadPeer(int port, String IP) {
		super(port, IP);
	}
	
	public void trackPieces(){
		
	}
	
	public boolean initiateHandShake(){
		return false;
		
	}
	
	
	public void downloadPiece(){
		
	}
	
	public boolean verifyPiece(){
		return false;
		
	}
	
	
	
	

}
