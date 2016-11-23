package logger;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class MyFilter implements Filter {
	private String name;
	
	public MyFilter(String name) {
		this.name = name;
	}
	
	@Override
	public boolean isLoggable(LogRecord record) {
		String [] str = record.getMessage().split(" ");
		if(str[0].equals(name))
				return true;
		return false;
	}
}