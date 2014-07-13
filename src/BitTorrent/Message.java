package BitTorrent;

public class Message {
	public static final byte MSG_KEEP_ALIVE = -1;
	public static final byte MSG_CHOKE = 0;
	public static final byte MSG_UNCHOKE = 1;
	public static final byte MSG_INTERESTED = 2;
	public static final byte MSG_NOT_INTERESTED = 3;
	public static final byte MSG_HAVE = 4;
	public static final byte MSG_BITFIELD = 5;
	public static final byte MSG_REQUEST = 6;
	public static final byte MSG_PIECE = 7;
	public static final byte MSG_CANCEL = 8;
	public final byte id;
	public int lengthPrefix;
	public byte[] message;


	public Message (int lengthPrefix, byte msgID){
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

	public void setPayload(int requestIndex, int currentDL, int numChunks) {

		if (id == MSG_HAVE) {
			System.arraycopy(Helper.intToByteArray(4), 0,message, 5, 4);
		} else if (id == MSG_PIECE) {

			System.arraycopy(Helper.intToByteArray(-1), 0, message, 5, 4);
			System.arraycopy(Helper.intToByteArray(currentDL), 0,message, 9, 4); 
			System.arraycopy(null, 0, message, 13, lengthPrefix-9);

		} else if (id == MSG_REQUEST) {

			System.arraycopy(Helper.intToByteArray(numChunks),0,message, 5, 4);
			System.arraycopy(Helper.intToByteArray(currentDL), 0,message, 9, 4);
			System.arraycopy(Helper.intToByteArray(requestIndex), 0,message, 13, 4);


		} else {
			System.out.println("Error: Payload requested for wrong message id type. Payload can only be done for have, pieces and request ids only.");

		}
		return;
	}
}