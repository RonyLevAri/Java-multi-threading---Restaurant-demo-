package bl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Host extends Thread {

	private final LinkedList<Customer> waitingCustomersQueue;
	private final LinkedList<Table> avaiableTables;
	private final LinkedList<Table> occupiedTables;
	private ShiftManager shiftManeger;
	private boolean isHosting;
	private final RestaurantManager theRestaurant;
	private Logger theLogger;

	public Host(RestaurantManager theRestaurant, LinkedList<Customer> waitingCustomerQueue,
			LinkedList<Table> allRestTables, Logger theLogger) {
		this.theRestaurant = theRestaurant;
		this.shiftManeger = null;
		this.waitingCustomersQueue = waitingCustomerQueue;
		this.avaiableTables = allRestTables;
		this.theLogger = theLogger;
		this.occupiedTables = new LinkedList<Table>();
		this.isHosting = false;
	}
	
	/** getters and setters */

	public void setShiftManager(ShiftManager shiftManager) {
		this.shiftManeger = shiftManager;
	}

	public synchronized void setIsHosting(boolean isHosting) {
		this.isHosting = isHosting;
	}
	
	public synchronized String showTakenTables() {
		StringBuffer buf = new StringBuffer();
		Iterator<Table> it = occupiedTables.iterator();
		while (it.hasNext())
			buf.append(it.next() + "\n");
		return buf.toString();
	}

	public synchronized String showAvailableTables() {
		StringBuffer buf = new StringBuffer();
		Iterator<Table> it = avaiableTables.iterator();
		while (it.hasNext())
			buf.append(it.next() + "\n");
		return buf.toString();
	}

	public synchronized void updateTableQueues(int tableNum) {
		for (int i = 0; i < occupiedTables.size(); i++) {
			if (occupiedTables.get(i).getTableNum() == tableNum) {
				avaiableTables.add(occupiedTables.get(i));
				occupiedTables.remove(i);
				break;
			}
		}
		this.notifyAll();
		synchronized (shiftManeger) {
			shiftManeger.notifyAll();
		}
	}
	
	/** run and managing methods */
	
	@Override
	public void run() {
		isHosting = true;
		theLogger.log(Level.INFO, "host thread running!!!");
		while (isHosting) {
			try {
				synchronized (this) {
					while (waitingCustomersQueue.isEmpty() || avaiableTables.size() == 0) {
						theLogger.log(Level.INFO, "host is about to wait");
						wait();
						theLogger.log(Level.INFO, "host woke up in run");
						if (!isHosting) {
							break;
						}
					}
				}
				theLogger.log(Level.INFO, "host is about to manage");
				manage();
				theLogger.log(Level.INFO, "host after manage");

			} catch (InterruptedException e) {
				isHosting = false;
				e.printStackTrace();
			}
		} // release all waiting customers
		theLogger.log(Level.INFO, "host is saying goodbye to any waiting customer");
		synchronized (this) {
			for (int i = 0; i < waitingCustomersQueue.size(); i++) {
				Customer customer = waitingCustomersQueue.pop();
				theLogger.log(Level.INFO, "bye bye customer" + customer.getCustomerName());
			}
		}
		theLogger.log(Level.INFO, "host finished it's job - leaving run method");
	}

	private synchronized void manage() throws InterruptedException {

		Waiter availableWaiter;
		Table availableTable;
		Customer enteringCustomer;
		boolean gotWaiter;
		if (!isHosting) {
			return;
		}
		do {
			availableWaiter = shiftManeger.allocateWaiter();
			if (availableWaiter == null) {
				gotWaiter = false;
				theRestaurant.noAvailableWaitersAlert();
				theLogger.log(Level.INFO, "host could not find available waiter - is waiting");
				wait();
				theLogger.log(Level.INFO, "host woke up to try find available waiter");
			} else
				gotWaiter = true;
		} while (!gotWaiter && isHosting);

		if (isHosting) {
			availableTable = allocateTable();
			enteringCustomer = waitingCustomersQueue.pop();
			theLogger.log(Level.INFO, "waiter " + availableWaiter.getWaiterName() + " was allocated to serve "
					+ enteringCustomer.getCustomerName());
			theLogger.log(Level.INFO, "table " + availableTable.getTableNum() + " was allocated for "
					+ enteringCustomer.getCustomerName());
			availableTable.setWaiter(availableWaiter);
			availableTable.setCustomer(enteringCustomer);
			availableWaiter.addTable(availableTable);
			enteringCustomer.setTable(availableTable);
			enteringCustomer.setWaiter(availableWaiter);
			theRestaurant.customerEntered();
			enteringCustomer.start();
			theLogger.log(Level.INFO, enteringCustomer.getCustomerName() + " thread has started");
		}
	}

	private Table allocateTable() {
		synchronized (avaiableTables) {
			if (!avaiableTables.isEmpty()) {
				Table t = avaiableTables.pop();
				occupiedTables.add(t);
				return t;
			}
		}
		return null;
	}
}
