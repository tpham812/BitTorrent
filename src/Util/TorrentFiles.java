package Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.TreeSet;

public class TorrentFiles {

	private static TreeSet<String> TorrentFilesSet;

	
	public static void addTorrentFiles(String name) {
		
		TorrentFilesSet.add(name);
	}
	
	public static TreeSet<String> getTorrentFiles() {
		return TorrentFilesSet;
	}
	public static void Serialize() {
		
		if(TorrentFilesSet.size() == 0) return;
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream("TorrentFiles.ser");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(TorrentFilesSet);
		} catch (Exception e) {
			System.out.println("ERROR: Could not serialize file.");
		} finally {
			try {
				if(fos != null) fos.close();
			} catch (Exception e){
				System.out.println("ERROR: Could not close FileOutputStream");
			}
			try {
				if(oos != null) oos.close();
			} catch (Exception e) {
				System.out.println("ERROR: Could not close ObjectOutputStream");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void DeSerialize() {
		
		File file = new File("TorrentFiles.ser");
		if(!file.exists()) {
			TorrentFilesSet = new TreeSet<String>();
			return;
		}
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream("TorrentFiles.ser");
			ois = new ObjectInputStream(fis);
			TorrentFilesSet = (TreeSet<String>)ois.readObject();
		} catch(Exception e) {
			System.out.println("ERROR: Could not deserialize file.");
		} finally {
			try {
				if(fis != null) fis.close();
			} catch (Exception e){
				System.out.println("ERROR: Could not close FileInputStream");
			}
			try {
				if(ois != null) ois.close();
			} catch (Exception e) {
				System.out.println("ERROR: Could not close ObjectInputStream");
			}
		}	
	}
}