import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;


public class CountryBlock extends Block{

	private String name = "";
	public String color = "";
	public Color actualColor;
	
	String [] colors = {"BLUE", "YELLOW", "BLACK", "RED"};
	Integer [] cubes = {0,0,0,0};
	Color [] colorPicker = {Color.BLUE, Color.YELLOW, Color.BLACK, Color.RED};
	boolean researchLab = false;
	ArrayList<String> countries = new ArrayList<String>();
	ArrayList<CountryBlock> attatchedBlocks = new ArrayList<CountryBlock>();

    public CountryBlock() {
    	width=40;
    	height=40;
    }
    
	public CountryBlock(int x, int y) {
		width=40;
    	height=40;
		xPos = x;
		yPos = y;
	}

	public void addBlock(CountryBlock tmp) {
		attatchedBlocks.add(tmp);
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
	
	public ArrayList<CountryBlock> getAttachedBlocks() {
		return attatchedBlocks;
	}
	
	public void setColor(String input) {
		color = input;
	}
	
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
	
	//check if players move is possible from this place on map
	public boolean validMove(String input) {
		return countries.contains(input);
	}
	
	public void initValues(String input) {
		String [] values = input.split("-");
		setName(values[0]);
		setColor(values[1]);
		int i = 0;
		while(i<4 && !colors[i].equals(color.toUpperCase())) {
			i++;
		}
		actualColor = colorPicker[i];

		xPos=Integer.parseInt(values[2]);
		yPos=Integer.parseInt(values[3]);
		
		if(values.length>4) {
			for(i=4;i<values.length;i++) {
				countries.add(values[i]);
			}
		}
	}
    

    public void paintLine(Graphics g, int x0, int y0) {
    	int x1=xPos+width/2;
    	int x2=x0+getWidth()/2;
    	int y1=yPos+height/2;
    	int y2=y0+getHeight()/2;
    	g.drawLine(x1, y1, x2, y2);
    }
    
    public void drawCubes(Graphics g) {
    	int x = xPos;
    	int y = yPos;
    	for(int i = 0; i<cubes.length;i++) {
    		if(cubes[i]>0) {
		    	g.setColor(colorPicker[i]);
		    	g.fillRect(x,y,8,11);
		    	if(colorPicker[i]==Color.YELLOW)
		    		g.setColor(Color.BLACK);
		    	else g.setColor(Color.WHITE);
		        g.drawString(cubes[i]+"",x,y+10);
		        x=x+8;
    		}
    	}
    }
    public void paintSquare(Graphics g){
        g.setColor(actualColor);
        g.fillRect(xPos,yPos,width,height);
        g.setColor(Color.BLACK);
        g.drawRect(xPos,yPos,width,height); 
        g.drawString(name,xPos,yPos);
        drawCubes(g);
    }
}
