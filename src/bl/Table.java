package bl;

// thread safe class - all mutable variables are guarded by "this" 
// except for the tableNumGenerator that is a static variable guarded by "tableNumGeneratorlock"

public class Table {

	private final int TABLE_NUM;
	private static int tableNumGenerator = 1;
	private static final Object tableNumGeneratorlock = new Object();
	private Customer customer;
	private Check check;
	private Waiter waiter;
	
	public Table() {
		this.TABLE_NUM = setTableNum();
		this.customer = null;
		this.check = null;
		this.waiter = null;
	}
	
	/** getters and setters */
		
	public int getTableNum() {
		return TABLE_NUM;
	}

	public synchronized Customer getCustomer() {
		return customer;
	}
	
	public synchronized Check getCheck() {
		return check;
	}
	
	public synchronized Waiter getWaiter() {
		return waiter;
	}
	
	private int setTableNum() {
		synchronized (tableNumGeneratorlock) {
			tableNumGenerator++;
			return tableNumGenerator-1;
		}
	}

	public synchronized void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
	public synchronized void setWaiter(Waiter waiter) {
		this.waiter = waiter;
	}
	
	public synchronized void setCheck(Check check) {
		this.check = check;
	}
	
	/** class methods */
	
	@Override
	public synchronized String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Table #" + TABLE_NUM + " Details: \r\n");
		
		if(getWaiter() == null) {
			builder.append("\t is served by: no one \r\n");
		} else
			builder.append("\t is served by: " + getWaiter().getWaiterName() + "\r\n");
		
		if(getCustomer() == null) {
			builder.append("\t occupied by: no one \r\n");
		} else
			builder.append("\t occupied by: " + getCustomer().getCustomerName() + "\r\n");
		
		if(getCheck() == null) {
			builder.append("\t check number associated: no check \r\n");
		} else
			builder.append("\t check number associated: " + getCheck().getSerialNum());	
			
		return builder.toString();
	}	
}
