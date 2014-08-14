package BitTorrent;

import java.util.ArrayList;

/**
 * Data Structure to help determine the rarest piece first algorithm.
 * @author Amulya Uppala Truong Pham
 * */
public class bitFieldDS {
	int sum;
	public ArrayList<Peer> lp;
	
	public bitFieldDS(){
		sum = 0;
		lp = new ArrayList<Peer>();
	}
}
