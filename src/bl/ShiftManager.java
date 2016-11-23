package bl;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShiftManager extends Thread implements WaiterRequestable {

	private final LinkedList<Waiter> workingWaiters;
	private boolean isManaging;
	private int maxWaitersInShift;
	private final LinkedList<Waiter> waitingWaitersQueue;
	private Logger theLogger;
	private Host theHost;
	private final RestaurantManager theRestaurant;
	
	public ShiftManager(RestaurantManager theRestaurant, LinkedList<Waiter> waitingWaitersQueue, int maxWiatersInShift, Logger theLogger) {
		
		this.theRestaurant = theRestaurant;
		this.waitingWaitersQueue = waitingWaitersQueue;
		this.workingWaiters = new LinkedList<Waiter>();
		this.theLogger = theLogger;
		this.theHost = null;
		this.isManaging = false;
		this.maxWaitersInShift = maxWiatersInShift;
	}
	
	/** getters and setters */
	
	public void setHost(Host theHost) {
		this.theHost = theHost;
	}
	
	public void setIsManaging(boolean isManaging) {
		this.isManaging = isManaging;
	}
	
	public synchronized void setMaxWaitersInShift(int maxWaitersInShift) {
		this.maxWaitersInShift = maxWaitersInShift;
	}
	
	/** run and managing methods */ 
	
	@Override
	public void run() {
		isManaging = true;
		theLogger.log(Level.INFO, "shift manager thread running!!!");
		while(isManaging) {
			try {
			synchronized(this) {
				while(waitingWaitersQueue.isEmpty() || workingWaiters.size() >= maxWaitersInShift){
					theLogger.log(Level.INFO, "host is about to wait");
					wait();			
					theLogger.log(Level.INFO, "shift manage woke up in run");
					if(!isManaging) {
						break;
					}
				}
			}
			theLogger.log(Level.INFO, "shift manage is about to manage");
			manage();
			theLogger.log(Level.INFO, "shift manage after manage");
			} catch (InterruptedException e) {
				isManaging = false;
				e.printStackTrace();
			}
		}// release all waiters
		theLogger.log(Level.INFO, "shift manager is calling all working waiters to finish working");
		try {
			insertPoisonPillToWorkingWaiters();
			for(int i = 0; i < workingWaiters.size(); i++) {
				workingWaiters.get(i).join();
			}
			theLogger.log(Level.INFO, "shift manager dismisses waiting waiters");
			for(int i = 0; i < waitingWaitersQueue.size(); i++) {
				Waiter waiter = waitingWaitersQueue.pop();
				theLogger.log(Level.INFO, "bye bye waiter " + waiter.getWaiterName());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		theLogger.log(Level.INFO, "shift manager finished doing it's jub - leaving run method");
	}
		
	
	private synchronized void insertPoisonPillToWorkingWaiters() throws InterruptedException {
		for(int i = 0; i < workingWaiters.size(); i++) {
			WaiterRequest request = new WaiterRequest(WaiterRequest.eType.POISON_PILL, this);
			workingWaiters.get(i).addRequest(request);
		}
	}

	private void manage() {
		Waiter waiterEnteringShift;
		if(!isManaging){
			return;
		}
		synchronized(this) {
				waiterEnteringShift = waitingWaitersQueue.pop();
				workingWaiters.add(waiterEnteringShift);
				theLogger.log(Level.INFO, "new waiter " + waiterEnteringShift.getWaiterName() + " is entering the shift ");
				waiterEnteringShift.start();			
					
		}
		synchronized(theHost) {
			theLogger.log(Level.INFO, "shift manager is done managing - notifies host");
			theHost.notifyAll();
		}
	}
	
	public synchronized void removeWaiterFromShift(Waiter waiter) throws InterruptedException {
	
		for(int i = 0; i < workingWaiters.size(); i++) {
			if(waiter.equals(workingWaiters.get(i))){
				workingWaiters.remove(i);
				theLogger.log(Level.INFO, "shift manager said good bye to " + waiter.getWaiterName());
				return;
			}
		}
		notifyAll();
	}
	
	// find the waiter serving minimum tables at the moment
	public synchronized Waiter allocateWaiter() {
		
		Waiter waiter = null;
		int minimumTable = -1;
		int waiterIndex = -1;
		for (int i = 0; i < workingWaiters.size(); i++) {
			
			if (workingWaiters.get(i).getNumOfTableServed() < workingWaiters
					.get(i).getMaxTableToServe()) {
				if (minimumTable == -1
						|| workingWaiters.get(i).getCurrentNumTables() < minimumTable) {
					waiterIndex = i;
					minimumTable = workingWaiters.get(i).getCurrentNumTables();
				}
			}
		}
		if (waiterIndex != -1) {
			waiter = workingWaiters.get(waiterIndex);
		}
		
		return waiter;
	}

	public void deliverOrderToKitchen(Order order) throws InterruptedException {
		theRestaurant.deliverOrderToKitchen(order);
	}

	public void addCheck(Table table, Waiter waiter) {
		theRestaurant.addCheck(table, waiter);
	}

	// customer left - notify host and shift manager
	public void closeCheck(Customer theCustomer) {
		theHost.updateTableQueues(theCustomer.getTable().getTableNum());
		theRestaurant.closeCheck(theCustomer.getTable().getCheck(), theCustomer.getCustomerName());
		synchronized(this) {
			this.notifyAll();
		}
		synchronized(theHost) {
			theHost.notifyAll();
		}
	}
}
