package oracle.cloudstorage.ftm.samples;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

public class SetupSamplesLog {
	private static final String logHeaderformat = "%d{yyyy/MM/dd HH:mm:ss.SSS} [%5p] %t (%F) - %m%n";
	public static void initLogging(String logFile) {
		Properties properties = new Properties();
		properties.setProperty("log4j.rootLogger", "INFO,stdout,MyFile");
		properties.setProperty("log4j.rootCategory", "INFO");
		properties.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
		properties.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
		properties.setProperty("log4j.appender.stdout.layout.ConversionPattern", logHeaderformat);
		properties.setProperty("log4j.appender.MyFile", "org.apache.log4j.RollingFileAppender");
		properties.setProperty("log4j.appender.MyFile.File", logFile);
		properties.setProperty("log4j.appender.MyFile.MaxFileSize", "100MB");
		properties.setProperty("log4j.appender.MyFile.MaxBackupIndex", "100");
		properties.setProperty("log4j.appender.MyFile.layout", "org.apache.log4j.PatternLayout");
		properties.setProperty("log4j.appender.MyFile.layout.ConversionPattern", logHeaderformat);
		PropertyConfigurator.configure(properties);
	}
}
