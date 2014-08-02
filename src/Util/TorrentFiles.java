package Util;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.TreeSet;

public class TorrentFiles {

	private static TreeSet<String> TorrentFilesSet = null;
	
	
	public static void addTorrentFiles(String name) {
		
		TorrentFilesSet.add(name);
	}
	
	public static void Serialize() {
		
		try {
			FileOutputStream fos = new FileOutputStream("TorrentFiles.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(TorrentFilesSet);
			oos.close();
			fos.close();
		} catch (Exception e) {
			System.out.println("ERROR: Could not serialize file.");
		}
	}
	
	public static void DeSerialize() {
		
		
	}
}
