package GUI;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JTextField;

public class TorrentDownloadInfo {

	JFrame frame;
	JPanel[] panel;
	JTextPane textArea;
	JTextField[] tf;
	JLabel name, size, downloaded, left, speed;
	
	public TorrentDownloadInfo() {
		
		frame = new JFrame("Download Information");
		panel = new JPanel[7];
		for(int i = 1; i < 6; i++) {
			panel[i] = new JPanel();
			panel[i].setLayout(new BoxLayout(panel[i], BoxLayout.X_AXIS));
		}
		
		panel[0] = new JPanel();
		panel[0].setLayout(new BoxLayout(panel[0], BoxLayout.X_AXIS));
		panel[6] = new JPanel();
		panel[6].setLayout(new BoxLayout(panel[6], BoxLayout.Y_AXIS));
		tf = new JTextField[5];
		for(int i = 0; i < 5; i++) {
			tf[i] = new JTextField();
			tf[i].setEditable(false);
			tf[i].setBackground(Color.white);
			tf[i].setMaximumSize(new Dimension(130, 15));
		}
		textArea = new JTextPane();
		textArea.setMinimumSize(new Dimension(250, 200));
		textArea.setMinimumSize(new Dimension(250, 200));
		textArea.setEditable(false);
		name = new JLabel("Name of file: ");
		size = new JLabel("Size of file: ");
		downloaded = new JLabel("Downloaded: ");
		left = new JLabel("Remaining: ");
		speed = new JLabel("Download Speed: ");
		createGUI();		
	}
	
	private void createGUI() {
		
		panel[0].add(Box.createRigidArea(new Dimension(20,0)));
		panel[0].add(textArea);
		panel[0].add(Box.createRigidArea(new Dimension(20,0)));
		panel[1].add(name);
		panel[1].add(Box.createRigidArea(new Dimension(57,0)));
		panel[1].add(tf[0]);
		panel[1].add(Box.createRigidArea(new Dimension(30,0)));
		panel[2].add(size);
		panel[2].add(Box.createRigidArea(new Dimension(68,0)));
		panel[2].add(tf[1]);
		panel[2].add(Box.createRigidArea(new Dimension(28,0)));
		panel[3].add(downloaded);
		panel[3].add(Box.createRigidArea(new Dimension(56,0)));
		panel[3].add(tf[2]);
		panel[3].add(Box.createRigidArea(new Dimension(27,0)));
		panel[4].add(left);
		panel[4].add(Box.createRigidArea(new Dimension(67,0)));
		panel[4].add(tf[3]);
		panel[4].add(Box.createRigidArea(new Dimension(25,0)));
		panel[5].add(speed);
		panel[5].add(Box.createRigidArea(new Dimension(32,0)));
		panel[5].add(tf[4]);
		panel[5].add(Box.createRigidArea(new Dimension(23,0)));
		
		panel[6].add(Box.createRigidArea(new Dimension(0,20)));
		panel[6].add(panel[0]);
		panel[6].add(Box.createRigidArea(new Dimension(0,20)));
		panel[6].add(panel[1]);
		panel[6].add(Box.createRigidArea(new Dimension(0,10)));
		panel[6].add(panel[2]);
		panel[6].add(Box.createRigidArea(new Dimension(0,10)));
		panel[6].add(panel[3]);
		panel[6].add(Box.createRigidArea(new Dimension(0,10)));
		panel[6].add(panel[4]);
		panel[6].add(Box.createRigidArea(new Dimension(0,10)));
		panel[6].add(panel[5]);
		panel[6].add(Box.createRigidArea(new Dimension(0,20)));
		
		frame.add(panel[6]);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setSize(new Dimension(350,350));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}