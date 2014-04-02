import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Random;
public class Board {
/*
 * NOTE TO SELF
 * 
 * LOL
 * 
 */
	//game stats
	int outbreaks = 0;
	static int infectionRateCounter = 0;
	static int [] infectionRate = {2,2,2,3,3,4,4}; 
	ArrayList<String> infectionDeck;
	ArrayList<String> infectionDiscardDeck;
	ArrayList<String> playerDeck;
	
	static String [] colors = {"BLUE", "YELLOW", "BLACK", "RED"};
	static boolean [] cured = {false, false, false, false};
	static ArrayList<Country> countries = new ArrayList<Country>();
	static ArrayList<Player> players = new ArrayList<Player>();
	Server server;
	int actions;
	Random shuffler;
	
	public Board() {
		//place-holder constructor
	}

	public Board(Server s) {
		server = s;
		actions = 5;
	}
	//read in all countries from txt file and add to arraylist
	public void initCountries() {
		
		try {
			Scanner in = new Scanner(new File("countries.txt"));
			String lineToParse = "";
			
			while(in.hasNextLine()) {
				Country tmp = new Country();
				lineToParse = in.nextLine();
				if(lineToParse.trim()!="") {
					tmp.initValues(lineToParse);
					countries.add(tmp);
				}
			}
		} catch (IOException e) {
			System.out.println("FILE IO ERROR");
		}	
		infectionDeck = new ArrayList<String>();
		playerDeck = new ArrayList<String>();
		infectionDiscardDeck = new ArrayList<String>();
		for(Country c : countries) {
			infectionDeck.add(c.name);
			playerDeck.add(c.name);
		}
		Collections.shuffle(infectionDeck);
		Collections.shuffle(playerDeck);

		int quarter = playerDeck.size()/4;
		ArrayList<String> tmp1 = new ArrayList<String>();
		tmp1.add("EPIDEMIC");
		ArrayList<String> tmp2 = new ArrayList<String>();
		tmp2.add("EPIDEMIC");
		ArrayList<String> tmp3 = new ArrayList<String>();
		tmp3.add("EPIDEMIC");
		for(int i=0;i<quarter;i++) {
			String card = playerDeck.get(0);
			tmp1.add(card);
			playerDeck.remove(0);
			card = playerDeck.get(0);
			tmp2.add(card);
			playerDeck.remove(0);
			card = playerDeck.get(0);
			tmp3.add(card);
			playerDeck.remove(0);
		}
		playerDeck.add("EPIDEMIC");
		Collections.shuffle(playerDeck);
		Collections.shuffle(tmp1);
		Collections.shuffle(tmp2);
		Collections.shuffle(tmp3);
		playerDeck.addAll(0, tmp1);
		playerDeck.addAll(0, tmp2);
		playerDeck.addAll(0, tmp3);
	}

	public void initInfections() {
		for(int i=3;i>0;i--) {
			for(int r=0;r<3;r++) {
				String tmp = drawCard();
				for(int y=0;y<i;y++) {
					server.addCube(tmp, findCountry(tmp).color);
				}
			}
		}	
	}
	
	public int getInfectionRate() {
		return infectionRate[infectionRateCounter];
	}
	
	public void cure(String s) {
		int pos = 0;
		for(int i=0;i<colors.length;i++) {
			if(colors[i].equals(s)) {
				pos = i;
			}
		}
		
		cured[pos] = true;
		checkWin();
	}
	
	public void checkWin() {
		int count = 0;
		for(int i=0;i<cured.length;i++) {
			if(cured[i]==true) {
				count++;
			}
		}
		if(count>2) {
			server.broadcast("YOU WIN!!!");
			server.broadcastAction("", "WIN");
		}
	}
	
	public void lose() {
		server.broadcast("YOU LOSE!!!");
		server.broadcastAction("", "LOSE");
	}
	
	public void drawPlayerCard() {
		String card = playerDeck.get(0);
		//remove card from deck
		playerDeck.remove(0);
		if(card.equals("EPIDEMIC")) {
			epidemic();
		}
		else server.broadcastAction(card, "DRAW");
		server.broadcast("Player drew: " + card);
	}
	
	public void epidemic() {
		infectionRateCounter++;
		server.broadcastAction(infectionRateCounter+"", "INFECT");
		
		String card = infectionDeck.get(infectionDeck.size()-1);
		//put card in discard pile
		infectionDiscardDeck.add(card);
		//remove card from deck
		infectionDeck.remove(infectionDeck.size()-1);
		while(findCountry(card).getCubes(findCountry(card).color) < 3) {
			server.addCube(card, findCountry(card).color);
		}
		server.broadcast(card+" has has an epidemic!");
		
		//reshuffle the deck
		Collections.shuffle(infectionDiscardDeck);
		infectionDeck.addAll(0, infectionDiscardDeck);
	}
	
	//print all countries in the arraylist
	public void printCountries() {
		for(Country c : countries) {
			System.out.println(c.name +" --- "+c.color);
		}
	}
	
	public void decrementActions() {
		actions--;
		if(actions<1) {
			actions = 5;
			server.changeTurn();
		}
	}
	//create a player and add to arraylist
	public void addPlayer(String input) {
		players.add(new Player(input, countries.get(0)));
	}

	public void addPlayer(String input, String startingPos) {
		players.add(new Player(input, findCountry(startingPos)));
	}

	//returns player at position i
	public Player getPlayer(int i) {
		return players.get(i);
	}
	
	public void incrementOutbreaks() {
		outbreaks++;
		server.broadcastAction(outbreaks+"", "OUTBREAK");
		if(outbreaks>7) {
			lose();
		}
	}
	
	public int getOutbreaks() {
		return outbreaks;
	}
	
	public String drawCard() {
		String card = infectionDeck.get(0);
		//put card in discard pile
		infectionDiscardDeck.add(card);
		//remove card from deck
		infectionDeck.remove(0);
		return card;
	}
	
	//returns country with matching name
	public Country findCountry(String input) {
		Country output = new Country();
		for(int i=0;i<countries.size();i++) {
			if(countries.get(i).name.equals(input))
				output = countries.get(i);
		}
		return output;
	}
	
	public void addCube(String place, String color) {
		findCountry(place).addCube(color); //add cube to this country
	}
	
	public void removeCube(String place, String color) {
		findCountry(place).removeCube(color);
	}
}
