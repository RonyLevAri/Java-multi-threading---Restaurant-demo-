package bl;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import blExceptions.NameException;
import logger.MyFormatter;

public class RestaurantManager {

	private int maxCustomers;
	private int numberOfTables;
	private int numberOfDishes;
	private int maxWaitersInShift;
	private String name;
	private int currentCustomerInRes;
	private int numCustomerRequestToEnter;
	private int numCustomerServed;
	private LinkedList<Waiter> waitingWaitersQueue;
	private LinkedList<Customer> waitingCustomersQueue;
	private LinkedList<Table> tables;
	private Vector<Check> closedChecks;
	private Vector<Check> checks;
	private Kitchen theKitchen;
	private boolean isOpen;
	private Logger theLogger;
	private Host theHost;
	private ShiftManager shiftManager;

	public RestaurantManager(Lock restLock, Condition cond, Logger theLogger,
			String name, int maxCustomers, int numberOfTables,
			int maxWaitersInShift, int numberOfDishes) throws NameException, SecurityException, IOException {
		this.theLogger = theLogger;
		setName(name);
		this.numCustomerRequestToEnter = 0;
		this.maxCustomers = maxCustomers;
		this.numberOfTables = numberOfTables;
		this.maxWaitersInShift = maxWaitersInShift;
		this.numberOfDishes = numberOfDishes;
		this.waitingWaitersQueue = new LinkedList<Waiter>();
		this.waitingCustomersQueue = new LinkedList<Customer>();
		this.tables = new LinkedList<Table>();
		this.closedChecks = new Vector<Check>();
		this.checks = new Vector<Check>();
		this.currentCustomerInRes = 0;
		this.numCustomerServed = 0;
		this.theHost = null;
		this.shiftManager = null;
		initTables();
	}

	/** ****************** setters and getters ************************** */
	
	private void initTables() {
		for (int i = 0; i < numberOfTables; i++) {
			tables.add(new Table());
		}
	}
	
	public void setName(String name) throws NameException {
		if (name == null || name.isEmpty())
			throw new NameException();
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setNumberOfDishes(int numberOfDishes)  {
		if(numberOfDishes <= 0)
			throw new IllegalArgumentException();
		this.numberOfDishes = numberOfDishes;
	}
	
	public synchronized boolean isOpen() {
		return isOpen;
	}
	
	public synchronized int getMaxCustomers() {
		return maxCustomers;
	}

	public synchronized void setMaxCustomers(int maxCustomers) {
		this.maxCustomers = maxCustomers;
	}

	public synchronized int getNumberOfTables() {
		return numberOfTables;
	}

	public synchronized void setNumberOfTables(int numberOfTables) {
		this.numberOfTables = numberOfTables;
	}

	public synchronized int getMaxWaitersInShift() {
		return maxWaitersInShift;
	}

	public synchronized void setMaxWaitersInShift(int maxWaitersInShift) {
		this.maxWaitersInShift = maxWaitersInShift;
	}

	public synchronized int getCurrentCustomerInRes() {
		return currentCustomerInRes;
	}

	public synchronized void setCurrentCustomerInRes(int currentCustomerInRes) {
		this.currentCustomerInRes = currentCustomerInRes;
	}

	public synchronized int getNumCustomerServed() {
		return numCustomerServed;
	}

	public synchronized void setNumCustomerServed(int numCustomerServed) {
		this.numCustomerServed = numCustomerServed;
	}

	public synchronized int getNumberOfDishes() {
		return numberOfDishes;
	}

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	/** ****************** system functions ************************** */
	
	public void addWaiter(Waiter waiter) throws InterruptedException {
		if(shiftManager != null){
			synchronized(shiftManager){
				waitingWaitersQueue.add(waiter);
				theLogger.log(Level.INFO, "the new waiter added to queue while shift manager is working - shift manager is being notified");
				waiter.setShiftManager(shiftManager);
				shiftManager.notifyAll();
			}
		}else {
			waitingWaitersQueue.add(waiter);
			theLogger.log(Level.INFO, waiter.getWaiterName() + " has entered the waiters waiting queuer ");
		}
	}

	public void addCustomer(Customer customer) throws InterruptedException {
		synchronized(this) {
			numCustomerRequestToEnter++;
			if(numCustomerRequestToEnter > maxCustomers || numCustomerRequestToEnter > numberOfDishes) {		
				theLogger.log(Level.INFO," restaurant finished it'"
						+ "s capacity for today - customer did not enter queue");
				return;
			}
		}
		if(theHost != null) {
			synchronized(theHost){
				waitingCustomersQueue.add(customer);
				theLogger.log(Level.INFO, "the new ustomer added to queue while host is working - host is being notified");
				theHost.notifyAll();
			}
		}else {
			waitingCustomersQueue.add(customer);
			theLogger.log(Level.INFO, customer.getCustomerName() + " has entered the customer waiting queuer ");
		}			
	}
	
	public synchronized void customerEntered() {
		currentCustomerInRes++;
		numCustomerServed++;
	}
		
	public void deliverOrderToKitchen(Order order) throws InterruptedException  {
			theKitchen.takeNewOrder(order);
	}

	public void addCheck(Table table, Waiter waiter) {
		
		Check newCheck = new Check(table.getTableNum(), waiter);
		newCheck.updateTotal(100);
		table.setCheck(newCheck);
		synchronized(this) {
			checks.add(newCheck);			
		}
		theLogger.log(Level.INFO, "restaurant added adding new check from waiter " + waiter.getWaiterName());
	}

	public synchronized void closeCheck(Check theCheck, String customerName)  {
		
		for(int i = 0; i < checks.size(); i++) {
			if(theCheck.equals(checks.get(i))) {
				checks.remove(i);
				break;
			}
		}
		currentCustomerInRes--;
		closedChecks.addElement(theCheck);
		theLogger.log(Level.INFO, "Restaurant is closind " + theCheck.getWaiter().getWaiterName() + " check for customer " + name);	
	}

	public String showAllTables() {
		StringBuffer buf  = new StringBuffer();

		if(theHost != null) {
			buf.append("Occupied Tables: \n");
			buf.append(theHost.showTakenTables());
			buf.append("\n");
			buf.append("Available Tables: \n");
			buf.append(theHost.showAvailableTables());
		}
		else {
			buf.append("All tables are available: \n");
			synchronized(this) {
				Iterator<Table> it = tables.iterator();
				while(it.hasNext())
					buf.append(it.next() + "\n");
			}
		}
		return buf.toString();	
	}
	
	public String showWaitingCustomers() {
		StringBuffer buf = new StringBuffer();
		if(theHost != null) {
			synchronized(theHost) {
				Iterator<Customer> it = waitingCustomersQueue.iterator();
				while(it.hasNext())
					buf.append(it.next().getCustomerName() + "\n");
			}
		}
		else {
			Iterator<Customer> it = waitingCustomersQueue.iterator();
			while(it.hasNext())
				buf.append(it.next().getCustomerName() + "\n");
		}

		return buf.toString();
	}

	public synchronized String showStatistics() {
		StringBuffer buf = new StringBuffer();
		buf.append("number of dishes " + numberOfDishes+ "\n");
		buf.append("max customer " + maxCustomers + "\n");
		buf.append("closed checks: \n");
		Iterator<Check> it = closedChecks.iterator();
		while(it.hasNext())
			buf.append(it.next().toString() + "\n");
		return buf.toString();
	}

	public void close() throws InterruptedException {
		synchronized(this) {
			if(!isOpen) {
				System.out.println("The restaurant is already closed");
				return;
			}
		}
		isOpen = false;
		synchronized (shiftManager) {
			shiftManager.setIsManaging(false);
			shiftManager.notifyAll();	
		}
		synchronized(theHost) {
			theHost.setIsHosting(false);
			theHost.notifyAll();	
		}
		theHost.join();
		shiftManager.join();
		theKitchen.takeNewOrder(new Order(null, null));
	}
	
	public void noAvailableWaitersAlert() {
		System.out.println("no waiter available!!");
	}
	
	public void open() throws SecurityException, IOException {
		FileHandler theHandler = new FileHandler("Logs/" + getName() + ".xml");
		theHandler.setFormatter(new MyFormatter());
		theLogger.addHandler(theHandler);
		theLogger.setUseParentHandlers(false);
		startKitchen(numberOfDishes, theLogger);
		isOpen = true;
		theHost = new Host(this, waitingCustomersQueue, tables, theLogger);
		shiftManager = new ShiftManager(this, waitingWaitersQueue, maxWaitersInShift, theLogger);
		setShiftManagerForWaitingWaiters();
		theHost.setShiftManager(shiftManager);
		shiftManager.setHost(theHost);
		shiftManager.start();
		theHost.start();
	}

	private void setShiftManagerForWaitingWaiters() {
		Iterator<Waiter> it = waitingWaitersQueue.iterator();
		while(it.hasNext()) {
			 it.next().setShiftManager(shiftManager);
		}
	}

	private void startKitchen(int numberOfDishes, Logger theLogger) throws SecurityException, IOException {
		this.theKitchen = new Kitchen(numberOfDishes,theLogger);
		Thread t = new Thread(theKitchen);
		t.start();
	}



}
