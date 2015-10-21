import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.io.*;

public class serverApp implements Runnable {
	private Map<Integer, ChatServerThread> testClients = new HashMap<>();
	
	private ServerSocket server = null;
	private Thread thread = null;
	Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
	Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);

	public serverApp(int port) {
		try {
			System.out.println("Binding to port " + port + ", please wait  ...");
			server = new ServerSocket(port);
			System.out.println("Server started: " + server);
			start();
		} catch (IOException ioe) {
			System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
		}
	}

	public void run() {
		while (thread != null) {
			try {
				System.out.println("Waiting for a client ...");
				addThread(server.accept());
			} catch (IOException ioe) {
				System.out.println("Server accept error: " + ioe);
				stop();
			}
		}
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public void stop() {
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
	}

	
	private int findClient(int ID) {
		return ID;
	}
	
	
	private String getAllNicknames() { // new method
		StringBuilder nicknames = new StringBuilder();
		for (Entry<Integer, ChatServerThread> entry: testClients.entrySet()) {
			nicknames.append(entry.getValue().getNickName() + "\n");
		}
		return nicknames.toString();
	}
	

	
	public synchronized void handle(int ID, String input) {

		if (".bye".equals(input)) {
			System.out.println("Got BYE message, sending BYE in answer...");
			// Sending the list of users when someone leaves
			testClients.get(findClient(ID)).send(".bye");
			System.out.println("BYE answer is sent to: " + ID);
			System.out.println(testClients);
			remove(ID);
			for (Entry<Integer, ChatServerThread> entry: testClients.entrySet()) {
				entry.getValue().send(".userlist" + getAllNicknames());
			}
		} else if (input.startsWith(".nickname")) {
			testClients.get(findClient(ID)).setNickName(input.toString().substring(9, input.length()));
			// Sending the list of users when someone links
			for (Entry<Integer, ChatServerThread> entry: testClients.entrySet()) {
				entry.getValue().send(".userlist" + getAllNicknames());
			}
		} else
			for (Entry<Integer, ChatServerThread> entry: testClients.entrySet()) {
				entry.getValue().send(testClients.get(findClient(ID)).getNickName() + ": " + input);
			}
	}

	
	public synchronized void remove(int ID) {
			try{
				testClients.get(findClient(ID)).close();
				testClients.remove(findClient(ID));
			} catch (IOException ioe) {
				System.out.println("Error closing thread: " + ioe);
			}
	}
	

	
	private void addThread(Socket socket) {
		System.out.println("Client accepted: " + socket);
		System.out.println(threadArray.length);
		testClients.put(socket.getPort(), new ChatServerThread(this, socket));
		try {
			testClients.get(socket.getPort()).open();
			testClients.get(socket.getPort()).start();
		} catch (IOException ioe) {
			System.out.println("Error opening thread: " + ioe);
		}
	}
	
	public static void main(String args[]) {
		serverApp test = new serverApp(3000);
	}
}