import java.net.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.*;

public class Client {

	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;
	private String server, username;
	private int port;
	private GUI gui;
	boolean isTurn;
	boolean gameOver;
	int id;

	Client(String server, int port, String username, GUI tempGUI) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.gui = tempGUI;
		isTurn = false;
		gameOver = false;
	}
	
	//gui methods	
	public void actionPerformed(ActionEvent evt) {
	
	}
	
	public boolean start() {
		try {
			display("Connecting to server on port: "+port);
			socket = new Socket(server, port);
		} 
		catch(Exception ec) {
			display("Error connecting to server:" + ec);
			return false;
		}
		
		display("Connection established " + socket.getInetAddress() + ":" + socket.getPort());
		try {
			sInput = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// creates the Thread to listen from the server 
		new ListenFromServer().start();
		// Send our username to the server 
		try {
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		return true;
	}

	/*
	 * To send a message to the console 
	 */

	private void display(String msg) {
		gui.append(msg.trim()+"\n");      // println in console mode
	}
	
	/*
	 * To send a message to the server
	 */
	void sendMessage(ActionMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} // not much else I can do
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} // not much else I can do
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} // not much else I can do
	}
	
	public static void main(String[] args) {
	
	}

	//a class that waits for the message and simply System.out.println() it in console mode
	class ListenFromServer extends Thread {
		public void run() {
			while(true) {
				try {
					ActionMessage actionMsg = (ActionMessage) sInput.readObject();
					String type = actionMsg.getType();
					// print the message and add back the prompt
					if(type.equals("CHAT")) {
						display(actionMsg.getMessage());
					}
					else if(type.equals("TRUE")) {
						gui.setBool(true);
					}
					else if(type.equals("FALSE")) {
						gui.setBool(false);
					}
					else if(type.equals("ADD")) {
						String[] words = actionMsg.getMessage().split(":");
						gui.gamePanel.findCountry(words[0]).addCube(words[1]);
					}
					else if(type.equals("DEC")) {
						String[] words = actionMsg.getMessage().split(":");
						gui.gamePanel.findCountry(words[0]).removeCube(words[1]);
					}
					else if(type.equals("PLAYER")) {
						gui.gamePanel.addPlayerBlock(Integer.parseInt(actionMsg.getMessage()));
					}
					else if(type.equals("START")) {
						gui.initGame();
					}
					else if(type.equals("MOVE")) {
						//update gamepanel thing 
						String[] words = actionMsg.getMessage().split(":");
						gui.gamePanel.updateCountry(words[0], Integer.parseInt(words[1]));
					}
					else if(type.equals("INFECT")) {
						gui.gamePanel.infectionRateCounter = Integer.parseInt(actionMsg.getMessage());
					}
					else if(type.equals("DRAW")) {
						if(isTurn)
							gui.gamePanel.addCard(actionMsg.getMessage());
					}
					else if(type.equals("OUTBREAK")) {
						gui.gamePanel.outbreakCounter = Integer.parseInt(actionMsg.getMessage());
					}
					else if(type.equals("TURN")) {
						String[] words = actionMsg.getMessage().split(":");
						if(words[0].equals("true")) {
							isTurn=true;
							id=Integer.parseInt(words[1]);
						}
						else isTurn=false;
					}
					else if(type.equals("CURE")) {	
						for(int i=0;i<gui.gamePanel.cures.size();i++) {
							if(gui.gamePanel.cures.get(i).parameters.equals(actionMsg.getMessage().toUpperCase())) {
								gui.gamePanel.cured[i]=true;
								gui.gamePanel.cures.get(i).color=Color.GRAY;
							}
						}
					}
					else if(type.equals("WIN")) {	
						isTurn=false;
						gameOver=true;
					}
					else if(type.equals("LOSE")) {	
						isTurn=false;
						gameOver=true;
					}
				}
				catch(IOException e) {
					display("Server has close the connection: " + e);
					break;
				}
				// can't happen with a String object but need the catch anyhow
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}