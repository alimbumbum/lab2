package EnjoyChat;

import Model.*;
import EnjoyChat.DataBase;
import java.io.*;
import java.util.*;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HttpMain {
	private static boolean loggedIn = false;
	private static User user;
	public static void main(String[] args) throws Exception {
		DataBase.initialize();
		DataBase.process("admin", "1");
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
					user = DataBase.getUser(login);
					if (user.isMatch(password)) {
						response = template.replace("#content#", readFromFile("chat"));
						t.getResponseHeaders().add("Location", "/chat");
						t.sendResponseHeaders(301, response.getBytes().length);						
						loggedIn = true;
					}
					else 
						throw new Exception("Login or password doesn't match");
				} catch (Exception e) {
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
					else x = DataBase.process(login, password);					
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
			Message msg = new Message(user.getLogin(), br.readLine());
			System.out.println(msg.getContent());
			br.close();
			DataBase.addComments(msg);
			
			response = template.replace("#comments#", DataBase.getComments());
			t.getResponseHeaders().add("Content-type", "text/html; charset=UTF-8");
			t.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
}
