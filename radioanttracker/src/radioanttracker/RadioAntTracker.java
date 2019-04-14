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
	int baudrate = 38400; // default baud rate for now
	
    // instantiate the radio object
	Radio radioOne = new Radio(radio1PortName, baud rate);
	
	Radio radioTwo = new Radio(radio2PortName, baud rate);
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
	public static boolean r1_Initialized = false;
	public static boolean r2_Initialized = false;
	
	// Default values until configuration file data is read
    public static String radio1PortName = "COM13";
    public static String radio1Baudrate = "38400";
    public static String radio1PollRate = "500";
	public static String radio2PortName = "COM15";
	public static String radio2Baudrate = "38400";
    public static String radio2PollRate = "500";
	static int baudrate = 38400; // default baud rate for now
	
	
	
	// Instantiate the application window
    public static DisplayFrame frame = new DisplayFrame();
	
	// Replace the PollRadioTask with updated version of Radio object
	// These may be able to be moved inside of main depending on how tasks work
	// replace references to polldata with radioOne and radioTwo where appropriate
	
	public static Radio radioOne = new Radio(radio1PortName, radio1Baudrate, radio1PollRate);
	public static Radio radioTwo = new Radio(radio2PortName, radio2Baudrate, radio2PollRate);
	
	
	public static void main(String[] args) {

	    // get the execution path to locate the desired configuration file
		// String configPath = getPath();  
		String configPath="C:\\Ham\\";  //default path to configuration file
		
		// Initialize program configuration information from configuration file
		String configFilename = "AntennaTracker.cfg"; 
		String configFile = configPath.concat(configFilename);
		
		// Initialize the configuration file information
		ConfigFile config = new ConfigFile(configPath);
		

		// configuration file read verification by printing to console
		
		System.out.println("********************");
		
		System.out.println("Radio1: " + config.radio1ModelNumber + "," + config.radio1ModelName + "," + config.radio1PortName + "," + config.radio1Baudrate + "," + config.radio1PollRate);
		System.out.println("Radio2: " + config.radio2ModelNumber + "," + config.radio2ModelName + "," + config.radio2PortName + "," + config.radio2Baudrate + "," + config.radio1PollRate);
		System.out.print("AMP: " + config.speAmpModelName + "," + config.speAmpPortName + "," + config.speAmpBaudrate);
		System.out.print("," + config.speAmpCATPort1 + "," + config.speAmpCATBaudrate1);
		System.out.println("," + config.speAmpCATPort2 + "," + config.speAmpCATBaudrate2);
		
		System.out.println("HostList: " + config.broadcastList);
		
		System.out.println("Switch #1 IP: " + config.switch1IP);
		System.out.println("Switch #2 IP: " + config.switch2IP);

		System.out.println("Radio 1 AntList: " + config.antLabelListR1);
		System.out.println("Radio 2 AntList: " + config.antLabelListR2);
		
		System.out.println("R1 Last = " + config.lastAntSelectR1);
		System.out.println("R2 Last = " + config.lastAntSelectR2);
		
		System.out.println("********************");
		
		// Initialize the radio polling objects
		
		if (radioOne.init(config.radio1PortName, config.radio1Baudrate, config.radio1PollRate)) {
			System.out.println("Radio 1 initialized...");
		}
		else {
			System.out.println("ERROR: Radio 1 NOT initialized");
		}

		if (radioTwo.init(config.radio2PortName, config.radio2Baudrate, config.radio2PollRate)) {
			System.out.println("Radio 2 initialized...");
		}
		else {
			System.out.println("ERROR: Radio 2 NOT initialized");
		}

		System.out.println("Polling started....");
		DisplayFrame.appendtext("Polling started....\n");
		
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
							
			if (DisplayFrame.swapflag) {
				
				// swap the frequency and mode data between the radios
				// read and save radio 1 data temporarily to send to radio 2
				String r1vfoA = radioOne.vfoA;
				String r1vfoB = radioOne.vfoB;
				String r1vfoAmode = radioOne.vfoAmode;
				String r1vfoBmode = radioOne.vfoBmode;
				int r1txVfo = radioOne.txVfo;
				radioOne.setVfoAB(radioTwo.vfoA, radioTwo.vfoB, radioTwo.vfoAmode, radioTwo.txVfo);
				DisplayFrame.appendtext("\n" + radioOne.getRadioModel() + " - Swap radio command complete \n\n");
				radioTwo.setVfoAB(r1vfoA, r1vfoB, r1vfoAmode, r1txVfo);
				DisplayFrame.appendtext("\n" + radioTwo.getRadioModel() + " - Swap radio command complete \n\n");
				// clear the swap flag
				DisplayFrame.clearswapflag();
			}
			
			else {
				
				// radio data change flags - true if data is same as last poll
				boolean r1 = data1.equals(radioOne.getRadioData());
				boolean r2 = data2.equals(radioTwo.getRadioData());
				boolean b1 = txband1.equals(radioOne.getTxBand());
				boolean b2 = txband2.equals(radioTwo.getTxBand());
				boolean b3 = txfreq1.equals(radioOne.getTxFreq());
				boolean b4 = txfreq2.equals(radioTwo.getTxFreq());

				
				if (DisplayFrame.runFlag) {
					// if the runflag and !swapflag are true
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
	} // end of main()
	
	
	public static void udpupdate() {
		// This method is run only when radio information changes
		// it will print radio summary to the console and then
		// send udp packets to all IP and ports in the list
		
		
		// update current TX band info
		model1 = radioOne.getRadioModel();
		model2 = radioTwo.getRadioModel();
		// System.out.println(model1 + "   "+ model2);
		
		txband1 = radioOne.getTxBand();
		txband2 = radioTwo.getTxBand();
		// System.out.println(txband1 + "   "+ txband2);
		
		// update current TX freq info 		
		txfreq1 = radioOne.getTxFreq();
		txfreq2 = radioTwo.getTxFreq();
		// System.out.println(txfreq1 + "   "+ txfreq2);
		
		antlabel1 = radioOne.getAntLabel();
		antlabel2 = radioTwo.getAntLabel();
		// System.out.println(antlabel1 + "   "+ antlabel2);
			
		// get current radio data and send a udp packet to antenna controller		
		data1 = radioOne.getRadioData();
		// System.out.println(data1);
		data2 = radioTwo.getRadioData();
		// System.out.println(data2);
		
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
		String hosts[] = {"127.0.0.1", "192.168.1.250"};
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

	
	public static String getPath() {
		
		String surroundingJar = null;
	
		// gets the path to the jar file if it exists; or the "bin" directory if calling from Eclipse
		String jarDir = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath()).getAbsolutePath();
		System.out.println("JAR Directory: " + jarDir);
		
		// gets the "bin" directory if calling from eclipse or the name of the .jar file alone (without its path)
		String jarFileFromSys = System.getProperty("java.class.path").split(";")[0];
	
		// If both are equal that means it is running from an IDE like Eclipse
		if (jarFileFromSys.equals(jarDir))
		{
		    System.out.println("RUNNING FROM IDE!");
		    // The path to the jar is the "bin" directory in that case because there is no actual .jar file.
		    surroundingJar = jarDir;
		    
		    // use this fixed path to configuration when executing in an IDE
		    jarDir = "C:\\Users\\ewpil\\iCloudDrive\\eclipse-workspace\\";
		}
		else
		{
		    // Combining the path and the name of the .jar file to achieve the final result
			jarDir = jarDir + "\\";
			surroundingJar = jarDir + jarFileFromSys;
		}
	
		System.out.println("JAR File: " + surroundingJar);
		System.out.println("JAR from Sys: " + jarFileFromSys);
		System.out.println("jarDir: " + jarDir);
		
		return jarDir; 
	}
} // end of RadioAntTracker class

