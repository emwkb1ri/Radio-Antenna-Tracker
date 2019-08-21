package radioanttracker;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.*;

import java.awt.event.*;

// Extends JFrame so it can create frames

public class DisplayFrame extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static JTextArea textArea1;
	JButton button1;
	JButton button2;
	int button1Clicked;
	int button2Clicked;
	static boolean runFlag = true;
	static boolean swapflag = false;
		
	public DisplayFrame(String ver){
		
		// Define the size of the frame
		this.setSize(400, 430);
		
		// Toolkit is the super class for the Abstract Window Toolkit
		// It allows us to ask questions of the OS
				
		Toolkit tk = Toolkit.getDefaultToolkit();
				
		// A Dimension can hold the width and height of a component
		// getScreenSize returns the size of the screen
				
		Dimension dim = tk.getScreenSize();
				
		// dim.width returns the width of the screen
		// this.getWidth returns the width of the frame you are making

		// set the window position to top right of screen
		int xPos = (dim.width) - (this.getWidth());
		// int yPos = (dim.height / 2) - (this.getHeight() / 2);
		int yPos = 10;
				 
		// You could also define the x, y position of the frame
				 
		this.setLocation(xPos, yPos);
		
		// Define how the frame exits (Click the Close Button)
		// Without this Java will eventually close the app
				
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
		// Define the title for the frame
				
		this.setTitle("Flex Antenna Tracker" + ver);
				
		// The JPanel contains all of the components for your frame
				
		JPanel thePanel = new JPanel();
		
		// How to add a text area ----------------------
		
		textArea1 = new JTextArea(20, 30);
				
		// Set the default text for the text area
				
		// textArea1.setText("Program output goes here - eventually\n");
				
		// If text doesn't fit on a line, jump to the next
		
		textArea1.setLineWrap(true);
		
		// Makes sure that words stay intact if a line wrap occurs
				
		textArea1.setWrapStyleWord(true);
				
		// Adds scroll bars to the text area ----------
				
		JScrollPane scrollbar1 = new JScrollPane(textArea1, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				
		// Other options: VERTICAL_SCROLLBAR_ALWAYS, VERTICAL_SCROLLBAR_NEVER, VERTICAL_SCROLLBAR_AS_NEEDED
				
		thePanel.add(scrollbar1);
		
		// Create a button with Pause on it
		
		button1 = new JButton("Pause");
		
		// Create an instance of ListenForEvents to handle events
		
		ListenForButton lForButton = new ListenForButton();
		
		// Tell Java that you want to be alerted when an event
		// occurs on the button
		
		button1.addActionListener(lForButton);
		
		thePanel.add(button1);
		
		// Create a button with Swap on it
		
		button2 = new JButton("Swap");
		
		// Create an instance of ListenForEvents to handle events
		
		ListenForButton lForSwap = new ListenForButton();
		
		// Tell Java that you want to be alerted when an event
		// occurs on the button
		
		button2.addActionListener(lForSwap);
		
		thePanel.add(button2);
		
		// Render the panel in the Frame now that all the components are defined
		
		this.add(thePanel);
	
		// Setup other listeners
		
		ListenForWindow lForWindow = new ListenForWindow();
		
		this.addWindowListener(lForWindow);
		
		this.setVisible(true);
		
		// Track the mouse if it is inside of the panel
		
		ListenForMouse lForMouse = new ListenForMouse();
		
		thePanel.addMouseListener(lForMouse);
		
	}
	
	public static void appendtext(String text) {
		// method to add text to 'textArea1'
		textArea1.append(text);
		textArea1.setCaretPosition(textArea1.getDocument().getLength());
	}
	
	// Implements ActionListener so it can react to events on components
	
	private class ListenForButton implements ActionListener{
	
	// This method is called when an event occurs
	
		public void actionPerformed(ActionEvent e){
			
			// Check if the source of the event was the button
			
			String state;
			
			if(e.getSource() == button1){
				
				button1Clicked++;
				state = button1.getText();
				
				if (state.equals("Pause")) {
				
					// insert call to pause radio polling & udp updates
					
					
					// Change the text for the label
					
					textArea1.append("UDP Updates Paused......\n" );
					button1.setText("Run");
					runFlag = false;
					
					// e.getSource().toString() returns information on the button
					// and the event that occurred
				}
				else {
					
					// Change the text for the label
					
					textArea1.append("UDP Updates Running......\n" );
					button1.setText("Pause");
					runFlag = true;
				}
			}
			else if (e.getSource() == button2) {
				// set Swap flags to swap radio 1 info with radio 2 info
				button2Clicked++;
				swapflag = true;
				textArea1.append("Swap button pressed......\n" );
			}
		}
	}
	
	
	public static void clearswapflag() {
			swapflag = false;
	}

	
	// By using KeyListener you can track keys on the keyboard
	
	private class ListenForKeys implements KeyListener{
		
		// Handle the key typed event from the text field.
	    public void keyTyped(KeyEvent e) {
	    	textArea1.append("Key Hit: " + e.getKeyChar() + "\n"); 
	    }

	    // Handle the key-pressed event from the text field.
	    public void keyPressed(KeyEvent e) {
	        
	    }

	    // Handle the key-released event from the text field.
	    public void keyReleased(KeyEvent e) {
	        
	    }
		
	}
	
	private class ListenForMouse implements MouseListener{

		// Called when a mouse button is clicked
		
		public void mouseClicked(MouseEvent e) {
			
			textArea1.append("Mouse Panel Pos: " + e.getX() + " " + e.getY() + "\n");
			textArea1.append("Mouse Screen Pos: " + e.getXOnScreen() + " " + e.getYOnScreen() + "\n"); 
			textArea1.append("Mouse Button: " + e.getButton()  + "\n"); 
			textArea1.append("Mouse Clicks: " + e.getClickCount()  + "\n");
			
		}

		// Called when the mouse enters the component assigned
		// the MouseListener
		
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		// Called when the mouse leaves the component assigned
		// the MouseListener
		
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		// Mouse button pressed
		
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		// Mouse button released
		
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class ListenForWindow implements WindowListener{

		// Called when window is the active window
		
		public void windowActivated(WindowEvent e) {
			textArea1.append("Window Activated\n");
			
		}

		// Called when window is closed using dispose
		// this.dispose(); can be used to close a window
		
		public void windowClosed(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		// Called when the window is closed from the menu
		
		public void windowClosing(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		// Called when a window is no longer the active window
		
		public void windowDeactivated(WindowEvent e) {
			textArea1.append("Window Activated\n");
			
		}

		// Called when the window goes from minimized to a normal state
		
		public void windowDeiconified(WindowEvent arg0) {
			textArea1.append("Window in Normal State\n");
			
		}

		// Called when the window goes from normal to a minimized state
		
		public void windowIconified(WindowEvent arg0) {
			textArea1.append("Window Minimized\n");
			
		}

		// Called when the window is first created
		
		public void windowOpened(WindowEvent arg0) {
			textArea1.append("Window Created\n");
			
		}	
	}
}
