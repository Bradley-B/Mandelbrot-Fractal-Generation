import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.security.auth.x500.X500Principal;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.xml.bind.ValidationEvent;

public class Canvas {

	JFrame canvas = new JFrame("Mandelbrot");
	CanvasPanel canvasPanel = new CanvasPanel();
	JPanel buttonPanel = new JPanel();
	JButton resetButton = new JButton("reset");

	JTextField xInputField = new JTextField(10);
	JTextField yInputField = new JTextField(10);
	JTextField radiusInputField = new JTextField(10); 
	JTextField resolutionInputField = new JTextField(4);
	JButton submitButton = new JButton("go to"); 

	double radius;
	double x; 
	double y;
	int resolution = 1000;		

	double xMin = x-radius;
	double xMax = x+radius;
	double yMin = y-radius;
	double yMax = y+radius;
	int curScreenMinWidth = 0;
	int curScreenMaxWidth;
	int curScreenMinHeight = 0;
	int curScreenMaxHeight;

	public static void main(String[] args) {
		new Canvas();
	}

	public Canvas() {
		startGUI();
		reset();
	}

	private void startGUI() {
		canvas.setSize(1000, 800);
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setLocationRelativeTo(null);
		canvas.setLayout(new BorderLayout());
		canvas.setUndecorated(true);
		//canvas.setOpacity(0.4f);
		canvas.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
		canvasPanel.addKeyListener(new KeyPressed());

		buttonPanel.add(xInputField);
		buttonPanel.add(yInputField);
		buttonPanel.add(radiusInputField);
		buttonPanel.add(resolutionInputField);
		buttonPanel.add(submitButton);
		buttonPanel.add(resetButton);
		buttonPanel.setBackground(Color.decode("#0d7e7c"));
		canvasPanel.setBackground(Color.WHITE);

		resetButton.addActionListener(new Clicked());
		resetButton.setBackground(Color.WHITE);
		submitButton.addActionListener(new Clicked());
		submitButton.setBackground(Color.WHITE);

		xInputField.setToolTipText("x coordinate");
		yInputField.setToolTipText("y coordinate");
		radiusInputField.setToolTipText("radius");
		resolutionInputField.setToolTipText("resolution");

		canvas.add(canvasPanel, BorderLayout.CENTER);
		canvas.add(buttonPanel, BorderLayout.PAGE_END);
		canvas.setVisible(true);

		curScreenMaxWidth = canvasPanel.getWidth();
		curScreenMaxHeight = canvasPanel.getHeight();

		canvasPanel.requestFocusInWindow();
	}

	public double scaleBetween(double unscaledNum, double minAllowed, double maxAllowed, double feedMin, double feedMax) { //map to this range from this range
		return (maxAllowed - minAllowed) * (unscaledNum - feedMin) / (feedMax - feedMin) + minAllowed;
	}

	public int getPixelEscapeTime(int pX, int pY) {
		double scaledX = scaleBetween(pX, xMin, xMax, curScreenMinWidth, curScreenMaxWidth);
		double scaledY = scaleBetween(pY, yMin, yMax, curScreenMinHeight, curScreenMaxHeight);
		double x = 0;
		double y = 0;
		int iteration = 0;
		int max_iteration = (int) resolution;

		while (x*x + y*y < 2*2 && iteration < max_iteration) {
			double xtemp = x*x - y*y + scaledX;
			y = 2*x*y + scaledY;
			x = xtemp;
			iteration++;
		}

		return iteration;
	}

	public void updateMinMax(double x, double y, double radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		xMin = x-radius;
		xMax = x+radius;
		yMin = y-radius;
		yMax = y+radius;

		canvasPanel.repaint();

		xInputField.setText(String.valueOf(x));
		yInputField.setText(String.valueOf(y));
		radiusInputField.setText(String.valueOf(radius));
		resolutionInputField.setText(String.valueOf(resolution));
	}

	@SuppressWarnings("serial")
	private class CanvasPanel extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
			Graphics graphics = g;
			super.paintComponent(graphics);

			//System.out.print("repainting at ("+x+", "+y+")"+" radius "+radius+" ... ");
			canvas.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			for(int x=0;x<curScreenMaxWidth;x++) {
				for(int y=0;y<curScreenMaxWidth;y++) {
					int pixelEscapeTime = getPixelEscapeTime(x, y);
					float nsmooth = (float) (1 - Math.log(Math.log(Math.abs(pixelEscapeTime)))/Math.log(2));
					int color = Color.HSBtoRGB(0.6f*nsmooth,1f,1f);
					graphics.setColor(new Color(color));
					graphics.drawRect(x, curScreenMaxHeight-y, 1, 1);
				}
			}

			canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			//System.out.println("Done");
		}
	}

	private class KeyPressed implements KeyListener {
		@Override public void keyPressed(KeyEvent e) {}
		@Override public void keyReleased(KeyEvent e) {}
		@Override public void keyTyped(KeyEvent e) {

			if(e.getKeyChar()=='8') {
				updateMinMax(x, y+(radius/2), radius);
			} else if(e.getKeyChar()=='2') {
				updateMinMax(x, y-(radius/2), radius);
			} else if(e.getKeyChar()=='4') { 
				updateMinMax(x-(radius/2), y, radius);
			} else if(e.getKeyChar()=='6') { 
				updateMinMax(x+(radius/2), y, radius);
			} else if(e.getKeyChar()=='5') {
				updateMinMax(x, y, radius-(radius/4));
			}

		}

	}

	public void reset() {
		resolution = 1000;
		updateMinMax(-0.5, 0, 2);
	}

	private class Clicked implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource().equals(resetButton)) {
				reset();
			} else if(e.getSource().equals(submitButton)) {
				try {
					double xInputVal = Double.parseDouble(xInputField.getText());
					double yInputVal = Double.parseDouble(yInputField.getText());
					double radiusInputVal = Double.parseDouble(radiusInputField.getText());
					resolution = (int) Double.parseDouble(resolutionInputField.getText());
					
					updateMinMax(xInputVal, yInputVal, radiusInputVal);

				} catch (NumberFormatException e2) {
					canvasPanel.getGraphics().setColor(Color.BLACK);
					canvasPanel.getGraphics().drawString("ERROR", canvasPanel.getWidth()/2, canvasPanel.getHeight()/2);				
				}
			}

			canvasPanel.requestFocusInWindow();
		}
	}

}
