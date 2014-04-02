
public class Block {
	protected int xPos = 50;
    protected int yPos = 50;
    protected int width = 40;
    protected int height = 40;
    
    public boolean clicked(int x0, int y0) {
    	if(x0>xPos&&y0>yPos&& x0<xPos+width&&y0<yPos+height)
    		return true;
    	return false;
    }
}
