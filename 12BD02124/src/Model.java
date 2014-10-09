package EnjoyChat;
import java.util.*;

public class Model {
	public static TreeMap<String, String> ps; // login->password
	public static boolean registered(String login) {
		return ps.containsKey(login);
	}
	private static void add(String login, String password) {
		ps.put(login, password);
	}
	protected static boolean match(String login, String password) {
		if (!registered(login)) return false;		
		return (ps.get(login)).equals(password);
	}
	protected static void initialize() {
		if (ps == null) ps = new TreeMap<String, String>();
	} 
	protected static int process(String login, String password) {
		if (registered(login)) return 0;		
		add(login, password);
		return 1;
	}
}
