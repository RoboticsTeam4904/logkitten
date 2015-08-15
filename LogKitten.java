package org.usfirst.frc4904.logkitten;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;

public class LogKitten {
	private FileOutputStream fileOutput;
	private final File file;
	private final String identifier;
	private final KittenLevel logLevel;
	private final KittenLevel printLevel;
	public final static KittenLevel LEVEL_WTF = new KittenLevel("WTF", -1);
	public final static KittenLevel LEVEL_FATAL = new KittenLevel("FATAL", 0);
	public final static KittenLevel LEVEL_ERROR = new KittenLevel("ERROR", 1);
	public final static KittenLevel LEVEL_WARN = new KittenLevel("WARN", 2);
	public final static KittenLevel LEVEL_VERBOSE = new KittenLevel("VERBOSE", 3);
	public final static KittenLevel LEVEL_DEBUG = new KittenLevel("DEBUG", 4);
	public static KittenLevel DEFAULT_LOG_LEVEL = LEVEL_VERBOSE;
	public static KittenLevel DEFAULT_PRINT_LEVEL = LEVEL_WARN;
	private static String LOG_PATH = "/home/lvuser/logs/";
	private static final int METHOD_CONSTRUCTOR = 3;
	private static final int METHOD_LOGGER = 4;
	private static boolean PRINT_MUTE = false;
	
	/**
	 * Gives birth to a new LogKitten instance - 4904's logging class
	 * 
	 * @param identifier
	 *        a string to identify the subject of the logs
	 * @param logLevel
	 *        the minimum KittenLevel (exposed publicly from LogKitten) which messages will be streamed to a file
	 * @param printLevel
	 *        the minimum KittenLevel (exposed publicly from LogKitten) which messages will be printed to the console
	 */
	public LogKitten(String identifier, KittenLevel logLevel, KittenLevel printLevel) {
		this.identifier = identifier;
		this.logLevel = logLevel;
		String filePath = LOG_PATH + identifier + ".log"; // Set this sessions log to /home/lvuser/logs/[current time].log
		file = new File(filePath);
		try {
			// Create new file if it doesn't exist (this should happen)
			if (!file.exists()) {
				file.createNewFile();
			}
			// Create FileOutputStream to actually write to the file.
			fileOutput = new FileOutputStream(file);
		}
		catch (IOException ioe) {
			System.out.println("Could not open logfile");
			ioe.printStackTrace();
		}
		this.printLevel = printLevel;
	}
	
	/**
	 * New LogKitten with default logLevel and specified printLevel
	 * 
	 * @param identifier
	 * @param printLevel
	 */
	public LogKitten(String identifier, KittenLevel printLevel) {
		this(identifier, DEFAULT_LOG_LEVEL, printLevel);
	}
	
	/**
	 * New LogKitten with default log and print level
	 * 
	 * @param identifier
	 */
	public LogKitten(String identifier) {
		this(identifier, DEFAULT_PRINT_LEVEL);
	}
	
	/**
	 * New LogKitten with caller class name as identifier
	 * 
	 * @param logLevel
	 * @param printLevel
	 */
	public LogKitten(KittenLevel logLevel, KittenLevel printLevel) {
		this(getCallerMethodName(METHOD_CONSTRUCTOR), logLevel, printLevel);
	}
	
	/**
	 * New LogKitten with caller class name as identifier and default logLevel
	 * 
	 * @param printLevel
	 */
	public LogKitten(KittenLevel printLevel) {
		this(getCallerMethodName(METHOD_CONSTRUCTOR), printLevel);
	}
	
	/**
	 * New LogKitten with caller class name as identifier and default log and print levels
	 */
	public LogKitten() {
		this(getCallerMethodName(METHOD_CONSTRUCTOR));
	}
	
	/**
	 * Get the name of a method's caller
	 * 
	 * @param methodType
	 *        either METHOD_CONSTRUCTOR if the callee is the constuctor or METHOD_LOGGER if the callee is f, e, w, v, d
	 * @return the name of the caller method as a string
	 */
	private static String getCallerMethodName(int methodType) {
		switch (methodType) {
			case METHOD_CONSTRUCTOR:
				return Thread.currentThread().getStackTrace()[METHOD_CONSTRUCTOR].getClassName().replaceAll("^.*\\.", "");
			case METHOD_LOGGER:
				return Thread.currentThread().getStackTrace()[METHOD_LOGGER].getMethodName();
		}
		return "";
	}
	
	/**
	 * Set the default level for which logs will be streamed to a file (for all LogKitten instances)
	 * 
	 * @param DEFAULT_LOG_LEVEL
	 *        default write-to-file level
	 */
	public static void setDefaultLogLevel(KittenLevel DEFAULT_LOG_LEVEL) {
		LogKitten.DEFAULT_LOG_LEVEL = DEFAULT_LOG_LEVEL;
	}
	
	/**
	 * Set the default level for which logs will be printed to the console (for all LogKitten instances)
	 * 
	 * @param DEFAULT_PRINT_LEVEL
	 *        default console log level
	 */
	public static void setDefaultPrintLevel(KittenLevel DEFAULT_PRINT_LEVEL) {
		LogKitten.DEFAULT_PRINT_LEVEL = DEFAULT_PRINT_LEVEL;
	}
	
	/**
	 * Set the logfile path for all LogKitten instances
	 * 
	 * @param LOG_PATH
	 *        logfile path as a string
	 */
	public static void setLogPath(String LOG_PATH) {
		LogKitten.LOG_PATH = LOG_PATH;
	}
	
	/**
	 * Mutes all messages except those overriding
	 * (useful for debugging)
	 * 
	 * @param mute
	 */
	public static void setPrintMute(boolean mute) {
		PRINT_MUTE = mute;
	}
	
	private void logMessage(String message, KittenLevel level, boolean override) {
		if (logLevel.compareTo(level) >= 0) {
			try {
				String content = timestamp() + " " + level.getName() + ": " + getCallerMethodName(METHOD_LOGGER) + ": " + message + " \n";
				fileOutput.write(content.getBytes());
				fileOutput.flush();
			}
			catch (IOException ioe) {
				System.out.println("Error logging " + level.getName() + " message");
				ioe.printStackTrace();
			}
		}
		if (!PRINT_MUTE || override) {
			if (printLevel.compareTo(level) >= 0) {
				System.out.println(identifier + " " + level.getName() + ": " + getCallerMethodName(METHOD_LOGGER) + ": " + message + " \n");
			}
		}
	}
	
	/**
	 * What a Terrible Failure: Report a condition that should never happen, allowing override
	 *
	 * @param message
	 * @param override
	 */
	public void wtf(String message, boolean override) {
		logMessage(message, LEVEL_WTF, override);
	}
	
	/**
	 * What a Terrible Failure: Report a condition that should never happen
	 *
	 * @param message
	 *        the message to log
	 */
	public void wtf(String message) { // Log WTF message
		wtf(message, false);
	}
	
	/**
	 * Log message at level LEVEL_FATAL allowing override
	 * 
	 * @param message
	 * @param override
	 */
	public void f(String message, boolean override) {
		logMessage(message, LEVEL_FATAL, override);
	}
	
	/**
	 * Log message at level LEVEL_FATAL
	 * 
	 * @param message
	 *        the message to log
	 */
	public void f(String message) { // Log fatal message
		f(message, false);
	}
	
	/**
	 * Log message at LEVEL_ERROR allowing override
	 * 
	 * @param message
	 * @param override
	 */
	public void e(String message, boolean override) {
		logMessage(message, LEVEL_ERROR, override);
	}
	
	/**
	 * Log message at level LEVEL_ERROR
	 * 
	 * @param message
	 *        the message to log
	 */
	public void e(String message) { // Log error message
		e(message, false);
	}
	
	/**
	 * Log message at LEVEL_WARN allowing override
	 * @param message
	 * @param override
	 */
	public void w(String message, boolean override) {
		logMessage(message, LEVEL_WARN, override);
	}
	
	/**
	 * Log message at level LEVEL_WARN
	 * 
	 * @param message
	 *        the message to log
	 */
	public void w(String message) { // Log warn message
		w(message, false);
	}
	
	/**
	 * Log message at LEVEL_VERBOSE allowing override
	 * @param message
	 * @param override
	 */
	public void v(String message, boolean override) {
		logMessage(message, LEVEL_VERBOSE, override);
	}
	
	/**
	 * Log message at level LEVEL_VERBOSE
	 * 
	 * @param message
	 *        the message to log
	 */
	public void v(String message) { // Log verbose message
		v(message, false);
	}
	
	/**
	 * Log message at LEVEL_VERBOSE (INFO links to verbose) allowing override
	 * @param message
	 * @param override
	 */
	public void i(String message, boolean override){
		logMessage(message, LEVEL_VERBOSE, override);
	}
	
	/**
	 * Log message at LEVEL_VERBOSE (INFO links to verbose)
	 * @param message
	 */
	public void i(String message){
		i(message, false);
	}
	
	/**
	 * Log message at level LEVEL_DEBUG allowing override
	 * @param message
	 * @param override
	 */
	public void d(String message, boolean override) {
		logMessage(message, LEVEL_DEBUG, override);
	}
	
	/**
	 * Log message at level LEVEL_DEBUG
	 * 
	 * @param message
	 *        the message to log
	 */
	public void d(String message) { // Log debug message
		d(message, false);
	}
	
	/**
	 * Tries to close the logfile stream
	 */
	public void clean() {
		try {
			fileOutput.close();
		}
		catch (IOException ioe) {
			System.out.println("Could not close logfile output. This should never happen");
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Get a timestamp for the current datetime - me-wow!
	 * 
	 * @return timestamp as string in the format "YEAR-MONTH-DAY_HOUR:MIN:SEC"
	 */
	private String timestamp() {
		Calendar now = Calendar.getInstance();
		String timestamp = Integer.toString(now.get(Calendar.YEAR));
		timestamp += "-" + Integer.toString(now.get(Calendar.MONTH) + 1);
		timestamp += "-" + Integer.toString(now.get(Calendar.DATE));
		timestamp += "_" + Integer.toString(now.get(Calendar.HOUR_OF_DAY));
		timestamp += ":" + Integer.toString(now.get(Calendar.MINUTE));
		timestamp += ":" + Integer.toString(now.get(Calendar.SECOND));
		return timestamp;
	}
	
	private static class KittenLevel implements Comparable<KittenLevel>, Comparator<KittenLevel> {
		private String name;
		private int severity;
		
		/**
		 * Construct a new KittenLevel instance
		 * 
		 * @param name
		 * @param severity
		 */
		public KittenLevel(String name, int severity) {
			this.severity = severity;
			this.name = name;
		}
		
		/**
		 * Get the level severity
		 * 
		 * @return the level severity as an int
		 */
		public int getSeverity() {
			return severity;
		}
		
		/**
		 * Get the level name
		 * 
		 * @return level name as a string
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Compare the severity of two KittenLevels
		 */
		public int compare(KittenLevel o1, KittenLevel o2) {
			return o1.getSeverity() - o2.getSeverity();
		}
		
		/**
		 * Compare the instance's severity to another KittenLevel
		 */
		public int compareTo(KittenLevel o) {
			if (compare(this, o) > 0) {
				return 1;
			} else if (compare(this, o) == 0) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}
