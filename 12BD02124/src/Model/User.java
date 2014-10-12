package Model;

public class User {
	private String login, password;
	public User(String login, String password) {
		this.login = login;
		this.password = password;
	}
	public String getLogin() { 
		return login; 
	}
	public boolean isMatch(String password) {
		return password.equals(this.password);
	}
}
