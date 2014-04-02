import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class GUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private JLabel label; // will first hold "Username:", later on "Enter message"
	private JTextField tf; // to hold the Username and later on the messages
	private JTextField tfServer, tfPort; // to hold the server address an the port number
	private JButton login; // to Logout and get the list of the users
	private JTextArea textArea; 	// for the chat room
	private boolean connected;	// if it is for connection
	private Client client; // the Client object
	private int defaultPort; // the default port number
	private String defaultHost;
	private boolean boolMessage;
	JPanel northPanel;
	JPanel midPanel;
	JPanel serverAndPort;
	GamePanel gamePanel;

	GUI(String host, int port) {

		super("Epidemic");
		defaultPort = port;
		defaultHost = host;
		
		northPanel = new JPanel(new GridLayout(2,1));
		// the server name and the port number
		serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
		// the two JTextField with default value for server address and port number
		tfServer = new JTextField(host);
		tfPort = new JTextField("" + port);
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);
		
		label = new JLabel("Enter your username below", SwingConstants.CENTER);
		northPanel.add(label);
		
		serverAndPort.add(new JLabel("Server Address:  "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel(""));
		northPanel.add(serverAndPort);

		// the TextField
		tf = new JTextField("Anonymous");
		tf.setBackground(Color.WHITE);
		northPanel.add(tf);
		
		login = new JButton("Login");
		login.addActionListener(this);
		
		northPanel.add(login);
		
		add(northPanel, BorderLayout.NORTH);

		//The southPanel which is the chat room
		textArea = new JTextArea("Welcome to the Chat room\n", 9, 80);
		JPanel southPanel = new JPanel(new GridLayout(1,1));
		southPanel.add(new JScrollPane(textArea));
		textArea.setEditable(false);
		add(southPanel, BorderLayout.SOUTH);

		//remove gridlayout to keep original size
		midPanel = new JPanel(new GridLayout(1,1));
		//midPanel.setPreferredSize(new Dimension(100, 100));
	    midPanel.setBackground(Color.white);
	    gamePanel = new GamePanel();
	   // midPanel.add(gamePanel);
	
		add(midPanel, BorderLayout.CENTER);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setVisible(true);
		tf.requestFocus();

	}

	// called by the Client to append text in the TextArea 
	void append(String str) {
		textArea.append(str);
		textArea.setCaretPosition(textArea.getText().length() - 1);
	}
	
	void setBool(boolean input) {
		boolMessage = input;
	}
	
	//used when a connection attempt fails
	void connectionFailed() {
		login.setEnabled(true);
		label.setText("Enter your username below");
		tf.setText("Anonymous");
		// reprint the default values
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		// let the user change them
		tfServer.setEditable(false);
		tfPort.setEditable(false);
		tf.removeActionListener(this);
		connected = false;
	}
		
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		

		// ok it is coming from the JTextField
		if(connected) {
			// just have to send the message
			client.sendMessage(new ActionMessage(tf.getText()));				
			tf.setText("");
			return;
		}
		
		if(o == login) {
			String username = tf.getText().trim();
			if(username.length() < 1)
				return;
			
			String server = tfServer.getText().trim();
			if(server.length() < 1)
				return;
			
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() < 1)
				return;
			
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;  
			}

			// try creating a new Client with GUI
			client = new Client(server, port, username, this);
			// test if we can start the Client
			if(!client.start()) 
				return;
			tf.setText("");
			label.setText("Enter your message below");
			connected = true;
			
			// disable login button
			login.setEnabled(false);
			// enable the 2 buttons
			
			// disable the Server and Port JTextField
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			// Action listener for when the user enter a message
			tf.addActionListener(this);
		}
	}

	public void initGame() {
		midPanel.add(gamePanel);
		pack();
	}
	
	//create instance of GUI
	public static void main(String[] args) {
		new GUI("localhost", 1500);
	}
	

	class GamePanel extends JPanel {

		private static final long serialVersionUID = 1L;
		ArrayList<CountryBlock> blocks = new ArrayList<CountryBlock>(); //keep track of countries
		ArrayList<PlayerBlock> playerBlocks = new ArrayList<PlayerBlock>();  //keep track of player pieces
		ArrayList<Card> hand = new ArrayList<Card>();  //keep track of player pieces
		boolean pressed = false;
		PlayerBlock currentBlock;
		int infectionRateCounter = 0;
		int outbreakCounter;
		int [] infectionRate = {2,2,2,3,3,4,4}; 
		ArrayList<ButtonBlock> cures;
		boolean [] cured = {false, false, false, false};
		ArrayList<ButtonBlock> buttons;
		
		public GamePanel() {
			setBorder(BorderFactory.createLineBorder(Color.black));
			setBackground(new Color(100,150,255));
			//parse the txt file and create country blocks
			try {
				Scanner in = new Scanner(new File("countries.txt"));
				String lineToParse = "";
				
				while(in.hasNextLine()) {
					CountryBlock tmp = new CountryBlock();
					lineToParse = in.nextLine();
					if(lineToParse.trim()!="") {
						tmp.initValues(lineToParse);
						blocks.add(tmp);
					}
				}
			} catch (IOException e) {
				System.out.println("FILE IO ERROR");
			}
			for(CountryBlock cBlock : blocks) {
				for(String s : cBlock.countries) {
					cBlock.addBlock(findCountry(s));
				}
			}
			buttons = new ArrayList<ButtonBlock>();
			ButtonBlock b = new ButtonBlock(Color.BLUE, "DEC", "BLUE", 50, 50);
			buttons.add(b);
			b = new ButtonBlock(Color.YELLOW, "DEC", "YELLOW", 70, 50);
			buttons.add(b);
			b = new ButtonBlock(Color.BLACK, "DEC", "BLACK", 90, 50);
			buttons.add(b);
			b = new ButtonBlock(Color.RED, "DEC", "RED", 110, 50);
			buttons.add(b);

			cures = new ArrayList<ButtonBlock>();
			cures.add(new ButtonBlock(Color.BLUE, "CURE", "BLUE", 380, 450));
			cures.add(new ButtonBlock(Color.YELLOW, "CURE", "YELLOW", 420, 450));
			cures.add(new ButtonBlock(Color.BLACK, "CURE", "BLACK", 460, 450));
			cures.add(new ButtonBlock(Color.RED, "CURE", "RED", 500, 450));
			
			
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					int mouseX = e.getX();
					int mouseY = e.getY();
					if(client.isTurn && !client.gameOver) {
						for(ButtonBlock b : buttons) {
							if(b.clicked(mouseX, mouseY)) {
								client.sendMessage(new ActionMessage(playerBlocks.get(client.id).getCountry().getName()+":"+b.parameters, "DEC"));
							}
						}
						
						for(int y=0;y<cures.size();y++) {
							if(cures.get(y).clicked(mouseX, mouseY) && cured[y]==false) {
								int count = 0;
								for(Card c : hand) {
									if(c.color.toUpperCase().equals(cures.get(y).parameters))
										count++;
								}
								if(count>=4) {
									count = 0;
									for(int i=hand.size()-1;i>=0&&count<4;i--) {
										if(hand.get(i).color.toUpperCase().equals(cures.get(y).parameters)) {
											count++;
											hand.remove(i);
											client.sendMessage(new ActionMessage(cures.get(y).parameters, "CURE"));
										}
									}
									cures.get(y).color = Color.GRAY;
									cured[y]=true;
									//send a message to the server
								}
							}
						}
						
						if(playerBlocks.get(client.id).clicked(mouseX,mouseY)) {
							currentBlock = playerBlocks.get(client.id);
							moveSquare(currentBlock, mouseX,mouseY);
							//addCube(c, "BLUE");
							pressed = true;
						}
					}
				}
			});
			
			addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent e) {
					if(!pressed)
						return; //if nothing is clicked dont do anything
					
					pressed = false;
					int mouseX = e.getX(); //get mouse x coordinate
					int mouseY = e.getY(); //get mouse y coordinate
					for(CountryBlock c : blocks) { //check if the mouse was release on a countryblock
						if(c.clicked(mouseX,mouseY) && currentBlock.getCountry().getAttachedBlocks().contains(c) && client.isTurn) {
							//check if the mouse was positioned on a country 
							//check that the new country is a valid position
							//check that it is currently the players turn
							client.sendMessage(new ActionMessage(c.getName(), "MOVE"));
							currentBlock.setCountry(c);
							currentBlock = null;
							return;
						}
					}
					currentBlock.setCountry(currentBlock.getCountry());
					currentBlock = null;
				}
			});
			
			addMouseMotionListener(new MouseAdapter() {
				public void mouseDragged(MouseEvent e) {
					if(pressed)
						moveSquare(currentBlock, e.getX(),e.getY());
				}
			});
		}

		public void updateCountry(CountryBlock c, PlayerBlock p) {
			p.setCountry(c);
		}

		public void updateCountry(String c, PlayerBlock p) {
			p.setCountry(findCountry(c));
		}
		
		public void updateCountry(String c, int playerInt) {
			playerBlocks.get(playerInt).setCountry(findCountry(c));
		}
		
		public void addCard(String name) {
			hand.add(new Card(name, findCountry(name).color));
		}
		
		public void addPlayerBlock() {
			playerBlocks.add(new PlayerBlock());
		}
		
		public void addPlayerBlock(int i) {
			PlayerBlock p = new PlayerBlock(i);
			playerBlocks.add(p);
			p.setCountry(blocks.get(0));
		}
		
		private void moveSquare(PlayerBlock c, int x, int y) {

			//block's x y height width values
	        final int CURR_X = c.getX();
	        final int CURR_Y = c.getY();
	        final int CURR_W = c.getWidth();
	        final int CURR_H = c.getHeight();
	        final int OFFSET = 1;

	        if ((CURR_X!=x) || (CURR_Y!=y)) {

	            // The square is moving, repaint background 
	            // over the old square location. 
	            repaint(CURR_X,CURR_Y,CURR_W+OFFSET,CURR_H+OFFSET);

	            // Update coordinates.
	            c.setX(x);
	            c.setY(y);
	            
	            // Repaint the square at the new location.
	            repaint(c.getX(), c.getY(), 
	                    c.getWidth()+OFFSET, 
	                    c.getHeight()+OFFSET);
	        }
		}
		    
		public Dimension getPreferredSize() {
			return new Dimension(1000,500);
		}

		public CountryBlock findCountry(String input) {
			CountryBlock output = new CountryBlock();
			for(int i=0;i<blocks.size();i++) {
				if(blocks.get(i).getName().equals(input))
					output = blocks.get(i);
			}
			return output;
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			repaint();
			for(CountryBlock c : blocks) {
				for(CountryBlock d : c.attatchedBlocks) {
					c.paintLine(g, d.getX(), d.getY());
				}
			}
			for(CountryBlock c : blocks) {
				c.paintSquare(g);
			}
			
			for(PlayerBlock p : playerBlocks) {
				p.paintSquare(g);
			}
			
			for(int i=0;i<infectionRate.length;i++) {
				if(i==infectionRateCounter)
					g.setColor(Color.WHITE);
				else g.setColor(Color.GREEN);
				g.fillRect(10+(8*i),9,8,12);
				g.setColor(Color.BLACK);
				g.drawString(infectionRate[i]+"",10+(8*i),18);
			}
			
			for(int i=0;i<9;i++) {
				if(i==outbreakCounter)
					g.setColor(Color.WHITE);
				else g.setColor(Color.GREEN);
				g.fillRect(10+(8*i),27,8,12);
				g.setColor(Color.BLACK);
				g.drawString(i+"",10+(8*i),36);
			}
			
			for(int i=0;i<hand.size();i++) {
				g.setColor(findCountry(hand.get(i).name).actualColor);
				g.fillRect(10,290+(12*i),8*hand.get(i).name.length(),12);
				if(findCountry(hand.get(i).name).actualColor==Color.BLACK)
					g.setColor(Color.WHITE);
				else g.setColor(Color.BLACK);
				g.drawString(hand.get(i).name, 10, 300+12*i);
			}
			
			for(ButtonBlock b : buttons) {
				b.paintSquare(g);
			}	
			
			for(ButtonBlock b : cures) {
				b.paintSquare(g);
			}	
			
			if(client.gameOver) {
				g.setFont(new Font("GAME OVER", Font.BOLD, 36));
				if(outbreakCounter>7)
					g.drawString("YOU LOSE!", 100,100);
				else g.drawString("YOU WIN!", 100,100);
			}
		}  
	}
}