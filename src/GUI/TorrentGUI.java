package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import Util.DefaultListModelAction;
import Util.TorrentFiles;


public class TorrentGUI {

	JPanel [] panel = new JPanel[5];
	JFrame frame, helpFrame;
	JScrollPane sp;
	JTextField tf;
	JFileChooser fc;
	JList<String> list;
	JButton help, browse, cancel, close;
	JLabel label;
	JTextPane errorDescription;
	ButtonListener listener;
	FrameListener frameListener;
	DefaultListModel<String> torrentModel; 

	
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
		helpFrame = new JFrame("Help");
		label = new JLabel("Torrent Files");
		fc = new JFileChooser();
		errorDescription = new JTextPane();
		tf = new JTextField();
		tf.setEditable(true);
		//tf.setBackground(Color.WHITE);
		tf.setMaximumSize(new Dimension(200,20));
		listener = new ButtonListener(this);
		frameListener = new FrameListener(this);
		help = new JButton("?");
		help.addActionListener(listener);
		browse = new JButton("Browse");
		browse.addActionListener(listener);
		cancel = new JButton("Cancel");
		close = new JButton("Close");
		close.addActionListener(listener);
		torrentModel = new DefaultListModel<String>();
		setupGUI();
	}
	
	private void setupGUI() {
		
		TorrentFiles.DeSerialize();
		//TreeSet<File> torrSet = TorrentFiles.getTorrentFiles();
	//	String[] torrentFiles = new String[torrSet.size()];
		//torrSet.toArray(torrentFiles);
		//DefaultListModelAction.newList(torrentModel, torrentFiles);
		list = new JList(torrentModel);
		sp = new JScrollPane(list);
		sp.setMaximumSize(new Dimension(450, 400));
		createHelpDescriptionPanel();
		createGUI();
	}
	
	private void createGUI() {
		
		panel[0].add(Box.createRigidArea(new Dimension(183,0)));
		panel[0].add(label);
		panel[0].add(Box.createRigidArea(new Dimension(150,0)));
		panel[0].add(help);
		
		panel[1].add(sp);
		
		panel[2].add(tf);
		panel[2].add(Box.createRigidArea(new Dimension(20,0)));
		panel[2].add(browse);
		
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
		frame.setSize(new Dimension(600,600));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(frameListener);
	}
	
	private void createHelpDescriptionPanel() {
	
		SimpleAttributeSet attribs = new SimpleAttributeSet();  
		StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_CENTER);  
		errorDescription.setParagraphAttributes(attribs, true);
		
		errorDescription.setText("Double click on torrent to start downloading. Click upload to upload a torrent. Click X to exit program.");
		errorDescription.setEditable(false);
		errorDescription.setMaximumSize(new Dimension(200,115));
		errorDescription.setBackground(null);
		close.setAlignmentX(JButton.CENTER_ALIGNMENT);
		panel[4].setSize(new Dimension(300,200));
		panel[4].add(Box.createRigidArea(new Dimension (0, 10)));
		panel[4].add(errorDescription);
		panel[4].add(close);
		helpFrame.add(panel[4]);
		helpFrame.setLocationRelativeTo(null);
		helpFrame.setSize(new Dimension(300, 200));
		helpFrame.setResizable(false);
		helpFrame.setVisible(false);
		helpFrame.addWindowListener(frameListener);
	}

	
	@SuppressWarnings("deprecation")
	public void hideHelpPanel() {
		
		helpFrame.setVisible(false);
		frame.enable();
		frame.setVisible(true);
	}
	
	@SuppressWarnings("deprecation")
	public void showHelpPanel() {
		helpFrame.setVisible(true);
		frame.disable();
	}
	
	class ButtonListener implements ActionListener {

		TorrentGUI tGUI;
		
		public ButtonListener(TorrentGUI tGUI) {
			
			this.tGUI = tGUI;
		}
		
		public void actionPerformed(ActionEvent e) {
			
			if(e.getSource() == tGUI.browse) {
				int value = fc.showOpenDialog(tGUI.frame);
				File file = null;
				if(value == JFileChooser.APPROVE_OPTION) {
					file = fc.getSelectedFile();
				}
			}
			else if(e.getSource() == tGUI.help) {
				tGUI.showHelpPanel();
			}
			else if(e.getSource() == tGUI.close) {
				tGUI.hideHelpPanel();
			}
		}
	}
	class FrameListener implements WindowListener {
		
		TorrentGUI tGUI;
		
		public FrameListener(TorrentGUI tGUI) {
			
			this.tGUI = tGUI;
		}

		public void windowActivated(WindowEvent arg0) {
		}
		public void windowClosed(WindowEvent arg0) {	
		}
		public void windowClosing(WindowEvent arg0) {
			
			if(arg0.getSource() == tGUI.frame)
				TorrentFiles.Serialize();
			else if(arg0.getSource() == tGUI.helpFrame)
				tGUI.hideHelpPanel();
		}
		public void windowDeactivated(WindowEvent arg0) {
			
		}
		public void windowDeiconified(WindowEvent arg0) {	
		}
		public void windowIconified(WindowEvent arg0) {
		}
		public void windowOpened(WindowEvent arg0) {
		}
	}
}