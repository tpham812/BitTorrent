package BitTorrent;

/**
 * This class represents message types sent between two peers through a stream of bytes.
 * @author Amulya Uppala, Jewel Lim, Truong Pham
 *
 */

public class Message {
	/** The message id byte that pertains to keep alive when communicating with peer.*/
	public static final byte MSG_KEEP_ALIVE = -1;
	/** The message id byte that pertains to choke when communicating with peer.*/
	public static final byte MSG_CHOKE = 0;
	/** The message id byte that pertains to unchoke when communicating with peer.*/
	public static final byte MSG_UNCHOKE = 1;
	/** The message id byte that pertains to interested when communicating with peer.*/
	public static final byte MSG_INTERESTED = 2;
	/** The message id byte that pertains to not interested when communicating with peer.*/
	public static final byte MSG_NOT_INTERESTED = 3;
	/** The message id byte that pertains to have when communicating with peer.*/
	public static final byte MSG_HAVE = 4;
	/** The message id byte that pertains to bitfield when communicating with peer.*/
	public static final byte MSG_BITFIELD = 5;
	/** The message id byte that pertains to request when communicating with peer.*/
	public	static final byte MSG_REQUEST = 6;
	/** The message id byte that pertains to piece when communicating with peer.*/
	public static final byte MSG_PIECE = 7;
	/** The message id byte.*/
	public byte id;
	/** Specifies the length of the message*/
	public int lengthPrefix;
	/** The byte array that stores the message to send to peer.*/
	public byte[] message;

	/**
	 * @param lengthPrefix
	 * @param msgID
	 * 
	 * This method is the constructor specifies the different message types sent between two peers. 
	 */
	public Message (int lengthPrefix, byte msgID){
		/**adds the length prefix and message id needed*/ 
		this.lengthPrefix = lengthPrefix;
		this.id = msgID;
		this.message = new byte[this.lengthPrefix + 4];
		switch(id){
		case MSG_CHOKE:
			System.arraycopy(Helper.intToByteArray(lengthPrefix), 0, this.message, 0, 4);
			this.message[4] = (byte) 0;
			break;
		case MSG_UNCHOKE:
			System.arraycopy(Helper.intToByteArray(lengthPrefix), 0, this.message, 0, 4);
			this.message[4] = (byte) 1;
			break;
		case MSG_KEEP_ALIVE:
			//empty 
			break;
		case MSG_INTERESTED:
			System.arraycopy(Helper.intToByteArray(lengthPrefix), 0, this.message, 0, 4);
			this.message[4] = (byte) 2;
			break;
		case MSG_NOT_INTERESTED:
			System.arraycopy(Helper.intToByteArray(lengthPrefix), 0, this.message, 0, 4);
			this.message[4] = (byte) 3;
			break;
		case MSG_HAVE:
			System.arraycopy(Helper.intToByteArray(lengthPrefix), 0, this.message, 0, 4);
			this.message[4] = (byte) 4;
			break;
		case MSG_REQUEST: 
			System.arraycopy(Helper.intToByteArray(lengthPrefix), 0, this.message, 0, 4);
			this.message[4] = (byte) 6;
			break;
		case MSG_PIECE:
			System.arraycopy(Helper.intToByteArray(lengthPrefix), 0, this.message, 0, 4);
			this.message[4] = (byte) 7;
			break;
		default: break;
		}
	}
	
	
	

	/**
	 * @param Index integer specifying the zero-based piece index
	 * @param begin zero-based byte offset within the piece 
	 * @param block integer specifying the requested length
	 * 
	 * This method sets the payload for messages that are type HAVE, REQUEST, or PIECE.
	 * If it is not one of those messages, it outputs an error.  
	 * Payloads are specific bytes that are added onto have, piece and request messages that describe the data.
	 */
	public void setPayload(int index, int begin, int length, byte[] bitfield) {
		/**Turn the required info into a byte array (big endian) and add it to the message. */
		if (id == MSG_HAVE) {
			System.arraycopy(Helper.intToByteArray(length), 0,message, 5, 4);
			
		} else if (id == MSG_PIECE) {
			System.arraycopy(Helper.intToByteArray(-1), 0, message, 5, 4);
			System.arraycopy(Helper.intToByteArray(begin), 0,message, 9, 4); 
			System.arraycopy(null, 0, message, 13, lengthPrefix-9);
			
		} else if (id == MSG_REQUEST) {
			System.arraycopy(Helper.intToByteArray(length),0,message, 5, 4);
			System.arraycopy(Helper.intToByteArray(begin), 0,message, 9, 4);
			System.arraycopy(Helper.intToByteArray(index), 0,message, 13, 4);
		}else if (id == MSG_BITFIELD){
			System.arraycopy(bitfield, 0, message, 5, bitfield.length);
		}else {
			System.out.println("Error: Payload requested for wrong message id type. Payload can only be done for have, pieces and request ids only.");
		}
		return;
	}
}