package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import GUI.TorrentGUI.ButtonListener;

public class TorrentDownloadInfo {

	JFrame frame, messageFrame;
	JPanel[] panel;
	JTextPane textArea, errorDescription;
	JTextField[] tf;
	JScrollPane sp;
	JLabel name, size, downloaded, left, speed;
	JButton close;
	ButtonListener listener;
	
	public TorrentDownloadInfo() {
		
		frame = new JFrame("Download Information");
		messageFrame = new JFrame("Complete");
		panel = new JPanel[8];
		for(int i = 0; i < 6; i++) {
			panel[i] = new JPanel();
			panel[i].setLayout(new BoxLayout(panel[i], BoxLayout.X_AXIS));
		}
		for(int i = 6; i < 8; i++) {
			panel[i] = new JPanel();
			panel[i].setLayout(new BoxLayout(panel[i], BoxLayout.Y_AXIS));
		}
		tf = new JTextField[5];
		for(int i = 0; i < 5; i++) {
			tf[i] = new JTextField();
			tf[i].setEditable(false);
			tf[i].setBackground(Color.WHITE);
			tf[i].setMaximumSize(new Dimension(130, 15));
		}
		listener = new ButtonListener(this);
		close = new JButton("Close");
		close.addActionListener(listener);
		textArea = new JTextPane();
		textArea.setMinimumSize(new Dimension(250, 200));
		textArea.setMinimumSize(new Dimension(250, 200));
		textArea.setEditable(false);
		errorDescription = new JTextPane();
		sp = new JScrollPane(textArea);
		name = new JLabel("Name of file: ");
		size = new JLabel("Size of file: ");
		downloaded = new JLabel("Downloaded: ");
		left = new JLabel("Remaining: ");
		speed = new JLabel("Download Speed: ");
		setupGUI();		
	}
	
	private void setupGUI() {
		
		createCompletedDescriptionPanel();
		createGUI();
	}
	
	private void createCompletedDescriptionPanel() {
		
		SimpleAttributeSet attribs = new SimpleAttributeSet();  
		StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_CENTER);  
		errorDescription.setText("Download has been completed. Now seeding.");
		errorDescription.setParagraphAttributes(attribs, true);
		errorDescription.setEditable(false);
		errorDescription.setMaximumSize(new Dimension(200,115));
		errorDescription.setBackground(null);
		close.setAlignmentX(JButton.CENTER_ALIGNMENT);
		panel[6].setSize(new Dimension(300,200));
		panel[6].add(Box.createRigidArea(new Dimension (0, 10)));
		panel[6].add(errorDescription);
		panel[6].add(close);
		panel[6].add(Box.createRigidArea(new Dimension(0,20)));
		messageFrame.add(panel[6]);
		messageFrame.setLocationRelativeTo(null);
		messageFrame.setSize(new Dimension(300, 150));
		messageFrame.setResizable(false);
		messageFrame.setVisible(true);
	}
	
	private void createGUI() {
		
		panel[0].add(Box.createRigidArea(new Dimension(20,0)));
		panel[0].add(sp);
		panel[0].add(Box.createRigidArea(new Dimension(20,0)));
		panel[1].add(name);
		panel[1].add(Box.createRigidArea(new Dimension(62,0)));
		panel[1].add(tf[0]);
		panel[1].add(Box.createRigidArea(new Dimension(26,0)));
		panel[2].add(size);
		panel[2].add(Box.createRigidArea(new Dimension(71,0)));
		panel[2].add(tf[1]);
		panel[2].add(Box.createRigidArea(new Dimension(25,0)));
		panel[3].add(downloaded);
		panel[3].add(Box.createRigidArea(new Dimension(58,0)));
		panel[3].add(tf[2]);
		panel[3].add(Box.createRigidArea(new Dimension(25,0)));
		panel[4].add(left);
		panel[4].add(Box.createRigidArea(new Dimension(68,0)));
		panel[4].add(tf[3]);
		panel[4].add(Box.createRigidArea(new Dimension(25,0)));
		panel[5].add(speed);
		panel[5].add(Box.createRigidArea(new Dimension(32,0)));
		panel[5].add(tf[4]);
		panel[5].add(Box.createRigidArea(new Dimension(23,0)));
		
		panel[7].add(Box.createRigidArea(new Dimension(0,20)));
		panel[7].add(panel[0]);
		panel[7].add(Box.createRigidArea(new Dimension(0,20)));
		panel[7].add(panel[1]);
		panel[7].add(Box.createRigidArea(new Dimension(0,10)));
		panel[7].add(panel[2]);
		panel[7].add(Box.createRigidArea(new Dimension(0,10)));
		panel[7].add(panel[3]);
		panel[7].add(Box.createRigidArea(new Dimension(0,10)));
		panel[7].add(panel[4]);
		panel[7].add(Box.createRigidArea(new Dimension(0,10)));
		panel[7].add(panel[5]);
		panel[7].add(Box.createRigidArea(new Dimension(0,20)));
		
		frame.add(panel[7]);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setSize(new Dimension(350,350));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	@SuppressWarnings("deprecation")
	public void hideCompletePanel() {
		
		messageFrame.hide();
		frame.enable();
		frame.show();
	}
	
	@SuppressWarnings("deprecation")
	public void showCompletePanel() {
		
		messageFrame.show();
		frame.disable();
	}
	class ButtonListener implements ActionListener {

		TorrentDownloadInfo tdi;
		
		public ButtonListener(TorrentDownloadInfo tdi) {
		
			this.tdi = tdi;
		}
		public void actionPerformed(ActionEvent e) {
			
			if(e.getSource() == tdi.close) {
				tdi.hideCompletePanel();
			}
		}
	}
}