package bl;

import java.util.Date;

//thread safe class - all mutable variables are guarded by "this" 
//except for the serialNumGenerator that is a static variable guarded by "checkNumGeneratorlock"

public class Check {
	
	private static int serialNumGenerator = 1000;
	private static final Object checkNumGeneratorlock = new Object();
	private final int SERIAL_NUM;
	private final int TABLE_NUM;
	private final Date OPENED;
	private final Waiter WAITER;
	private Date closed;
	private int total;
	private boolean isPaid;

	public Check(int tableNum, Waiter waiter) {
		
		this.isPaid = false;
		this.WAITER = waiter;
		this.SERIAL_NUM = setSerialNum();
		this.TABLE_NUM = tableNum;
		this.OPENED = new Date();
		this.closed = null;
		this.total = 0;
	}

	/** getters and setters */
	
	public synchronized void updateTotal(int priceOfDish) {
		this.total += priceOfDish;
	}

	public Waiter getWaiter() {
		return WAITER;
	}

	public int getSerialNum() {
		return SERIAL_NUM;
	}

	public int getTableNum() {
		return TABLE_NUM;
	}

	public Date getOpened() {
		return OPENED;
	}
	
	public synchronized int getTotal() {
		return total;
	}
	
	public synchronized Date getClosed() {
		return closed;
	}
	
	public synchronized boolean isPaid() {
		return isPaid;
	}
	
	public synchronized void setTotal(float price) {
		this.total += price;
	}
	
	private int setSerialNum() {
		synchronized (checkNumGeneratorlock) {
			serialNumGenerator++;
			return serialNumGenerator-1;
		}
	}
	
	/** class methods */

	public synchronized void setDiscount(int discount) {
		total = ((100 - discount) / 100) * total;
	}

	public synchronized void close() {
		closed = new Date();
		isPaid = true;
	}
	
	@Override
	public synchronized String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Check #" + SERIAL_NUM + " Details: \r\n");
		
		if(getWaiter() == null) {
			builder.append("\t opened by: no one \r\n");
		} else
			builder.append("\t opened by: " + getWaiter().getWaiterName() + "\r\n");
		
		builder.append("\t on: " + OPENED + "\r\n");
		builder.append("\t table number: " + TABLE_NUM + "\r\n");
		builder.append("\t closed: " + this.getClosed() + "\r\n\r\n");
		builder.append("\t TOTAL: " + this.getTotal());
		return builder.toString();
	}

}


