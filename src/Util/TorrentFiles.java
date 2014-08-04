package Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class TorrentFiles {

	
	private static HashMap<String, File> torrents = new HashMap<String, File>();
	public static void addTorrentFiles(String name, File file) {
		
		torrents.put(name,file);
	}
	
	public static TreeSet<String> getTorrentFiles() {
		
		TreeSet<String> set = new TreeSet<String>(torrents.keySet());
		return set;
	}
	public static void Serialize() {
		
		if(torrents.size() == 0) return;
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream("TorrentFiles.ser");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(torrents);
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
			torrents = new HashMap<String, File>();
			CreateTorrentFolder();
			return;
		}
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream("TorrentFiles.ser");
			ois = new ObjectInputStream(fis);
			torrents = (HashMap<String, File>)ois.readObject();
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
	
	private static void CreateTorrentFolder() {

		File newFolder = new File("Torrents");
		if(!newFolder.exists()) {
			if(!newFolder.mkdir()) {
				System.out.println("ERROR: Could not create folder to store torrents.");
			}
		}
	}
}