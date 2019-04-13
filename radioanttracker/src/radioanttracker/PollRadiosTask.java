package radioanttracker;

import java.util.*;
    
class PollRadiosTask {
	
	// radio data for swap function
	public static String vfoA = "";
	public static String vfoB = "";
	public static String vfoAmode = "";
	public static String vfoBmode = "";
	
    private Timer timer;   

    private String radio1data = "";
    private String radio2data = "";
    
    private String radio1txband = "";
    private String radio2txband = "";

    private String radio1txfreq = "";
    private String radio2txfreq = "";
    
    private String radio1rxfreq = "";
    private String radio2rxfreq = "";
    
    private String radio1model = "";
    private String radio2model = "";
    
    private String radio1antlabel = "";
    private String radio2antlabel = "";
    
    String radio1PortName = "COM13";
	String radio2PortName = "COM15";
	int baudrate = 38400; // default baudrate for now
	
	
    // instantiate the radio object
	Radio radioOne = new Radio(radio1PortName, baudrate);

	Radio radioTwo = new Radio(radio2PortName, baudrate);
	
	public PollRadiosTask(long msec) {
		
        timer = new Timer();
        // long msec = 1000;
        timer.scheduleAtFixedRate(new MyTask(), 0*msec, msec);
        System.out.println("Timer is started.");
        DisplayFrame.appendtext("Timer is started.\n");
	}
	
	public String getradio1data() {
		return radio1data;
	}
	
	public String getradio2data() {
		return radio2data;
	}
	
	public String getradio1txband() {
		return radio1txband;
	}
	
	public String getradio2txband() {
		return radio2txband;
	}
	
	public String getradio1txfreq() {
		return radio1txfreq;
	}
	
	public String getradio2txfreq() {
		return radio2txfreq;
	}
	
	public String getradio1rxfreq() {
		return radio1rxfreq;
	}
	
	public String getradio2rxfreq() {
		return radio2rxfreq;
	}
	
	public String getradio1model() {
		return radio1model;
	}
	
	public String getradio2model() {
		return radio2model;
	}
	
	public String getradio1antlabel() {
		return radio1antlabel;
	}
	
	public String getradio2antlabel() {
		return radio2antlabel;
	}
	
    private class MyTask extends TimerTask {
        @Override
        public void run() {

        	// task to do
        	
        	if (radioOne.initialized && radioOne.initialized) {
        	
	        	// if swapflag not set do a regular radio poll else swap radio data
	        	if (!DisplayFrame.swapflag) { 
	            
		            // Instantiate a Date object
		            Date date = new Date();
		            
		        	radio1data = radioOne.getRadioData();
		        	radio1model = radioOne.getRadioModel();
		        	radio1txband = radioOne.getTxBand();
		        	radio1txfreq = radioOne.getTxFreq();
		        	radio1rxfreq = radioOne.getRxFreq();
		        	radio1antlabel = radioOne.getAntLabel();
		 
		        	/*
		        	String mhz = radio1txfreq.substring(0, radio1txfreq.length() - 6);
		        	String khz = radio1txfreq.substring(radio1txfreq.length() - 6, radio1txfreq.length() - 3 );
		        	String hz = radio1txfreq.substring(radio1txfreq.length() - 3, radio1txfreq.length());
		        	
		        	System.out.print("----------------");
		            // display time and time zone
		            String str = String.format("%tT %tZ", date, date);
		        	System.out.printf(str);   	
		        	System.out.println("----------------");
		        	
					System.out.print(radioOne.getRadioModel() + " - " + mhz + "." + khz + "." + hz);
					System.out.print(" - " + radioOne.getTxBand());
					System.out.println(" - " + radioOne.getAntLabel());
		        	System.out.println(radio1data);
		        	*/
		        	
		        	radio2data = radioTwo.getRadioData();
		        	radio2model = radioTwo.getRadioModel();
		        	radio2txband = radioTwo.getTxBand();
		        	radio2txfreq = radioTwo.getTxFreq();
		        	radio2rxfreq = radioTwo.getRxFreq();
		        	radio2antlabel = radioTwo.getAntLabel();
		        	
		        	/*
		        	mhz = radio2txfreq.substring(0, radio2txfreq.length() - 6);
		        	khz = radio2txfreq.substring(radio2txfreq.length() - 6, radio2txfreq.length() - 3 );
		        	hz = radio2txfreq.substring(radio2txfreq.length() - 3, radio2txfreq.length());
		
		
		        	System.out.println("--------------------------------------------");
					System.out.print(radioTwo.getRadioModel() + " - " + mhz + "." + khz + "." + hz);
					System.out.print(" - " + radioTwo.getTxBand());
					System.out.println(" - " + radioTwo.getAntLabel());
		        	// System.out.println(radio2data);
					
		        	System.out.println("--------------------------------------------");
		        	*/
	        	}
	        	// swap radio data instead of a normal radio poll
	        	else {
	        		radioOne.setVfoAB(radioTwo.vfoA, radioTwo.vfoB, radioTwo.vfoAmode, radioTwo.txVfo);
	        		// System.out.println("Mode: "+ radioTwo.vfoAmode);
	        		
	        		radioTwo.setVfoAB(radioOne.vfoA, radioOne.vfoB, radioOne.vfoAmode, radioOne.txVfo);
	        		// System.out.println("Mode: "+ radioOne.vfoAmode);
	        		
	        		DisplayFrame.appendtext("\nSwap radio command complete\n\n");
	
	        		// sleep for 500 milliseconds before clearing swap flag
	    			try {
	    				Thread.sleep(500);
	    			} catch (InterruptedException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}  // sleep for a little bit	
	    			
	        		DisplayFrame.clearswapflag();  // clear the swap flag
	        	}
	        }
        }
    }     
}
