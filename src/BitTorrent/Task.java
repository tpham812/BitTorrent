package BitTorrent;


public class Task implements Runnable {

	public boolean stop;
	public int seconds;
	@Override
	public void run() {
		stop = false;
		while(!false) {
			try {
				Thread.sleep(3000);
				seconds = seconds + 1;
				System.out.println(seconds);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}