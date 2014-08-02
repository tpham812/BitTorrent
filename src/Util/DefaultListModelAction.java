package Util;

import javax.swing.DefaultListModel;

public class DefaultListModelAction {

	public static void newList(DefaultListModel<String> model, String[] users) {
		
		model.clear();
		for(int count = 0; count < users.length; count++) {
			model.addElement(users[count]);
		}
	}
}
