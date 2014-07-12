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
    public byte[] info_hash;
    public byte[] peerID;
    public byte[] piece;

    
    
    public Message (int lengthPrefix, byte msgID){
		this.lengthPrefix = lengthPrefix;
		this.id = msgID;
		this.message = new byte[this.lengthPrefix + 4];
		
		switch(id){
		case MSG_CHOKE:
			//create byte array
		case MSG_UNCHOKE:
			//create byte array
		case MSG_KEEP_ALIVE:
			//create byte array
		case MSG_INTERESTED:
			//create byte array
		case MSG_NOT_INTERESTED:
			//create byte array
		case MSG_HAVE:
			//create byte array
			//has payload, add this in
		case MSG_BITFIELD: 
			//create byte array
		case MSG_REQUEST: 
			//create byte array
			//has payload
		case MSG_PIECE:
			//create byte array
			//has payload
		case MSG_CANCEL:
			//create byte array	
		}
		
		
//error checking needed to make sure there are correct bytes for messages.
//all other messages besides HAVE, REQUEST, and PIECE, have no payload
		
	}
    public byte[] getPayload() throws Exception {
        byte[] payload = null;
        if (id == MSG_HAVE  || id == MSG_REQUEST || id == MSG_PIECE) {
        if (id == MSG_HAVE) {
        payload = new byte[4];
        System.arraycopy(this.message, 5, payload, 0, 4);
        return payload;

        } else if (id == MSG_PIECE) {
        payload = new byte[this.lengthPrefix - 1];
        System.arraycopy(this.message, 5, payload, 0,
        this.lengthPrefix - 1);
        return payload;

        } else {
        // request message
        payload = new byte[12];
        System.arraycopy(this.message, 5, payload, 0, 12);
        return payload;
        }
        } else {
        throw new Exception("No payload for this type of message.");
        }
    }   
	
	
}
