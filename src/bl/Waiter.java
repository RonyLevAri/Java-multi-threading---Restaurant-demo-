package bl;

import java.io.IOException;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import blExceptions.IdException;
import blExceptions.NameException;
import logger.MyFilter;
import logger.MyFormatter;

public class Waiter extends Thread {

	private static int idGenerator = 1;
	private static final Object idGeneratorlock = new Object();
	private static final int ACTION_TIME = 1000;
	private final int ID;
	private String name;
	private Vector<Table> tables;  //guarded by "this"
	private int numOfTableServed;  //guarded by "this"
	private final int MAX_TABLE_TO_SERVE; // final field - doesn't need to be gaurded
	private int currentNumTables;  //guarded by "this"
	private BlockingQueue<WaiterRequest> requests;  // blocking queue
	private boolean inShift;  // guarded by "this"
	private boolean stillWaitForCustomersToFinish; // guarded by "this"
	private Logger theLogger;
	private ShiftManager shiftManager;
	
	public Waiter(String name, Logger theLogger, int maxTableToServe) throws NameException, IdException, SecurityException, IOException {
		
		this.ID = setID();
		this.name = name + "_" + ID;
		this.tables = new Vector<Table>();
		this.numOfTableServed = 0;
		this.MAX_TABLE_TO_SERVE = maxTableToServe;
		this.currentNumTables = 0;
		//this.theRestaurant = theRestuarnt;
		this.shiftManager = null;
		this.requests = new LinkedBlockingQueue<>();
		this.inShift = false;
		this.stillWaitForCustomersToFinish = true;
		this.theLogger = theLogger;
		FileHandler theHandler = new FileHandler("Logs/Waiters/" + getWaiterName() + ".xml");
		theHandler.setFilter(new MyFilter(getWaiterName()));
		theHandler.setFormatter(new MyFormatter());
		theLogger.addHandler(theHandler);
	}
	
	/** getters and setters */
	
	public int getID() {
		return ID;
	}
	
	public String getWaiterName() {
		return name;
	}
	
	// return a copy of the table vector for thread safety
	public Vector<Table> getTables() {
		Vector<Table> tableList = new Vector<Table>();
		synchronized (this) {
			Collections.copy(tableList, tables);
		}
		return tableList;
	}
	
	public int getMaxTableToServe() {
		return MAX_TABLE_TO_SERVE;
	}
	
	public synchronized int getNumOfTableServed() {
		return numOfTableServed;
	}

	public synchronized int getCurrentNumTables() {
		return currentNumTables;
	}
	
	private int setID() {
		synchronized (idGeneratorlock) {
			idGenerator++;
			return idGenerator-1;
		}
	}
	
	public synchronized void setShiftManager(ShiftManager shiftManager) {
		this.shiftManager = shiftManager;
	}
	
	/** class methods */
	
	public void addRequest(WaiterRequest request) throws InterruptedException {
		requests.put(request);
	}
	
	public synchronized void addTable(Table newTableToServe) {
		tables.addElement(newTableToServe);
		currentNumTables++;
		numOfTableServed++;
	}
	
	@Override
	public void run() {
		startShift();
		while(inShift || stillWaitForCustomersToFinish) {
			try {
				WaiterRequest request = requests.take();
				serve(request);
				synchronized(this) {
					if(!inShift && tables.size() == 0)
						stillWaitForCustomersToFinish = false;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void startShift() {
		theLogger.log(Level.INFO, getWaiterName() + " started the shift");
		inShift = true;
	}
	
	private void serve(WaiterRequest request) throws InterruptedException {
		
		switch(request.getRequest()) {	
		case 0:
			serveMenu(request);
			break;
		case 1:
			takeOrder(request);
			break;
		case 2:
			serveCheck(request);
			break;
		case 3:
			serveDish(request);
			break;
		case 4:
			leaveShift(false);
		default:
			break;
		} 
	}

	private void serveMenu(WaiterRequest request) throws InterruptedException {
		Customer theCustomer = (Customer) request.getRequester();
		theLogger.log(Level.INFO, getWaiterName() + " serving menu to " + theCustomer.getCustomerName());
		sleep(ACTION_TIME);
		synchronized(theCustomer) {
			theCustomer.notifyAll();
		}
		theLogger.log(Level.INFO, getWaiterName() + " done serving menu to " + theCustomer.getCustomerName());
	}
	
	private void takeOrder(WaiterRequest request) throws InterruptedException {
		Customer theCustomer = (Customer) request.getRequester();
		theLogger.log(Level.INFO, getWaiterName() + " taking order from " + theCustomer.getCustomerName());
		sleep(ACTION_TIME);
		theLogger.log(Level.INFO, getWaiterName() + " finished taking order from " + theCustomer.getCustomerName());
		synchronized(theCustomer) {
			theCustomer.notifyAll();
		}
		openCheck(theCustomer);
		theLogger.log(Level.INFO, getWaiterName() + " done openening check and taking order from " + theCustomer.getCustomerName());
	}
	
	private void openCheck(Customer theCustomer) throws InterruptedException {
		theLogger.log(Level.INFO, getWaiterName() + " is opening a check for " + theCustomer.getCustomerName());
		shiftManager.addCheck(theCustomer.getTable(), this);
		shiftManager.deliverOrderToKitchen(new Order(theCustomer.getTable(), this));
	}
	
	private void serveDish(WaiterRequest request) throws InterruptedException {
		Customer theCustomer = request.getOrder().getTable().getCustomer();
		theLogger.log(Level.INFO, getWaiterName() + " serving dish to " + theCustomer.getCustomerName());
		sleep(ACTION_TIME);
		synchronized(theCustomer) {
			theCustomer.notifyAll();
		}
	}

	private void serveCheck(WaiterRequest request) throws InterruptedException {
		Customer theCustomer = (Customer) request.getRequester();
		theLogger.log(Level.INFO, getWaiterName() + " serving check for " + theCustomer.getCustomerName());
		sleep(ACTION_TIME);
		synchronized(theCustomer) {
			theCustomer.notifyAll();
		}
		closeCheck(theCustomer);	
		theLogger.log(Level.INFO, getWaiterName() + " is done serving and closing check for " + theCustomer.getCustomerName());
		if(tables.size() == 0  && MAX_TABLE_TO_SERVE == numOfTableServed) {
			leaveShift(true);
		}	
	}
		
	private void closeCheck(Customer theCustomer) throws InterruptedException {
		theLogger.log(Level.INFO, getWaiterName() + " is closing check for " + theCustomer.getCustomerName());
		theCustomer.getTable().getCheck().close();
		synchronized(this) {
			tables.remove(theCustomer.getTable());
			currentNumTables--;
		}
		shiftManager.closeCheck(theCustomer);
	}
	
	private void leaveShift(boolean calledFromInside) throws InterruptedException {
		theLogger.log(Level.INFO, getWaiterName() + " served: " + numOfTableServed + " - leaving shift");
		inShift = false;
		if(calledFromInside){
			stillWaitForCustomersToFinish = false;
		}
		shiftManager.removeWaiterFromShift(this);	
	}

	@Override
	public String toString() {
		return "Waiter [ID=" + ID + ", name=" + name + "]";
	}

	
}
