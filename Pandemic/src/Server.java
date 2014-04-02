import java.io.*;
import java.net.*;
import java.util.*;

/*
 * The server that can be run both as a console application or a GUI
 */
public class Server {
	// a unique ID for each connection
	private static int uniqueId;
	private ArrayList<ClientThread> users;
	private int port;
	private boolean keepGoing;
	private int turn = 0;
	private Board board;
	private boolean gameStarted;
	
	
	public Server(int port) {
		this.port = port;
		users = new ArrayList<ClientThread>();
		gameStarted = false;
	}
	
	public void start() {
		keepGoing = true;
		try 
		{
			ServerSocket serverSocket = new ServerSocket(port); // create socket used by server
			board = new Board(this); // create instance of board only 1 needed
			board.initCountries(); // read the file and add countries to the board
			
			while(keepGoing) // infinite loop to wait for connections
			{	
				display("Server waiting for Clients on port " + port + ".");
				Socket socket = serverSocket.accept();  //wait for a client to connect	
				if(users.size()<3) {
					// if I was asked to stop
					if(!keepGoing)
						break;
					ClientThread tmp = new ClientThread(socket);  // create thread for that user
					users.add(tmp);	//add the user to the array
					tmp.start(); //start the thread
					board.addPlayer("player"+users.size());
				}
			}
			//if receive message to stop
			try {
				serverSocket.close();
				for(int i = 0; i < users.size(); ++i) {
					ClientThread tc = users.get(i);
					try {
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
						System.out.println(ioE);
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		catch (IOException e) {
            String msg = "Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}		
	
	//Display an event (not a message) to the console or the GUI
	private void display(String msg) {
		System.out.println(msg);
	}
	
	public void createPlayers() {
		for(int i=0;i<users.size();i++) {
			broadcastAction(""+i, "PLAYER"); //tell other clients that user has arrived
		}
	}
	
	// to broadcast a message to all Clients
	public synchronized void broadcast(String message) {
	
		String messageLf = message + "\n";
		// display message on console or GUI
		System.out.print(messageLf);
		
		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for(int i = users.size(); --i >= 0;) {
			ClientThread ct = users.get(i);
			// try to write to the Client if it fails remove it from the list
			if(!ct.writeMsg(messageLf, "CHAT")) {
				users.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}
	
	// to broadcast an action to all Clients
	public synchronized void broadcastAction(String message, String action) {
		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for(int i = users.size(); --i >= 0;) {
			ClientThread ct = users.get(i);
			// try to write to the Client if it fails remove it from the list
			if(!ct.writeMsg(message, action)) {
				users.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}

	public void changeTurn() {
		endOfTurnRoutine();
		turn = (turn+1)%users.size();
		broadcastAction("false", "TURN");
		ClientThread ct = users.get(turn);
		ct.writeMsg("true:"+turn, "TURN");
	}
	
	public void endOfTurnRoutine() {
		//draw cards for player
		board.drawPlayerCard();
		board.drawPlayerCard();
		//infect random places
		for(int r=0;r<board.getInfectionRate();r++) {
			String tmp = board.drawCard();
			if(board.findCountry(tmp).getCubes(board.findCountry(tmp).color)<3) {
				addCube(tmp, board.findCountry(tmp).color);
			}
			else {
				board.incrementOutbreaks();
				ArrayList<String> tmpCountries = board.findCountry(tmp).getAdjacentCountries();
				ArrayList<String> outbreakList = new ArrayList<String>();
				outbreakList.add(tmp);
				spread(tmpCountries, outbreakList, board.findCountry(tmp).color);
			}
		}
	}		
	
	// for a client who logoff using the LOGOUT message
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for(int i = 0; i < users.size(); ++i) {
			ClientThread ct = users.get(i);
			// found it
			if(ct.id == id) {
				users.remove(i);
				return;
			}
		}
	}
	
	public static void main(String[] args) {
		// use port 1500
		int portNumber = 1500;
		// create a server object and start it
		Server server = new Server(portNumber);
		server.start();
	}
	
	public void addCube(String place, String color) {
		board.addCube(place, color);
		broadcastAction(place+":"+color, "ADD");
	}
	
	public void removeCube(String place, String color) {
		board.removeCube(place, color);
		broadcastAction(place+":"+color, "DEC");
	}
	
	public void spread(ArrayList<String> adjacent, ArrayList<String> outbreakList, String color) {
		for(String s : adjacent) {
			if(board.findCountry(s).getCubes(color)<3)
				addCube(s, color);
			else if(!outbreakList.contains(s)) {
				broadcast("Spread in "+s);
				board.incrementOutbreaks();
				outbreakList.add(s);
				ArrayList<String> tmpCountries = board.findCountry(s).getAdjacentCountries();
				spread(tmpCountries, outbreakList, color);
			}
		}
	}

	/** One instance of this thread will run for each client */
	class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client
		String username;
		// the only type of message a will receive
		ActionMessage cm;

		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			System.out.println("Setting up connection");
			try
			{
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				display(username + " just connected.");
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
				System.out.println(e);
			}
		}

		// what will run forever
		public void run() {
			// to loop until LOGOUT
			boolean keepGoing = true;
			while(keepGoing) {
				// read a String (which is an object)
				try {
					cm = (ActionMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// the message part of the ActionMessage
				String message = cm.getMessage();
				
				//send that
				String type = getMessageType(cm);
 				if(type.equals("CHAT")) {
 					if(message.equals("!start") && !gameStarted) {
 						broadcastAction("", "START");
 						gameStarted=true;
 						createPlayers();
 						board.initInfections();
 						ClientThread ct = users.get(0);
 						ct.writeMsg("true:"+0, "TURN");
 					}
 					else
 						broadcast(username + ": " + message);
				}
 				else if(type.equals("DEC") && (users.get(turn).username).equals(username)) {
 					String[] words = message.split(":");
 					board.decrementActions();
 					if(board.findCountry(words[0]).getCubes(words[1])>0) {
	 					removeCube(words[0], words[1]);
	 				}
				}
 				else if(type.equals("ADD") && (users.get(turn).username).equals(username)) {
 					String[] words = message.split(":");
 					board.decrementActions();
 					if(board.findCountry(words[0]).getCubes(words[1])<3) {
	 					addCube(words[0], words[1]);
	 				}
 					else {
 						board.incrementOutbreaks();
 						broadcast("Spread in "+words[0]);
 						ArrayList<String> tmpCountries = board.findCountry(words[0]).getAdjacentCountries();
 						ArrayList<String> outbreakList = new ArrayList<String>();
 						outbreakList.add(words[0]);
 						spread(tmpCountries, outbreakList, words[1]);
 					}
				}
				else if(type.equals("MOVE") && (users.get(turn).username).equals(username)) {				
					board.getPlayer(turn).move(board.findCountry(message));
					broadcastAction(message+":"+turn, "MOVE");
					System.out.print(board.getPlayer(turn)+" MOVED TO ");
					board.getPlayer(turn).printPosition();
					board.decrementActions();
				}
				else if(type.equals("CURE") && (users.get(turn).username).equals(username)) {				
					board.cure(message);
					broadcastAction(message, "CURE");
					broadcast(message+" has been cured!");
					board.decrementActions();
				}
			}
			// remove from the client list
			remove(id);
			close();
		}
		
		//if country has outbreak spread to other countries recursively
		
		/*
		public void addCube(String place, String color) {
			board.addCube(place, color);
			broadcastAction(place+":"+color, "ADD");
		}
		
		*/
		
		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if(sOutput != null) 
					sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}
		
		//Write a String to the Client output stream
		private boolean writeMsg(String msg, String action) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(new ActionMessage(msg, action));
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
		
		private String getMessageType(ActionMessage input) {
			return input.getType();	
		}
	}
}