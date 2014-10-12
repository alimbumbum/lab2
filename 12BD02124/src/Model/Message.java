package Model;

import java.net.URLDecoder;

public class Message {
	private String content; 
	public Message(String login, String msg) {
		if (msg == null) 
			content = "";
		else 
			content = login + ": " + URLDecoder.decode(msg.substring(5)) + "\n";
	}
	public String getContent() {
		return content;
	}
}
