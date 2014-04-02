import java.util.ArrayList;


public class Country {
	
	public String name = "";
	public String color = "";
	String [] colors = {"BLUE", "YELLOW", "BLACK", "RED"};
	Integer [] cubes = {0,0,0,0};
	boolean researchLab = false;
	ArrayList<String> countries = new ArrayList<String>();
	
	public Country() {
		// TODO Auto-generated constructor stub
	}
	
	public ArrayList<String> getAdjacentCountries() {
		return countries;
	}
	
	public void setName(String input) {
		name = input;
	}
	
	public void setColor(String input) {
		color = input;
	}
	
	//check if players move is possible from this place on map
	public boolean validMove(String input) {
		return countries.contains(input);
	}
	
	//add a cube
	public void addCube(String colorInput) {
		for(int i=0;i<colors.length;i++) {
			if(colors[i].equals(colorInput.toUpperCase())) {
				cubes[i]++;
			}
		}
	}
	
	public void removeCube(String colorInput) {
		for(int i=0;i<colors.length;i++) {
			if(colors[i].equals(colorInput.toUpperCase()) && cubes[i]>0) {
				cubes[i]--;
			}
		}
	}
	
	public int getCubes(String colorInput) {
		for(int i=0;i<colors.length;i++) {
			if(colors[i].equals(colorInput.toUpperCase())) {
				return cubes[i];
			}
		}
		return 0;
	}
	
	public void initValues(String input) {
		String [] values = input.split("-");
		setName(values[0]);
		setColor(values[1]);
		if(values.length>4) {
			for(int i=4;i<values.length;i++) {
				countries.add(values[i]);
			}
		}
	}
}
