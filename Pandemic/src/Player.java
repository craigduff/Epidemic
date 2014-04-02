
public class Player {
	String playerName = "";
	Country position;
	
	public Player() {
		//default constructor
	}
	
	public Player(String name, Country input) {
		playerName = name;
		position = input;
	}
	
	public void setPosition(Country input) {
		position = input;
	}
	
	public void move(Country input) {
		if(position.validMove(input.name)) {
			position = input;
		}
	}
	
	public void printPosition() {
		System.out.println(position.name);
	}
}
