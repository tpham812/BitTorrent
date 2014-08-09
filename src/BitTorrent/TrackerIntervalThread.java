package BitTorrent;

public class TrackerIntervalThread implements Runnable {

	private boolean end;
	private int seconds;
	
	public TrackerIntervalThread (int seconds) {
		
		end = false;
		this.seconds = seconds;
	}
	public void endThread() {
		
		end = true;
	}
	public void run() {
		
		while(!end) {
			try {
				Thread.sleep(seconds * 1000);
				//tracker interval method here 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
