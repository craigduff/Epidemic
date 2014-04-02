import java.awt.Color;
import java.awt.Graphics;


public class ButtonBlock extends Block {
	Color color;
	String type;
	String parameters;
	
	public ButtonBlock() {
		//default
	}
	
	public ButtonBlock(Color c, String type0, String msg, int x, int y) {
		color = c;
		type = type0;
		yPos = y;
		xPos = x;
		height = 20;
		width = 20;
		parameters = msg;
	}
	
	public void paintSquare(Graphics g){
        g.setColor(color);
        g.fillRect(xPos,yPos,width,height);
        g.setColor(Color.BLACK);
        g.drawRect(xPos,yPos,width,height); 
    }
}
