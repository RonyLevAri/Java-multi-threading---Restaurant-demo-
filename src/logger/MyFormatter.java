package logger;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MyFormatter extends Formatter{

	@Override
	public String format(LogRecord record) {
		StringBuffer buf = new StringBuffer(1000);
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		
		buf.append(now.format(dateFormat) + " ");
		buf.append(record.getLevel() + " ");
		buf.append(formatMessage(record));
		buf.append("\n");
		
		return buf.toString();
	}

}