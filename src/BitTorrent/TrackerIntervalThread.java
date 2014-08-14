package BitTorrent;

/**
 * This class implements Runnable which is a thread to update information to tracker
 * @author Amulya Uppala, Truong Pham, Jewel Lim
 *
 */
public class TrackerIntervalThread implements Runnable {

	private boolean end;
	private int seconds;
	
	public TrackerIntervalThread (int seconds) {
		
		end = false;
		this.seconds = seconds;
	}
	public void EndThread() {
		
		end = true;
	}
	public void run() {
		
		while(!end) {
			try {
				Thread.sleep(seconds * seconds);
			
			} catch (InterruptedException e) {
			
				e.printStackTrace();
			}
		}
	}
}
