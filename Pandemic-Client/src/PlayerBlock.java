import java.awt.Color;
import java.awt.Graphics;

public class PlayerBlock extends Block {
	private int playerID;
	private String name = "";
	private String color = "";
	private Color actualColor;
	private CountryBlock position;
	
	Color [] colors = {Color.PINK, Color.GREEN, Color.WHITE, Color.RED};
	
    public PlayerBlock() {

        width = 10;
        height = 30;
    	xPos = 300;
    	yPos = 300;
    	actualColor = Color.PINK;
    }
    
    public PlayerBlock(int i) {
    	width = 10;
        height = 30;
    	xPos = 300;
    	yPos = 300;
    	actualColor = colors[i];
    	playerID = i;
    }
    
	public PlayerBlock(int x, int y) {
		xPos = x;
		yPos = y;
	}
	
    public void setX(int xPos){ 
        this.xPos = xPos;
    }

    public int getX(){
        return xPos;
    }
    public void setY(int yPos){
        this.yPos = yPos;
    }

    public int getY(){
        return yPos;
    }

    public int getWidth(){
        return width;
    } 

    public int getHeight(){
        return height;
    }
    
	public void setName(String input) {
		name = input;
	}
	
	public String getName() {
		return name;
	}
	
	public void setColor(String input) {
		color = input;
	}
	
	public void setCountry(CountryBlock input) {
		position = input;
		xPos=input.getX()+(width*playerID);
		yPos=input.getY()+10;
	}
	
	public CountryBlock getCountry() {
		return position;
	}
	
	public void paintSquare(Graphics g){
		g.setColor(actualColor);
		g.fillRect(xPos,yPos,width,height);
		g.setColor(Color.BLACK);
		g.drawRect(xPos,yPos,width,height); 
	}
}
