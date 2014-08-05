package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


public class TorrentGUI {

	JPanel [] panel = new JPanel[5];
	JFrame frame, messageFrame;
	JTextField tf;
	JFileChooser fc;
	FileNameExtensionFilter filter;
	JList<String> list;
	JButton help, browse, cancel, close, start;
	JLabel label;
	JTextPane errorDescription;
	ButtonListener listener;

	public TorrentGUI() {
		 
		for(int i = 0; i < 3; i++) {
			panel[i] = new JPanel();
			panel[i].setLayout(new BoxLayout(panel[i], BoxLayout.X_AXIS));
		}
		panel[3] = new JPanel();
		panel[3].setLayout(new BoxLayout(panel[3], BoxLayout.Y_AXIS));
		panel[4] = new JPanel();
		panel[4].setLayout(new BoxLayout(panel[4], BoxLayout.Y_AXIS));
		frame = new JFrame("Bit Torrent");
		messageFrame = new JFrame("Help");
		label = new JLabel("Select Torrent");
		fc = new JFileChooser();
		filter = new FileNameExtensionFilter("Torrents", "torrent");
		fc.setFileFilter(filter);
		errorDescription = new JTextPane();
		tf = new JTextField();
		tf.setEditable(false);
		tf.setBackground(Color.WHITE);
		tf.setMaximumSize(new Dimension(200,20));
		listener = new ButtonListener(this);
		start = new JButton("Start");
		start.addActionListener(listener);
		help = new JButton("?");
		help.addActionListener(listener);
		browse = new JButton("Browse");
		browse.addActionListener(listener);
		cancel = new JButton("Cancel");
		close = new JButton("Close");
		close.addActionListener(listener);
		setupGUI();
	}
	
	private void setupGUI() {
		
		createHelpDescriptionPanel();
		createGUI();
	}
	
	private void createGUI() {
		
		panel[0].add(Box.createRigidArea(new Dimension(103,0)));
		panel[0].add(label);
		panel[0].add(Box.createRigidArea(new Dimension(160,0)));
		panel[0].add(help);
		
		panel[1].add(tf);
		panel[1].add(Box.createRigidArea(new Dimension(20,0)));
		panel[1].add(browse);
		
		panel[2].add(start);
		panel[2].add(Box.createRigidArea(new Dimension(85,0)));
		
		panel[3].add(Box.createRigidArea(new Dimension(0,10)));
		panel[3].add(panel[0]);
		panel[3].add(Box.createRigidArea(new Dimension(0,15)));
		panel[3].add(panel[1]);
		panel[3].add(Box.createRigidArea(new Dimension(0,15)));
		panel[3].add(panel[2]);
		
		frame.add(panel[3]);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setSize(new Dimension(400,165));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void createHelpDescriptionPanel() {
	
		SimpleAttributeSet attribs = new SimpleAttributeSet();  
		StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_CENTER);  
		errorDescription.setText("Browse through directories and select a torrent file. Click start to begin downloading. Click x on panel to end program.");
		errorDescription.setParagraphAttributes(attribs, true);
		errorDescription.setEditable(false);
		errorDescription.setMaximumSize(new Dimension(200,115));
		errorDescription.setBackground(null);
		close.setAlignmentX(JButton.CENTER_ALIGNMENT);
		panel[4].setSize(new Dimension(300,200));
		panel[4].add(Box.createRigidArea(new Dimension (0, 10)));
		panel[4].add(errorDescription);
		panel[4].add(close);
		messageFrame.add(panel[4]);
		messageFrame.setLocationRelativeTo(null);
		messageFrame.setSize(new Dimension(300, 200));
		messageFrame.setResizable(false);
		messageFrame.setVisible(false);
	}
	
	@SuppressWarnings("deprecation")
	public void hideMessagePanel() {
		
		messageFrame.hide();
		frame.enable();
		frame.show();
	}
	
	@SuppressWarnings("deprecation")
	public void showMessagePanel() {
		messageFrame.show();
		frame.disable();
	}
	
	class ButtonListener implements ActionListener {

		TorrentGUI tGUI;
		
		public ButtonListener(TorrentGUI tGUI) {
			
			this.tGUI = tGUI;
		}
		
		public void actionPerformed(ActionEvent e) {
			
			if(e.getSource() == tGUI.browse) {
				int value = tGUI.fc.showOpenDialog(tGUI.frame);
				File file = null;
				if(value == JFileChooser.APPROVE_OPTION) {
					file = tGUI.fc.getSelectedFile();
					tGUI.tf.setText(file.getName());
				}
			}
			else if(e.getSource() == tGUI.start) {
				tGUI.tf.setText(null);
			}
			else if(e.getSource() == tGUI.help) {
				tGUI.showMessagePanel();
			}
			else if(e.getSource() == tGUI.close) {
				tGUI.hideMessagePanel();
			}
		}
	}
}