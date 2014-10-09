package EnjoyChat;

import EnjoyChat.Model;
import java.io.*;
import java.util.*;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;



public class HttpMain {
	private static boolean loggedIn = false;
	private static String comments = "", name = "";
	public static void main(String[] args) throws Exception {
		Model.initialize();
		Model.process("admin", "1");
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.createContext("/enter", new EnterHandler());
		server.createContext("/chat", new ChatHandler());
		server.createContext("/register", new RegisterHandler());
		server.setExecutor(null); // creates a default executor
		server.start();
	}
	private static String readFromFile(String from) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(from));
		String s, res = "";
		while (true) {
			s = br.readLine();
			if (s == null) 
				break;
			res += s + "\n";
		}
		br.close();
		return res;
	}

	private static int toDec(char ch) {
		if ('0' <= ch && ch <= '9') return ch - '0';
		return 10 + (ch - 'A');
	}
	private static String convert(String s) {
		String t = "";
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch == '+') t += ' ';
			else if (ch == '%') {
				System.out.println(toDec(s.charAt(i+1)) + " " + toDec(s.charAt(i+2)));
				t += (char)(16 * toDec(s.charAt(i+1)) + toDec(s.charAt(i+2)));
				i += 2;
			} else t += ch;
		}
		return t;
	}
	
	static class EnterHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			loggedIn = false;
			String response = "", template, login, password;
			template = readFromFile("temp");
			
			InputStream is = t.getRequestBody();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String vars = br.readLine();
			br.close();
			if (vars == null) {
				response = template.replace("#content#", readFromFile("enter"));
				t.getResponseHeaders().add("Content-type", "text/html; charset=UTF-8");
				t.sendResponseHeaders(200, response.getBytes().length);
			} else {
				try {
					StringTokenizer st = new StringTokenizer(vars, "=&");
					st.nextToken(); login = st.nextToken();
					st.nextToken(); password = st.nextToken();
					
					if (Model.match(login, password)) {
						response = template.replace("#content#", readFromFile("chat"));
						t.getResponseHeaders().add("Location", "/chat");
						t.sendResponseHeaders(301, response.getBytes().length);						
						name = login;
						loggedIn = true;						
					}
					else throw new Exception("Login or password doesn't match");
				} catch (Exception e){
					String page = readFromFile("message").replace("#message#", "Invalid login or password.");					
					response = template.replace("#content#", page);
					t.getResponseHeaders().add("Content-type", "text/html; charset=UTF-8");
					t.sendResponseHeaders(200, response.getBytes().length);
				}
			}			
				
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
		
	static class RegisterHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			loggedIn = false;
			String login, password, confpass, response = "";
			
			BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
			String vars = br.readLine();
			br.close();
			
			if (vars == null) {
				response = readFromFile("temp").replace("#content#", readFromFile("register"));
			} else {
				int x = -1;
				try {
					StringTokenizer st = new StringTokenizer(vars, "=&");
					st.nextToken();    login = st.nextToken();
					st.nextToken(); password = st.nextToken();
					st.nextToken(); confpass = st.nextToken();
					if (!password.equals(confpass)) x = -2; 
					else x = Model.process(login, password);					
				} catch (Exception e) { 
					x = -1;
					e.printStackTrace();
				}
				
				String msg = ( x == -2 ? "Password mismatches!" : x == -1 ? "Fields was filled incorrectly, try again." : 
						x == 0 ? "User is already exits, try another login." : "You have been successfully registered!");
				String page  = readFromFile("message").replace("#message#", msg); 
				
				response = readFromFile("temp").replace("#content#", page);
			}
			t.getResponseHeaders().add("Content-type", "text/html; charset=UTF-8");
			t.sendResponseHeaders(200, response.getBytes().length);
			
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	
	static class ChatHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			if (!loggedIn) {
				String page =  readFromFile("message").replace("#message#", "Access denied! Please, log in.");
				String response = readFromFile("temp").replace("#content#", page);
				t.sendResponseHeaders(200, response.getBytes().length);
				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.close();
			}
			String response, template = readFromFile("temp").replace("#content#", readFromFile("chat"));
			BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
			String vars = br.readLine();
			br.close();
			if (vars != null) { 
				String msg = convert(vars.substring(5));//.replaceAll("+", " ");
				comments += name + ": " + msg + "\n";
			} 			
			response = template.replace("#comments#", comments);
			
			t.getResponseHeaders().add("Content-type", "text/html; charset=UTF-8");
			t.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
}
