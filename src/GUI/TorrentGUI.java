package GUI;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class TorrentGUI {

	JPanel panel;
	JFrame frame;
	JScrollPane sp;
	JList<String> list;
	JButton help, upload;
	JLabel label;

	
	public TorrentGUI() {
		
		panel = new JPanel();
		frame = new JFrame("Bit Torrent");
		label = new JLabel("Torrent Files");
		help = new JButton("?");
		upload = new JButton("Upload");
		createGUI();
	}
	
	private void createGUI() {
		
	}
}
