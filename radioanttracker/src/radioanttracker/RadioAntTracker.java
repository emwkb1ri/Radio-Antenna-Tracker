package radioanttracker;

// Java version of Radio_Ant_Tracker.py
// Reads each of my radios COM ports every .5 secs
// Calculates operating frequency from TX frequency
// Selects ant number based on band lookup table
// Assembles an XML packet in the N1MM+ OTRSP format
// Sends the XML packet via UDP to a LOCALHOST and a remote HOST IP socket port.
// Sends UDP packet every 10 seconds or when an antenna change is detected.
// Monitors keyboard input for 'p' pause, 'r' run, 'q' quit

import java.util.*;
import java.io.*;
import java.net.*;

public class RadioAntTracker {
	
/*  not sure how to instantiate the radios for global program access
	String radio1PortName = "COM13";
	String radio2PortName = "COM15";
	int baudrate = 38400; // default baudrate for now
	
    // instantiate the radio object
	Radio radioOne = new Radio(radio1PortName, baudrate);
	
	Radio radioTwo = new Radio(radio2PortName, baudrate);
*/
	
	// Initialize the current band to null to force the initial udp packet
	// to be sent to set the antenna switch controller
	
	public static String model1 = "";
	public static String model2 = "";
	public static String antlabel1 = "";
	public static String antlabel2 = "";
	public static String txband1 = "";
	public static String txband2 = "";
	public static String txfreq1 = "";
	public static String txfreq2 = "";
	public static String data1 = "";
	public static String data2 = "";
	
	// Instantiate the application window
    public static DisplayFrame frame = new DisplayFrame();
		
	// Instantiate the polling task for the radios
	// poll every 500 msec
	public static PollRadiosTask polldata = new PollRadiosTask(500);
	
	public static void udpupdate() {
		// This method is run only when radio information changes
		// it will print radio summary to the console and then
		// send udp packets to all IP and ports in the list
		
		
		// update current TX band info
		model1 = polldata.getradio1model();
		model2 = polldata.getradio2model();
		
		txband1 = polldata.getradio1txband();
		txband2 = polldata.getradio2txband();
		
		// update current TX freq info 		
		txfreq1 = polldata.getradio1txfreq();
		txfreq2 = polldata.getradio2txfreq();
		
		antlabel1 = polldata.getradio1antlabel();
		antlabel2 = polldata.getradio2antlabel();
			
		// get current radio data and send a udp packet to antenna controller		
		data1 = polldata.getradio1data();
		data2 = polldata.getradio2data();
		
		// print the updated radio summary
		
		printradioinfo();
		
		System.out.println("+++++++++ Radio 1 +++++++++");
		DisplayFrame.appendtext("+++++++++ Radio 1 +++++++++\n");
		// System.out.println(data1);
		sendudp(data1);
		System.out.println("+++++++++ Radio 2 +++++++++");
		DisplayFrame.appendtext("+++++++++ Radio 2 +++++++++\n");
		// System.out.println(data2);
		sendudp(data2);
		// System.out.println("+++++++++++++++++");
	}
	
	public static void sendudp(String data) 
	{	
		// String LOCALHOST = "127.0.0.1";
		// KB1RI-pi1 = 192.168.1.74
		// port list to send radio info udp packets
		String hosts[] = {"127.0.0.1", "192.168.1.74"};
    	int ports[][] = {
    			{12060, 13063, 13065},
    			{12060}
    			};
    	
    	// 12060 = N1MM+ default radio info port
    	// 13063 = WaterfallBandmap program instance 1 port
    	// 13065 = WaterfallBandmap program instance 2 port
		    	    	
		// send udp packets to all ports listed in the hosts[] and ports[] lists
    	for (int h = 0; h < hosts.length; h++ ) {
	    	for (int i = 0; i < ports[h].length; i++) {
	    	
		        try {
		        	InetAddress address = InetAddress.getByName(hosts[h]);
			    	// InetAddress address = InetAddress.getByName(LOCALHOST);
					DatagramSocket socket = new DatagramSocket();
					 
					byte[] buffer = new byte[1024];
					buffer = data.getBytes();
					 
					DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, ports[h][i]);
					socket.send(request);
			        socket.close();
		        } catch (SocketException ex) {
		            System.out.println("Socket error: " + ex.getMessage());
		        } catch (IOException ex) {
		            System.out.println("I/O error: " + ex.getMessage());
		        }
		        System.out.println("*** " + hosts[h] + ": " + ports[h][i] + " ***");
		        DisplayFrame.appendtext("*** " + hosts[h] + ": " + ports[h][i] + " ***\n");
			}
    	}
	}
	
	public static void printradioinfo() {
		
		// This method prints a summary of the key radio info
		
        // Instantiate a Date object
        Date date = new Date();

		// print radio 1 information
        
    	String mhz = txfreq1.substring(0, txfreq1.length() - 6);
    	String khz = txfreq1.substring(txfreq1.length() - 6, txfreq1.length() - 3 );
    	String hz = txfreq1.substring(txfreq1.length() - 3, txfreq1.length());
    	
    	System.out.print("----------------");
    	DisplayFrame.appendtext("-------------------");
        // display time and time zone
        String str = String.format("%tT %tZ", date, date);
    	System.out.printf(str);
    	DisplayFrame.appendtext(str);
    	System.out.println("----------------");
    	DisplayFrame.appendtext("-------------------\n");
    	
		System.out.print(model1 + " - " + mhz + "." + khz + "." + hz);
		System.out.print(" - " + txband1);
		System.out.println(" - " + antlabel1);
		
		DisplayFrame.appendtext(model1 + " - " + mhz + "." + khz + "." + hz);
		DisplayFrame.appendtext(" - " + txband1);
		DisplayFrame.appendtext(" - " + antlabel1 +"\n");
    	// System.out.println(radio1data);
    	
		// print radio 2 information
		
		mhz = txfreq2.substring(0, txfreq2.length() - 6);
    	khz = txfreq2.substring(txfreq2.length() - 6, txfreq2.length() - 3 );
    	hz = txfreq2.substring(txfreq2.length() - 3, txfreq2.length());
  	
    	System.out.println("--------------------------------------------");
		System.out.print(model2 + " - " + mhz + "." + khz + "." + hz);
		System.out.print(" - " + txband2);
		System.out.println(" - " + antlabel2);
    	
		DisplayFrame.appendtext("---------------------------------------------------------\n");
		DisplayFrame.appendtext(model2 + " - " + mhz + "." + khz + "." + hz);
		DisplayFrame.appendtext(" - " + txband2);
		DisplayFrame.appendtext(" - " + antlabel2 + "\n");
 
		// System.out.println(radio2data);
		
    	System.out.println("--------------------------------------------");
    	DisplayFrame.appendtext("---------------------------------------------------------\n");
	}
	
	
	public static void main(String[] args)
	{

		// create a window with text field

		System.out.println("Running....");
		DisplayFrame.appendtext("Running....\n");
		
		// delay starting the main loop to allow for the radio
		// communication ports to be opened and initial data collected
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  // sleep for a little bit

		int msec = 100; // band change poll interval in milliseconds
		int d = 30000; // regular udp update interval in milliseconds regardless of band change
		int i = d/msec; // udp band check loops before forcing a udp update
		
		while (true ) {	
			
			// radio data change flags - true if data is same as last poll
			boolean r1 = data1.equals(polldata.getradio1data());
			boolean r2 = data2.equals(polldata.getradio2data());
			boolean b1 = txband1.equals(polldata.getradio1txband());
			boolean b2 = txband2.equals(polldata.getradio2txband());
			boolean b3 = txfreq1.equals(polldata.getradio1txfreq());
			boolean b4 = txfreq2.equals(polldata.getradio2txfreq());

			// System.out.println(b1 + " " + b2);
						
			if (frame.runFlag && !DisplayFrame.swapflag) {
				// if the runflag and swapflag are true
				// then if either radio data has changed since last poll

				if (!r1 || !r2 || ( i== 0)) {
					
					// send udp packets to antenna controller
					
					udpupdate();
					
					i = d/msec; // reset the loop counter
					
				}
				else {
					// delay a little bit before checking for a band change
				
					try {
						Thread.sleep(msec);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  // sleep for a little bit
					
					i--; // decrement udp loop counter
				}
			}
			else {
				i = 0;  // force a udp packet update after a pause
				// now delay for 500 milliseconds before checking button status
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  // sleep for a little bit	
			}		
		}
	}
}

