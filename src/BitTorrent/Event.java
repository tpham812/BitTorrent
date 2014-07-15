package BitTorrent;
 
/**
 * This class holds events for GET request
 * @author Amulya Uppala, Truong Pham, Jewel Lim
 *
 */
public class Event {

	/**
	 * Start event
	 * @return start event to concatenate for GET request
	 */
	public static String sendStartedEvent() {
		
		return "&event=started";
	}
	
	/**
	 * Start event
	 * @return completed event to concatenate for GET request
	 */
	public static String sendCompletedEvent() {
		
		return "&event=completed";
	}
	
	/**
	 * Start event
	 * @return stopped event to concatenate for GET request
	 */
	public static String sendStoppedEvent() {
		
		return "&event=stopped";
	}
}
