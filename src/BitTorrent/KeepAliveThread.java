package BitTorrent;

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
				//keepAlive method
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}