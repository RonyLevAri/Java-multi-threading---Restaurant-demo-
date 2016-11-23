package bl;

//thread safe class - all variables are final 
//except for the serialNumGenerator that is a static variable guarded by "orderNumGeneratorlock"

final public class Order {
	
	private static int serialNumGenerator = 1;
	private static final Object orderNumGeneratorlock = new Object();
	private final int SERIAL_NUM;
    private final Table TABLE;
    private final Waiter WAITER;
	
    public Order(Table table, Waiter theWaiter) {
		this.TABLE = table;	
		this.WAITER = theWaiter;
		this.SERIAL_NUM = setSerialNum();
	}

    /** getters and setters */
    
	public Table getTable() {
		return TABLE;
	}
	
	public Waiter getWaiter() {
		return WAITER;
	}
	
	public int getSerialNum() {
		return SERIAL_NUM;
	}

	private int setSerialNum() {
		synchronized (orderNumGeneratorlock) {
			serialNumGenerator++;
			return serialNumGenerator-1;
		}
	}
	
	/** class methods */
	
	@Override
	public synchronized String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Order #" + SERIAL_NUM + " Details: \r\n");
		
		if(getWaiter() == null) {
			builder.append("\t ordered by  : no one \r\n");
		} else
			builder.append("\t ordered by : " + getWaiter().getWaiterName() + "\r\n");
		
		if(getTable() == null) {
			builder.append("\t table number : no one \r\n");
		} else
			builder.append("\t table number : " + getTable().getTableNum() + "\r\n");
		
		return builder.toString();
	}	
}


