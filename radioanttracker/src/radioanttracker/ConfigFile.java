package radioanttracker;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.util.Properties;

public class ConfigFile {

	//public configuration variables
		// Radio 1 information
		public String radio1ModelNumber="ID0460";
		public String radio1ModelName="FTdx3000";
		public String radio1PortName="COM13";
		public String radio1Baudrate="38400";
		public String radio1PollRate="500";  // default 500 millisec
		// Radio 2 information
		public String radio2ModelNumber="ID0570";
		public String radio2ModelName="FT-991";
		public String radio2PortName="COM15";
		public String radio2Baudrate="38400";
		public String radio2PollRate="500";  // default 500 millisec
		// speAmp communications are currently not used by this program
		// control port is used by the SPE Term_1.3K application
		// CAT port 1 is connected to Win4Yaesu for radio 1
		// CAT port 2 is not used at this time - 3/10/2019
		public String speAmpModelName="SPE 1.3K";
		public String speAmpPortName="COM3";  //control port
		public String speAmpBaudrate="4800";
		public String speAmpCATPort1="COM2";  //CAT port radio 1
		public String speAmpCATBaudrate1="38400";
		public String speAmpCATPort2="COM99";  //CAT port radio 2
		public String speAmpCATBaudrate2="38400";
		// udp broadcast port information (defaults)
		public String broadcastList="LOCALHOST:127.0.0.1:12060, 13063, 13065; NR4O-pi1:192.168.1.250:12060; ESP_349ADA:192.168.1.75:12060;";
		// IP controlled antenna controller information
		public String switch1IP="192.168.1.x";
		public String switch2IP="192.168.1.y";
		public String antLabelListR1="";
		public String antLabelListR2="";
		public String lastAntSelectR1="";
		public String lastAntSelectR2="";
		// default configuration file information
		public String configPath="C:\\Ham\\";  //default path to configuration file
		public String configFilename="AntennaTracker.cfg";  //default configuration file
		public String configFile=configPath.concat(configFilename);
		
	public ConfigFile(String filePath) {
		
		// constructor code block
		configPath = filePath;
		configFilename = "AntennaTracker.cfg"; 
		configFile = configPath.concat(configFilename);
		System.out.println("Configuration file: " + configFile);
		// read the configuration file when the object is created to initialize all variables
		
		// initialize File object
		File origFile = new File(configFile);
		
		// check if file exists and read it else create a default configuration file
		if(origFile.exists()) {
			System.out.println("Initializing: Reading configuration file");
			readConfigFile(configFile);
		}	
		else {
			System.out.println("Initializing: Configuration file does not exist");
			createConfigFile(configFile);
		}
	}

	public void readConfigFile(String file) {
		
		//This code sets up the File Reader Object and points to the config file.
		try(FileReader reader = new FileReader(file)) {
			Properties properties = new Properties();
			properties.load(reader);
			
			//Strings to read from properties list (Remember case sensitive)

			radio1ModelNumber = properties.getProperty("radio1ModelNumber");
			radio1ModelName = properties.getProperty("radio1ModelName");
			radio1PortName = properties.getProperty("radio1PortName");
			radio1Baudrate = properties.getProperty("radio1Baudrate");
			radio1PollRate = properties.getProperty("radio1PollRate");
			
			radio2ModelNumber = properties.getProperty("radio2ModelNumber");
			radio2ModelName = properties.getProperty("radio2ModelName");
			radio2PortName = properties.getProperty("radio2PortName");
			radio2Baudrate = properties.getProperty("radio2Baudrate");
			radio2PollRate = properties.getProperty("radio2PollRate");
			
			speAmpModelName = properties.getProperty("speAmpModelName");
			speAmpPortName = properties.getProperty("speAmpPortName");
			speAmpBaudrate = properties.getProperty("speAmpBaudrate");
			speAmpCATPort1 = properties.getProperty("speAmpCATPort1");
			speAmpCATBaudrate1 = properties.getProperty("speAmpCATBaudrate1");
			speAmpCATPort2 = properties.getProperty("speAmpCATPort2");
			speAmpCATBaudrate2 = properties.getProperty("speAmpCATBaudrate2");
			
			broadcastList = properties.getProperty("broadcastList");
			
			switch1IP = properties.getProperty("switch1IP");
			switch2IP = properties.getProperty("switch2IP");
			
			antLabelListR1 = properties.getProperty("antLabelListR1");
			antLabelListR2 = properties.getProperty("antLabelListR2");
			
			lastAntSelectR1 = properties.getProperty("lastAntSelectR1");
			lastAntSelectR2 = properties.getProperty("lastAntSelectR2");
			
			
		} catch (Exception e) {
			System.out.println("readConfigFile: Configuration file does not exist or read error");
			e.printStackTrace();
		}
	}
	
	public void writeConfigFile(String file) {
		File fileInput = new File(file);
		
		try (FileWriter fileWriter = new FileWriter(fileInput)) {
			
			BufferedWriter bufferWrite = new BufferedWriter(fileWriter);
			bufferWrite.append("# java properties file for my AntennaTracker program project\r\n\r\n");
			bufferWrite.append("# Radio & accessory model information\r\n");
			bufferWrite.append("radio1ModelNumber=" + radio1ModelNumber + "\r\n");
			bufferWrite.append("radio1ModelName=" + radio1ModelName + "\r\n");
			bufferWrite.append("radio1PortName=" + radio1PortName + "\r\n");
			bufferWrite.append("radio1Baudrate=" + radio1Baudrate + "\r\n");
			bufferWrite.append("# default poll rate 500 milliseconds\r\n");
			bufferWrite.append("radio1PollRate=" + radio1PollRate + "\r\n");
			bufferWrite.newLine();
			bufferWrite.append("radio2ModelNumber=" + radio2ModelNumber + "\r\n");
			bufferWrite.append("radio2ModelName=" + radio2ModelName + "\r\n");
			bufferWrite.append("radio2PortName=" + radio2PortName + "\r\n");
			bufferWrite.append("radio2Baudrate=" + radio2Baudrate + "\r\n");
			bufferWrite.append("# default poll rate 500 milliseconds\r\n");
			bufferWrite.append("radio2PollRate=" + radio2PollRate + "\r\n");
			bufferWrite.newLine();
			bufferWrite.append("# speAmp communications are currently not used by this program\r\n" + 
					"	# control port is used by the SPE Term_1.3K application\r\n" + 
					"	# CAT port 1 is connected to Win4Yaesu for radio 1\r\n" + 
					"	# CAT port 2 is not used at this time - 3/10/2019\r\n");
			bufferWrite.append("speAmpModelName=" + speAmpModelName + "\r\n");
			bufferWrite.append("speAmpPortName=" + speAmpPortName + "\r\n");
			bufferWrite.append("speAmpBaudrate=" + speAmpBaudrate + "\r\n");
			bufferWrite.append("speAmpCATPort1=" + speAmpCATPort1 + "\r\n");
			bufferWrite.append("speAmpCATBaudrate1=" + speAmpCATBaudrate1 + "\r\n");
			bufferWrite.append("speAmpCATPort2=" + speAmpCATPort2 + "\r\n");
			bufferWrite.append("speAmpCATBaudrate2=" + speAmpCATBaudrate2 + "\r\n");
			bufferWrite.newLine();
			bufferWrite.append("# broadcastList format \"Hostname:ip-address:port list(comma separated);\"\r\n" + 
					"#\r\n" + 
					"#     	12060 = N1MM+ default radio info port\r\n" + 
					"#   	13063 = WaterfallBandmap program instance 1 port\r\n" + 
					"#   	13065 = WaterfallBandmap program instance 2 port\r\n" + 
					"#\r\n");
			bufferWrite.append("broadcastList=" + broadcastList + "\r\n");
			bufferWrite.newLine();
			bufferWrite.append("# switchIP format \"ip-address\"\r\n");
			bufferWrite.append("switch1IP=" + switch1IP + "\r\n");
			bufferWrite.append("switch2IP=" + switch2IP + "\r\n");
			bufferWrite.newLine();
			bufferWrite.append("# antLabelList format \"relay number:ant label:band(comma separated);\" for each radio\r\n");
			bufferWrite.append("antLabelListR1=" + antLabelListR1 + "\r\n\r\n");
			bufferWrite.append("antLabelListR2=" + antLabelListR2 + "\r\n");
			bufferWrite.newLine();
			bufferWrite.append("# last saved antenna selection for each radio - default value is 0 \r\n");		
			bufferWrite.append("lastAntSelectR1=" + lastAntSelectR1 + "\r\n");
			bufferWrite.append("lastAntSelectR2=" + lastAntSelectR2 + "\r\n");
			bufferWrite.newLine();
			
			// Flush the file write buffer and close the file		
			bufferWrite.flush();
			bufferWrite.close();
			
		} catch (IOException e) {
			System.out.println("writeConfigFile: Configuration file does not exist or write error");
			e.printStackTrace();
		}
	}
		
	public boolean backupConfigFile(String file) {
		boolean returnVal = false;
		// create a new config backup file if one doesn't exist
		
		// initialize File object
		File origFile = new File(file);
		
		String backupFilename = file.substring(0,file.length()-3) + "bak";
		
		File backupFile = new File(backupFilename);
		
		try {
			// check if file exists
			if(origFile.exists()) {
				
				if(backupFile.exists()) {
					// delete current backupfile
					boolean result = backupFile.delete();
					
					if(result) {
						System.out.println("Successfully deleted: " + backupFile.getCanonicalPath());
					} else {
						System.out.println("Failed deleting " + backupFile.getCanonicalPath());
					}
				}
				
				// rename the file if it exists
				boolean result = origFile.renameTo(backupFile);
				// check if the rename operation is success
				if(result) {
					
					// now read the newly created backup file
					// readConfigFile(backupFilename);
				
					// write a new version of the configuration file
					writeConfigFile(file);
				
					System.out.println("backupConfigFile: Configuration file backup complete");
	
					returnVal = true;
					return returnVal;
				
				} else {
					System.out.println("backupConfigFile: Configuration file backup failed");
					return returnVal;
				}	
			
			} else {
				System.out.println("backupConfigFile: Backup Configuration file does not exist");
			
				// if file doesn't exist create it
				createConfigFile(file);
			}
		} catch (IOException e) {
			System.out.println("backupConfigFile: Backup Configuration file error");
			e.printStackTrace();
		}
		return returnVal;
	}

	public boolean createConfigFile(String file) {	
		
		boolean returnVal = false;
		// initialize the File object
		File newFile = new File(file);
		
		boolean result;
		try {
			System.out.println("Creating config file from defaults");
			// create new empty file with default values
			result = newFile.createNewFile();
			
			// test if successfully created a new file
			if(result) {
				returnVal = true;
				
				// now write default values to the config file
				writeConfigFile(file);
				
				System.out.println("Config file successfully created: " + newFile.getCanonicalFile());
			}
			else {
				returnVal = false;
				System.out.println("Failed creating " + newFile.getCanonicalPath());		
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnVal;
	} // end of createConfigFile
}
