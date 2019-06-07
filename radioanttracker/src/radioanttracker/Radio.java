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
	public Radio(String portname, String baud, String m) {
		 
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
		
		// First set VFO A freq with VFO B freq using "FAxxxx;" command
		if ((this.radioModel).equals("FT-991")) {
			System.out.println(this.radioModel);
			// pad the frequency with a leading "0" or "00" if this is the FT-991
			// frequency must be 9 digits long for FT-991
			for (int n = a.length(); n < 9; n++)  {
				a = "0" + a;
			}	
			for (int n = b.length(); n < 9; n++)  {
				b = "0" + b;
			}
		}
		// otherwise frequency must be 8 digits long for FTdx3000 
		for (int n = a.length(); n < 8; n++)  {
			a = "0" + a;
		}	
		for (int n = b.length(); n < 8; n++)  {
			b = "0" + b;
		}

		sendSerial("FA" + b + ";");
		
		// then set MODE using "MDxx;" command
		sendSerial(md);
		
		// then move VFO A to VFO B using "AB;" command
		sendSerial("AB;");
		
		// now set VFO A freq with VFO freq using "FAxxxx;" command

		sendSerial("FA" + a + ";");
		
		// then set MODE using "MDxx;" command
		sendSerial(md);
		
		// now set TX VFO using "FTx;" x = 3(VFO B) if tx ==  1 else x = 2(VFO A)
		if (tx == 1) {
			sendSerial("FT3;");
		}
		else {
			sendSerial("FT2;");
		}
		
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
			antenna = "8";	// set this band to antenna 8 - avoids conflict with radio 1 on 6M
			antLabel = "2M / 70cm J-Pole";
		}
		else if (4300 <= val && val <= 4499) {
			band = "70cm";
			antenna = "8";  // set this band to antenna 8 - avoids conflict with radio 1 on 6M
			antLabel = "2M / 70cm J-Pole";
		}
		else if (9020 <= val && val <= 9279) {
			band = "33cm";
			antenna = "8";  // set this band to antenna 8 - avoids conflict with radio 1 on 6M
			antLabel = "NONE";
		}
		else if (1240 <= val && val <= 1299) {
			band = "23cm";
			antenna = "8";  // set this band to antenna 8 - avoids conflict with radio 1 on 6M
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
		sendSerial("ID;");
		id = getSerial();

		if (id.length() != 0) {
			if (id.equals("ID0570;")) {
				radioModel = "FT-991";
				radioNr = "2";
			}
			else if (id.equals("ID0460;")) {
				radioModel = "FTdx3000";
				radioNr = "1";
			}
		}
		
		// get RX VFO -- "FR;" for FTdx3000 skip for FT-991
		// FR0 or FR1 = vfo A -- FR4 or FR5 = vfo B		
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
			
		// get vfo A freq -- "FA;"
		sendSerial("FA;");
		
		String afreq = getSerial();
		
		String afreqHz = afreq;
		String afreqtenHz = afreq;
		
		l = afreq.length();
		// strip the "FA", leading "0" and trailing ";" from the response
		// ten Hz resolution freqtenHz to meet N1MM xml definition
		if (afreq.substring(2, 3).equals("0")) {
			// if both leading digits are "00" strip them 
			if (afreq.substring(2, 4).equals("00")) {
				afreqHz = afreq.substring(4, l -1); 
				afreqtenHz = afreq.substring(4, l - 2);
			}
			
			else {
				// just strip the single leading "0"
				afreqHz = afreq.substring(3, l - 1);
				afreqtenHz = afreq.substring(3, l - 2);
			}
		}
			
		// no leading "0"
		else { 
			afreqHz = afreq.substring(2, l - 1);
			afreqtenHz = afreq.substring(2, l - 2);
		}
		
		// System.out.println("VFO A: " + afreq);
		
		// get vfo B freq -- "FB;"
		sendSerial("FB;");
		
		String bfreq = getSerial();

		String bfreqHz = bfreq;
		String bfreqtenHz = bfreq;
		
		l = bfreq.length();
		// strip the FB and ; from the response
		if (bfreq.substring(2, 3).equals("0")) {
			// if both leading digits are "00" strip them 
			if (bfreq.substring(2, 4).equals("00")) {
				bfreqHz = bfreq.substring(4, l -1);
				bfreqtenHz = bfreq.substring(4, l - 2);
			}
			// just strip the single leading "0"
			else {
				bfreqHz = bfreq.substring(3, l - 1);
				bfreqtenHz = bfreq.substring(3, l - 2);
			}
		}
		
		// no leading "0"
		else { 
			bfreqHz = bfreq.substring(2, l - 1);
			bfreqtenHz = bfreq.substring(2, l - 2);
		}
		
		// System.out.println("VFO B: " + bfreq);
		
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
		
		// get mode -- MD0;
		sendSerial("MD0;");
		txMode = getSerial();
		vfoAmode = txMode;
		// System.out.println(mode);
		
		// decode the mode response from radio
		switch (txMode) {
		
		case ("MD01;"):  
			txMode = "LSB";
			rxMode = txMode;
			break;
			
		case ("MD02;"):
			txMode = "USB";
			rxMode = txMode;
			break;
			
		case ("MD03;"): 
			txMode = "CW";
			rxMode = txMode;
			break;
			
		case ("MD04;"):
			txMode = "FM";
			rxMode = txMode;
			break;
			
		case ("MD05;"): 
			txMode = "AM";
			rxMode = txMode;
			break;
			
		case ("MD06;"):
			txMode = "RTTY";
			rxMode = txMode;
			break;
			
		case ("MD07;"):
			txMode = "CW-R";
			rxMode = txMode;
			break;
			
		case ("MD08;"):
			txMode = "DATA-L";
			rxMode = txMode;
			break;
			
		case ("MD09;"):
			txMode = "RTTY-R";
			rxMode = txMode;
			break;
			
		case ("MD0A;"):
			txMode = "DATA-FM";
			rxMode = txMode;
			break;
			
		case ("MD0B;"):
			txMode = "FM-N";
			rxMode = txMode;
			break;
			
		case ("MD0C;"):
			txMode = "DATA-U";
			rxMode = txMode;
			break;
			
		case ("MD0D;"):
			txMode = "AM-N";
			rxMode = txMode;
			break;
			
		case ("MD0E;"):
			txMode = "C4FM";
			rxMode = txMode;
			break;
			
		default:
			txMode = "none";
			rxMode = txMode;
		}
		
		// System.out.println("Mode = " + txMode);
		
		// Finally assemble the N1MM compatible radio XML datagram string 
		radioDataGram = (radioInfo1 + "\n\t" + stationName + "\n\t" + radioNr1 + radioNr + radioNr2 
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
        		// waiting for the radio initialization to tbe completed
        		System.out.println("Waiting for radio initialization...");
        		DisplayFrame.appendtext("Waiting for radio initialization...\n");
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