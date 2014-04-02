import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class IntroScreen extends JFrame implements ActionListener {
	
	private JButton start, tut;
	
	public IntroScreen() {
		setTitle("Epidemic");
		try {
			//setContentPane(new JLabel(new ImageIcon(ImageIO.read(new File("intro2.png")))));
			setContentPane(new JPanel() {
		        BufferedImage image = ImageIO.read(new File("intro2.png"));
		        public void paintComponent(Graphics g) {
		            super.paintComponent(g);
		            g.drawImage(image, 0, 0, 425, 500, this);
		        }
		    });
    	} catch (IOException e) {
		//catch
    	}
    	start = new JButton("Start");
    	tut = new JButton("Tutorial");
    	start.addActionListener(this);
    	tut.addActionListener(this);
		start.setEnabled(true);
		tut.setEnabled(true);
    	add(start);
    	add(tut);
    	
    	setDefaultCloseOperation(EXIT_ON_CLOSE);
    	setSize(425, 525);
    	setResizable(false);
		start.setVisible(true);
		
		setVisible(true);	
	}
	
	public static void main(String [] args) {
		IntroScreen is = new IntroScreen();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object object = e.getSource();
		if(object==start) {
			GUI g = new GUI("localhost", 1500);
		}
		else if(object==tut) {
			try {
		         //Set your page url in this string. For eg, I m using URL for Google Search engine
		         String url = "http://www.zmangames.com/boardgames/files/pandemic/Pandemic_Rules.pdf";
		         java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		       }
		       catch (java.io.IOException ex) {
		    	 System.out.println("LOLOLOLOL");
		       }
		}
	}
}
