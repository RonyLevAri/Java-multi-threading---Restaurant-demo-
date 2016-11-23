package bl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import annotation.WhileWaiting;
import blExceptions.IdException;
import blExceptions.NameException;
import logger.MyFilter;
import logger.MyFormatter;

public class Customer extends Thread implements WaiterRequestable {
	
	private static int idGenerator = 1;
	private static final Object idGeneratorlock = new Object();
	private static final int ACTION_TIME = 2000;
	private final int ID;
	private String name;
	private Waiter theWaiter;
	private WaiterRequest.eType status;
	private Table theTable;
	private String activity;
	private Logger theLogger;
	
	public Customer(String name, Logger theLogger) throws NameException, IdException, SecurityException, IOException {
		
		this.ID = setID();
		this.name = name + "_" + ID;
		this.activity = null;
		this.theLogger = theLogger;
		FileHandler theHandler = new FileHandler("Logs/Customers/" + getCustomerName() + ".xml");
		theHandler.setFilter(new MyFilter(getCustomerName()));
		theHandler.setFormatter(new MyFormatter());
		theLogger.addHandler(theHandler);
	}
	
	/** getters and setters */
	
	public int getID() {
		return ID;
	}
	
	public String getCustomerName() {
		return name;
	}
	
	public synchronized Waiter getWaiter() {
		return theWaiter;
	}
	
	public synchronized Table getTable() {
		return theTable;
	}
	
	public synchronized void setActivity(String whileWaitingActivity) {
		this.activity = whileWaitingActivity;	
	}
	
	public synchronized void setWaiter(Waiter theWaiter) {
		this.theWaiter = theWaiter;
	}
	
	public synchronized void setTable(Table theTable) {
		this.theTable = theTable;
	}
	
	private int setID() {
		synchronized (idGeneratorlock) {
			idGenerator++;
			return idGenerator-1;
		}
	}
	
	/** class methods */
	
	@Override
	public void run() {
			try {
				askForMenu();
				readMenu();
				orderDish();
				doActivity();
				eat();
				askForCheck();
				leaveRestaurant();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
		 
	private synchronized void askForMenu() throws InterruptedException {
		theLogger.log(Level.INFO, getCustomerName() + " wants menu from " + theWaiter.getWaiterName());
		status = WaiterRequest.eType.ASK_MENU;
		WaiterRequest request = new WaiterRequest(status, this);
		theWaiter.addRequest(request);
		theLogger.log(Level.INFO, getCustomerName() + " asked for menu from " + theWaiter.getWaiterName());
		theLogger.log(Level.INFO, getCustomerName() + " is waiting for menu");
		wait();
		theLogger.log(Level.INFO, getCustomerName() + " woke up by waiter " + theWaiter.getWaiterName() +  " - got menu");
	}
	
	private synchronized void readMenu() throws InterruptedException {
		theLogger.log(Level.INFO, getCustomerName() + " begin reading the menu");
		sleep(ACTION_TIME);
		theLogger.log(Level.INFO, getCustomerName() + " done reading the menu");
	}
		
	private synchronized void orderDish() throws InterruptedException {
		theLogger.log(Level.INFO, getCustomerName() + " wants to order from " + theWaiter.getWaiterName());
		status = WaiterRequest.eType.ORDER_DISH;
		WaiterRequest request = new WaiterRequest(status, this);
		theWaiter.addRequest(request);
		wait();
		theLogger.log(Level.INFO, getCustomerName() + " woke up by waiter " + theWaiter.getWaiterName() +  " - done ordering");
	}
	
	private synchronized void doActivity() throws InterruptedException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		theLogger.log(Level.INFO, getCustomerName() + " is about to wait to his meal");
		Class<? extends Customer> tempCustomer = this.getClass();
		Method[] allMethods = tempCustomer.getDeclaredMethods();
		Method toExecute = null;
		//find if config method exists
		for(int i = 0; i < allMethods.length; i ++) {
			if(allMethods[i].getName().equals(activity))
				toExecute = allMethods[i];
		}
		
		if(toExecute != null) {
			Annotation [] annotations = toExecute.getAnnotations();
			if(annotations != null) {
				for(int i = 0; i < annotations.length; i++) {
					if(annotations[i].annotationType().getName().endsWith("WhileWaiting"))
						toExecute.invoke(this);
				}
			} else {
					theLogger.log(Level.INFO, getCustomerName() + " is about to do nothing");
					wait();
					theLogger.log(Level.INFO, getCustomerName() + " woke up by waiter " + theWaiter.getWaiterName() + " - got his meal");
			}
		} else {
				theLogger.log(Level.INFO, getCustomerName() + " is about to do nothing");
				wait();
				theLogger.log(Level.INFO, getCustomerName() + " woke up by waiter " + theWaiter.getWaiterName() + " - got his meal");
		}
	}
	
	private synchronized void eat() throws InterruptedException {
		theLogger.log(Level.INFO, getCustomerName() + " starts to eat");
		sleep(ACTION_TIME);
		theLogger.log(Level.INFO, getCustomerName() + " finished to eat");
	}
	
	private synchronized void askForCheck() throws InterruptedException {
		theLogger.log(Level.INFO, getCustomerName() + " asks for check");
		status = WaiterRequest.eType.ASK_FOR_CHECK;
		WaiterRequest request = new WaiterRequest(status, this);
		theWaiter.addRequest(request);
		theLogger.log(Level.INFO, getCustomerName() + " is waiting to get his check");
		wait();
		theLogger.log(Level.INFO, getCustomerName() + " got his check");
	}
	
	public synchronized void leaveRestaurant() {
		theLogger.log(Level.INFO, getCustomerName() + " is leaving the restaurant. It was delicious");
	}
	
	/** Customer possible activities while waiting for meal */
	
	@WhileWaiting
	public void readNewsPaper() throws InterruptedException {
		theLogger.log(Level.INFO, getCustomerName() + " is about to read new paper");
		synchronized (this) {
			wait();
		}
		theLogger.log(Level.INFO, getCustomerName() + " woke up by waiter " + theWaiter.getWaiterName() + " - got his meal");
	}
	
	@WhileWaiting
	public void doHomework() throws InterruptedException {
		theLogger.log(Level.INFO, getCustomerName() + " is about to to do homework");
		synchronized (this) {
			wait();
		}
		theLogger.log(Level.INFO, getCustomerName() + " woke up by waiter " + theWaiter.getWaiterName() + " - got his meal");
	}
	
	@WhileWaiting
	public void talkOnThePhone() throws InterruptedException {
		theLogger.log(Level.INFO, getCustomerName() + " is about to to talk on the phone");
		synchronized (this) {
			wait();
		}
		theLogger.log(Level.INFO, getCustomerName() + " woke up by waiter " + theWaiter.getWaiterName() + " - got his meal");
	}
	
	@Override
	public String toString() {
		return "Customer [ID=" + ID + ", name=" + name + "]";
	}
}
