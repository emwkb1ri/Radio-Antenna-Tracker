package radioanttracker;

import java.util.Timer;
import java.util.TimerTask;

import com.fazecast.jSerialComm.SerialPort;

public class Radio {
	
	// radio data for swap function
	public String vfoA = "";
	public String vfoB = "";
	public String vfoAmode = "";
	public String vfoBmode = "";
	public boolean initialized = false;
	public int initializeWaitCounter = 0; // counter to delay printing of COM port wait
	public int initializeWaitValue = 120; // default waiut counter value # * pollrate(500 mSec)
	
	// XML radioData string
	public String radioData = "";
	
	// define the key data items to be accessed from the radio
	private String rxFreq;
	private String rxFreqTenHz;
	private String txFreq;
	private String txFreqTenHz;
	private int rxVfo;
	public int txVfo;
	private String rxMode;
	private String txMode;
	private String rxBand;
	private String txBand;
	String antenna;
	String antLabel;
	private String id;
	private String radioNr;
	private String radioModel;
	private String comPort;
	private int baudRate = 38400;  // default baud rate
	private long msec = 500; // default millisecond poll rate
	private String readData;

    private Timer timer;   

	// items not needed outside of the radio object for now

    private SerialPort[] ports;
    private SerialPort port;
	private int chosenPort = 0;

	// Radio constructor method
	public Radio(String radioNumber, String portname, String baud, String m) {
		
		// set the radio number for this instance
		radioNr = radioNumber;
		 
    	// set comPort to default values passed at time of instantiation
		comPort = portname;
		baudRate  = Integer.decode(baud);
		msec = Long.decode(m); // set polling rate 
		
		// get list of ports available on system
        ports = SerialPort.getCommPorts();
    	
        // Create a timer task to poll this radio object every msec milliseconds
        timer = new Timer();

        timer.scheduleAtFixedRate(new MyTask(), 0*msec, msec);
        System.out.println("Timer is started.");
        DisplayFrame.appendtext("Timer is started.\n");
	
	}  // END OF CONSTRUCTOR
	
	public boolean init(String portname, String baud, String m) {
		// Method to select and initialize communication with radio COM port
		// returns true if initialization successful
		
    	// set comPort to values passed in initialization call
		comPort = portname;
		baudRate  = Integer.decode(baud);
		msec = Long.decode(m); // set polling rate (*** need to investigate how to change poll rate)
        
        System.out.print("Selecting port:  ");
        DisplayFrame.appendtext("Selecting port:  ");
        
		// determine which serial port to use by comparing 
        // SystemPortName to the parameter portname
        int i = 1;
        for(SerialPort port : ports) 
        {       	
       		// System.out.println(i + ". " + port.getSystemPortName());
        	i++;
       		// if you find a match print the SystemPortName
        	// and save the port list index to chosentPort
        	// this is used later to send and receive data from 
        	// the chosenPort
        	if (port.getSystemPortName().equals(comPort))
   			{
        		System.out.println(port.getSystemPortName());
        		DisplayFrame.appendtext(port.getSystemPortName() + "\n");
       			chosenPort = i - 2;
       			break;
   			}
        }
        
       	// open and configure the port
        port = ports[chosenPort];
        System.out.print("Opening port: ");
        DisplayFrame.appendtext("Opening port: ");
        System.out.println(" " + port.getSystemPortName());
        DisplayFrame.appendtext(port.getSystemPortName() + "\n");
        
        try {

	        // validate port was opened
			if(port.openPort()) {
				System.out.println("Successfully opened the port.");
				DisplayFrame.appendtext("Successfully opened the port.\n");
			} 
			else {
				System.out.println("Unable to open the port.");
				DisplayFrame.appendtext("Unable to open the port.\n");
				return initialized;	
			}
	
        } catch (Exception e) { 
        	System.out.println("Error in opening port " + comPort);
        	e.printStackTrace(); 
        }
		
        // set baudRate = 38400(default), DataBits = 8, StopBits = 1, Parity = None
        port.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        
        // set semi blocking mode
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 2000, 0);
            
        // print current port settings
        System.out.print("Baudrate: " + port.getBaudRate());
        DisplayFrame.appendtext("Baudrate: " + port.getBaudRate());
        System.out.println("  Flow Control: " + port.getFlowControlSettings());
        DisplayFrame.appendtext("  Flow Control: " + port.getFlowControlSettings() + "\n");        
        
        // Read the radio data for the first time
        radioData = readRadioData();
        // print the radio id and model to validate initialization
		System.out.print(id);
		System.out.println("  " + radioModel);
		DisplayFrame.appendtext(id + "  " + radioModel + "\n");
		initialized = true;  // set this radio as initialized
		return initialized;       
    }
	
	// define getters and setters for the radio object
	String getRadioModel() {
		return radioModel;
	}
	
	String getRxFreq() {
		return rxFreq;
	}
	
	String getRxFreqTenHz() {
		return rxFreqTenHz;
	}
	
	String getTxFreq() {
		return txFreq;
	}
	
	String getTxFreqTenHz() {
		return txFreqTenHz;
	}
	
	int getRxVfo() {
		return rxVfo;
	}
	
	int getTxVfo() {
		return txVfo;
	}
	
	String getRxMode() {
		return rxMode;
	}
	
	String getTxMode() {
		return txMode;
	}
	
	String getRxBand() {
		return rxBand;
	}
	
	String getTxBand() {
		return txBand;
	}
	
	String getComPort() {
		return comPort;
	}
	
	int getBaudRate() {
		return baudRate;
	}
	
	String getAntLabel() {
		return antLabel;
	}
	
	String getRadioData() {
		return radioData;
	}
	
	String setVfoAB(String a, String b, String md, int tx) {
		// this function will set VFO A and B frequencies, mode and TX VFO
		
		// First set VFO A freq with VFO B freq using "ZZFAxxxx;" command

		// frequency must be 11 digits long for the Flex 
	
		for (int n = a.length(); n < 11; n++)  {
			a = "0" + a;
		}	
		for (int n = b.length(); n < 11; n++)  {
			b = "0" + b;
		}

/*  For the Flex I don't need to manipulate the VFO A & B to swap radios like on the Yaesu FT-991		
		sendSerial("ZZFA" + b + ";");
		
		// then set MODE using "ZZMDxx;" command
		sendSerial(md);
		
		// then move VFO A to VFO B using "AB;" command
		sendSerial("AB;");
		
		// now set VFO A freq with VFO freq using "FAxxxx;" command
*/
		
		sendSerial("ZZFA" + a + ";");
		
		// then set MODE using "ZZMDxx;" command
		sendSerial(md);

/* Skip setting the TX VFO in this function for now - just swap the VFO's between slices hopefully
 * 
		// now set TX VFO using "ZZSWx;" x = 1(VFO B) if tx == 1 else x = 0(VFO A)
		if (tx == 1) {
			sendSerial("ZZSW1;");
		}
		else {
			sendSerial("ZZSW0;");
		}
		
*/
		// Now clear the swapflag for this radio
		// sleep for 100 milliseconds before returning
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  // sleep for a little bit	
		
		return radioNr;
	}
	
	void  sendSerial(String cmd) {
		// Method to send serial data to the radio COM port
		
		// send command to radio serial port
        byte[] writeBuffer = cmd.getBytes();
        
        // System.out.println("Write: " + new String(writeBuffer));
             
        port.writeBytes(writeBuffer, cmd.length());
        
        // delay a short time to allow radio time to respond to command ~77msec @ 38400 baud
        
        int byterate = baudRate/1000/8; // bytes per millisecond transmission rate
        int bytespercmd = cmd.length();  // bytes for this radio command
        int bytesperresp = 12; // max bytes per command response
        int radioresp = byterate * bytesperresp; // milliseconds to allow for radio to respond 
        
        int msec = byterate * bytespercmd + radioresp; // milliseconds to sleep
        
		try {
			Thread.sleep(msec);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in sendtSerial writing to: " + comPort);
			e.printStackTrace();
		}  // sleep for a little bit
		
		return;		
	}
	
	String getSerial() {
        // SerialPort port = ports[chosenPort];
        
        byte[] readBuffer = new byte[128];
        
        // Read data from the selected COM port
        try {
            int numRead = port.readBytes(readBuffer, readBuffer.length);
            readData = new String(readBuffer);
            readData = readData.substring(0, numRead);
            // System.out.println("Read " + numRead + " bytes");
            // System.out.println(readData);
           
        } catch (Exception e) { 
        	System.out.println("Error in getSerial reading from: " + comPort);
        	e.printStackTrace(); 
        	}
        
		return readData;	
	}
	
	boolean closeRadio() {
		// SerialPort port = ports[chosenPort];
		
		boolean close = port.closePort();
		
		return close;
	}
	
	String setBand(String freq) {
		// set band fields based on rxFreq and txFreq
		// set antenna number and label selection for each band
		
		// calculate 100 Khz range of rxFreq and txFreq
		
		String hundredkhz; 
		String band;
		
		hundredkhz = freq.substring(0, freq.length() - 5);  
		
		int val = Integer.parseInt(hundredkhz);
		
		if (18 <= val && val <= 19) {
			band = "160M";
			antenna = "1";
			antLabel ="Inv-L";
		}
		else if (35 <= val && val <= 39) {
			band = "80M";
			antenna = "2";
			antLabel = "80M Dipole";
		}
		else if (53 <= val && val <= 54) {
			band = "60M";
			antenna = "1";
			antLabel = "Inv-L";
		}
		else if (70 <= val && val <= 72) {
			band = "40M";
			antenna = "3";
			antLabel = "40M Dipole";
		}
		else if (100 <= val && val <= 101) {
			band = "30M";
			antenna = "7";
			antLabel = "30-17-12M Dipole";
		}
		else if (140 <= val && val <= 143) {
			band = "20M";
			antenna = "4";
			antLabel = "20M Loop";
		}
		else if (180 <=val && val <= 181) {
			band = "17M";
			antenna = "7";
			antLabel = "30-17-12M Dipole";
		}
		else if (210 <= val && val <= 214) {
			band = "15M";
			antenna = "5";
			antLabel = "15M Loop";
		}
		else if (248 <= val && val <= 249) {
			band = "12M";
			antenna = "7";
			antLabel = "30-17-12M Dipole";
		}
		else if (280 <= val && val <= 297) {
			band = "10M";
			antenna = "6";
			antLabel = "10M Loop";
		}
		else if (500 <= val && val <= 549) {
			band = "6M";
			antenna = "1";
			antLabel = "Inv-L";
		}
		else if (1440 <= val && val <= 1479) {
			band = "2M";
			antenna = "0";	// set this band to antenna 0 - avoids conflict with radio 1 on 6M
			antLabel = "2M / 70cm J-Pole";
		}
		else if (4300 <= val && val <= 4499) {
			band = "70cm";
			antenna = "0";  // set this band to antenna 0 - avoids conflict with radio 1 on 6M
			antLabel = "2M / 70cm J-Pole";
		}
		else if (9020 <= val && val <= 9279) {
			band = "33cm";
			antenna = "0";  // set this band to antenna 0 - avoids conflict with radio 1 on 6M
			antLabel = "NONE";
		}
		else if (1240 <= val && val <= 1299) {
			band = "23cm";
			antenna = "0";  // set this band to antenna 0 - avoids conflict with radio 1 on 6M
			antLabel = "NONE";
		}
		else {
			band = "GEN";
			antenna = "1";
			antLabel = "Inv-L";
		}
	return band;
	}
	
	
	String readRadioData() {
		// Assemble the XML radio data packet
		
		String radioDataGram;
		
		// Initialize the components of the XML data packet
		String radioInfo1 = "<RadioInfo>";
		String radioInfo2 = "</RadioInfo>";
		String stationName = "<StationName>NR4O</StationName>";
		String radioNr1 = "<RadioNr>";
		// radioNr = "";
		String radioNr2 = "</RadioNr>";
		String freq1 = "<Freq>";
		// rxFreq = "";
		String freq2 = "</Freq>";
		String txFreq1 = "<TXFreq>";
		// txFreq = "";
		String txFreq2 = "</TXFreq>";
		String mode1 = "<Mode>";
		// txMode = "";
		String mode2 = "</Mode>";
		String opCall = "<OpCall>NR4O</OpCall>";
		String isRunning = "<IsRunning>False</IsRunning>";
		String focus = "<FocusEntry>00000</FocusEntry>";
		String antenna1 = "<Antenna>";
		String antenna2 = "</Antenna>";
		String rotor = "<Rotors>none</Rotors>";
		String focusRadio = "<FocusRadioNr>0</FocusRadioNr>";
		String isStereo = "<IsStereo>False</IsStereo>";
		String activeRadio = "<ActiveRadioNr>0</ActiveRadioNr>";
		
        int l = 0;
		
		// get radio ID -- ID;
		// ID = 0460 -- FTdx3000
		// ID = 0570 -- FT-991
        // ID = 909 -- Flex6600
		sendSerial("ID;");
		id = getSerial();
		if (id.equals("ID909;")) {
			radioModel = "Flex6600";
			radioModel = radioModel + "-" + radioNr; // add radio number to the model name
		}
		
		// get RX VFO -- "FR;"
		// FR0; = vfo A -- FR1; = vfo B		
		sendSerial("FR;");
		String rxvfo = getSerial();
		if (rxvfo.equals("FR1;"))
		{
			rxVfo = 1;
		}
		else
		{
			rxVfo = 0;
		}
		
		// System.out.println("RX VFO = " + rxVfo);
		
		// get TX VFO -- "FT;"
		// FT0 = vfo A -- FT1 = vfo B		
		sendSerial("FT;");
		String txvfo = getSerial();
		if (txvfo.equals("FT1;"))
		{
			txVfo = 1;
		}
		else
		{
			txVfo = 0;
		}
		
		// System.out.println("TX VFO = " + txVfo);
			
		// get vfo A freq -- "ZZFA;"
		sendSerial("ZZFA;");
		
		String afreq = getSerial();
		
		String afreqHz = afreq;
		String afreqtenHz = afreq;
		
		l = afreq.length();
		
		// if length isn't what's expected set a default freq
		if (l != 16) {
			afreq = "ZZFB00001800000;";
		}
		// strip the "FA", leading "0" and trailing ";" from the response
		// ten Hz resolution freqtenHz to meet N1MM xml definition
		int i = 4; // frequency digits start at index pointer = 4
		
		while (afreq.substring(i, i + 1).equals("0")) {
			// increment the string pointer for every leading "0"
			i++;
		}
		
		// now set the strings to the frequency with no leading zeros
		// or trailing ";"
		afreqHz = afreq.substring(i, l - 1); 
		afreqtenHz = afreq.substring(i, l - 2);

		
		// ********* may have to validate the VFO B actually exists or will this return slice B info?
		
		// if rxVfo or txVfo are not set to 1 - skip reading vfo B freq 
		
		String bfreq = "ZZFB00000100000;"; // set default freq of 100 kHz
		String bfreqHz = "100000";
		String bfreqtenHz = "10000";
		
		if (rxVfo == 1 || txVfo == 1) {
			
			// get vfo B freq -- "ZZFB;"
			sendSerial("ZZFB;");
			
			bfreq = getSerial();

			bfreqHz = bfreq;
			bfreqtenHz = bfreq;
			
			l = bfreq.length();
			// if length isn't what's expected set a default freq
			if (l != 16) {
				bfreq = "ZZFB00000100000;";  // set a default of 100 kHz
			}
						
			// strip the FB and ; from the response
			i = 4; // frequency digits start at index pointer = 4 
			while (bfreq.substring(i, i + 1).equals("0")) {
				// increment the string pointer for every leading "0"
				i++;
			}
			// now set the strings to the frequency with no leading zeros
			// or trailing ";"
			bfreqHz = bfreq.substring(i, l - 1); 
			bfreqtenHz = bfreq.substring(i, l - 2);
		}
		
		// set the global vfoA and vfoB values
		vfoA = afreqHz;
		vfoB = bfreqHz;	

		
		// set the rxFreq and txFreq values based on rx/txVfo values
		if (rxVfo == 0) {
			rxFreq = afreqHz;
			rxFreqTenHz = afreqtenHz;
		}
		else if (rxVfo == 1) {
			rxFreq = bfreqHz;
			rxFreqTenHz = bfreqtenHz;
		}
		
		if (txVfo == 0) {
			txFreq = afreqHz;
			txFreqTenHz = afreqtenHz;
		}
		else if (txVfo == 1) {
			txFreq = bfreqHz;
			txFreqTenHz = bfreqtenHz;
		}
		
		// Set the rx and tx band values
		rxBand = setBand(rxFreq);
		txBand = setBand(txFreq);
		
		// get mode -- from Flex with ZZMD;
		sendSerial("ZZMD;");
		txMode = getSerial();
		vfoAmode = txMode;
		// System.out.println(mode);
		
		// decode the mode response from radio
		switch (txMode) {
		
		case ("ZZMD00;"):  
			txMode = "LSB";
			rxMode = txMode;
			break;
			
		case ("ZZMD01;"):
			txMode = "USB";
			rxMode = txMode;
			break;
			
		case ("ZZMD03;"): 
			txMode = "CWL";
			rxMode = txMode;
			break;
			
		case ("ZZMD04;"):
			txMode = "CWU";
			rxMode = txMode;
			break;
			
		case ("ZZMD05;"): 
			txMode = "FM";
			rxMode = txMode;
			break;
			
		case ("ZZMD06;"):
			txMode = "AM";
			rxMode = txMode;
			break;
			
		case ("ZZMD07;"):
			txMode = "DIGU";
			rxMode = txMode;
			break;
			
		case ("ZZMD09;"):
			txMode = "DIGL";
			rxMode = txMode;
			break;
			
		case ("ZZMD10;"):
			txMode = "SAM";
			rxMode = txMode;
			break;
			
		case ("ZZMD11;"):
			txMode = "NFM";
			rxMode = txMode;
			break;
			
		case ("ZZMD12;"):
			txMode = "DFM";
			rxMode = txMode;
			break;
			
		case ("ZZMD20;"):
			txMode = "FDV";
			rxMode = txMode;
			break;
			
		case ("ZZMD30;"):
			txMode = "RTTY";
			rxMode = txMode;
			break;
			
		case ("ZZMD40;"):
			txMode = "DSTR";
			rxMode = txMode;
			break;
			
		default:
			txMode = "none";
			rxMode = txMode;
		}
		
		// System.out.println("Mode = " + txMode);
		
		// Finally assemble the N1MM compatible radio XML datagram string
		String xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		
		radioDataGram = (xmlHeader + "\n" + radioInfo1 + "\n\t" + stationName + "\n\t" + radioNr1 + radioNr + radioNr2 
				+ "\n\t" + freq1 + rxFreqTenHz + freq2 + "\n\t" + txFreq1 + txFreqTenHz + txFreq2 + "\n\t" + 
				mode1 + txMode + mode2 + "\n\t" + opCall + "\n\t" + isRunning + "\n\t" + focus + 
				"\n\t" + antenna1 + antenna + antenna2 + "\n\t" + rotor + "\n\t" + focusRadio + 
				"\n\t" + isStereo + "\n\t" + activeRadio + "\n" + radioInfo2);
		
		return radioDataGram;
	}
	
    private class MyTask extends TimerTask {
        @Override
        public void run() {

        	// task to do
        	
        	if (initialized) {
        		if (DisplayFrame.runFlag) {
	        		// poll the radio on each timer task interrupt    
		        	radioData = readRadioData();
        		}
        	}
        	else {
        		// waiting for the radio initialization to be completed
        		// print approximately once per minute
        		
        		if (initializeWaitCounter == 0) {
	        		System.out.println("Waiting for radio initialization...");
	        		DisplayFrame.appendtext("Waiting for radio initialization...\n");
	        		// reset the waiting for initialize print counter
	        		initializeWaitCounter = initializeWaitValue;
        		}
        		else {
        			// decrement the wait counter
        			--initializeWaitCounter;	 			
        		}
        	}
        }
    }
}  // END RADIO CLASS
	
/* the N1MM+ radio udp packet XML datagram format
udp_packet = """<?xml version="1.0" encoding="utf-8"?>
   <RadioInfo>
      <StationName>NR4O</StationName>
      <RadioNr>1</RadioNr>
      <Freq>2120000</Freq>
      <TXFreq>2120000</TXFreq>
      <Mode>CW</Mode>
      <OpCall>NR4O</OpCall>
      <IsRunning>False</IsRunning>
      <FocusEntry>00000</FocusEntry>
      <Antenna>4</Antenna>
      <Rotors>tribander</Rotors>
      <FocusRadioNr>1</FocusRadioNr>
      <IsStereo>False</IsStereo>
      <ActiveRadioNr>1</ActiveRadioNr>
   </RadioInfo>
"""
*/