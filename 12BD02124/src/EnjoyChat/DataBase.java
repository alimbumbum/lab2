package EnjoyChat;
import java.util.*;

import Model.Message;
import Model.User;

public class DataBase {
	private static TreeMap<String, User> ps; // login->password
	private static String comments;
	public static boolean registered(String login) {
		return ps.containsKey(login);
	}
	public static User getUser(String login) {
		return ps.get(login);
	}
	private static void add(String login, String password) {
		ps.put(login, new User(login, password));
	}
	protected static void initialize() {
		comments = "";
		if (ps == null) 
			ps = new TreeMap<String, User>();
	}
	protected static int process(String login, String password) {
		if (registered(login)) return 0;		
		add(login, password);
		return 1;
	}
	protected static String getComments() { return comments; }
	protected static void addComments(Message msg) { comments += msg.getContent();	} 
}

