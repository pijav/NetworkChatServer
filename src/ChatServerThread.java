import java.net.*;
import java.io.*;

public class ChatServerThread extends Thread {
	private serverApp server = null;
	private Socket socket = null;
	private int ID = -1;
	private DataInputStream streamIn = null;
	private DataOutputStream streamOut = null;
	private volatile boolean stopMe = false;


	public ChatServerThread(serverApp _server, Socket _socket) {
		super();
		server = _server;
		socket = _socket;
		ID = socket.getPort();
	}

	public void send(String msg) {
		try {
			streamOut.writeUTF(msg);
			streamOut.flush();
		} catch (IOException ioe) {
			System.out.println(ID + " ERROR sending: " + ioe.getMessage());
			server.remove(ID);
			interrupt();
		}
	}

	public int getID() {
		return ID;
	}

	public void stopMe() {
		stopMe = true;
	}
	
	public void run() {
		System.out.println("Server Thread " + ID + " running.");
		while (!stopMe) {
			try {
				server.handle(ID, streamIn.readUTF());
			} catch (IOException ioe) {
				System.out.println(ID + " ERROR reading: " + ioe.getMessage());
				server.remove(ID);
				stopMe();
			}
		}
	}

	public void open() throws IOException {
		streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	}
	

	public void close() throws IOException {
		if (socket != null)
			socket.close();
			System.out.println("Socket closed");
		if (streamIn != null)
			streamIn.close();
			System.out.println("Input stream closed");
		if (streamOut != null)
			streamOut.close();
			System.out.println("Output stream closed");
		stopMe();		
	}
}