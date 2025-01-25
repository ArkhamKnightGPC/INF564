package mini_turtle;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Turtle extends JFrame implements KeyListener {

	private static final long serialVersionUID = 1L;

	private boolean draw = true;
	private double tx = 400, ty = 400;
	private Angle angle = new Angle(0);

	private BufferedImage image;
    private Graphics2D g;

	Turtle() {
        this.image = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
        this.g = image.createGraphics();
        g.setColor(java.awt.Color.BLACK);
        BasicStroke stroke = new BasicStroke(2);
        g.setStroke(stroke);
        ImageIcon icon = new ImageIcon(image);
        JLabel draw = new JLabel(icon);
        this.setContentPane(draw);
		this.setTitle("Mini Turtle");
		this.setSize(800, 800);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
	    this.addKeyListener(this);
        this.pack();
        this.requestFocusInWindow();
		this.setVisible(true);
	}

	void pendown() { this.draw = true; }
	void penup() { this.draw = false; }

	void turnleft(double deg) { this.angle.add(new Angle(deg)); }
	void turnright(double deg) { turnleft(-deg); }

	void forward(int len) {
		double nx = tx + len * angle.cos();
		double ny = ty - len * angle.sin();
		if (this.draw) { g.drawLine((int)tx, (int)ty, (int)nx, (int)ny); this.repaint(); }
		tx = nx;
		ty = ny;
	}
	
	void setColor(Color c) {
		switch (c) {
		case Black: g.setColor(java.awt.Color.BLACK); break;
		case White: g.setColor(java.awt.Color.WHITE); break;
		case Green: g.setColor(java.awt.Color.GREEN); break;
		case Blue : g.setColor(java.awt.Color.BLUE ); break;
		case Red  : g.setColor(java.awt.Color.RED  ); break;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	@Override
	public void keyReleased(KeyEvent e) {}

}

// angles en degrés
class Angle {
	static final double PI_OVER_180 = Math.atan(1) / 45;
	private double rad;

	Angle(double deg) {
		this.rad = deg * PI_OVER_180;
	}

	void add(Angle a) {
		this.rad += a.rad;
	}

	double cos() {
		return Math.cos(this.rad);
	}

	double sin() {
		return Math.sin(this.rad);
	}
}
