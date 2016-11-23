package bl;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import bl.WaiterRequest.eType;
import logger.MyFilter;
import logger.MyFormatter;

public class Kitchen implements Runnable, WaiterRequestable {
	
    private int curruntDishServed;				//guarded by "this"
    private BlockingQueue<Order> orders;		//guarded by "this"
    private boolean isOpen;						//guarded by "this"
    private Logger theLogger;
    private String name;						//guarded by "this"	
    private static final int PREP_TIME = 2000;
	
    public Kitchen(int numberOfDishes, Logger theLogger) throws SecurityException, IOException {
    	
    	this.name = "Kitchen"; 
    	this.isOpen = true;    
		this.curruntDishServed = 0; 
		this.orders = new LinkedBlockingQueue<Order>();
		this.theLogger = theLogger;
		FileHandler theHandler = new FileHandler("Logs/Kitchen.xml");
		theHandler.setFormatter(new MyFormatter());
		theHandler.setFilter(new MyFilter("Kitchen"));
		theLogger.addHandler(theHandler);
	}

    /** getter and setters */
    
    public String getName() {
    	return name;
    }

	public synchronized int getcurruntDishServed() {
		return curruntDishServed;
	}

	public synchronized void setCurruntDish(int curruntDish) {
		this.curruntDishServed = curruntDish;
	}
	
	public synchronized void close() {
		this.isOpen = false;
	}

	@Override
	public String toString() {
		return "Kitchen [curruntDish=" + curruntDishServed + ", orders=" + orders + "]";
	}
	
	@Override
	public void run() {
		while(isOpen) {
			try {
				Order order = orders.take();
				makeDish(order);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	} 

    private void makeDish(Order order) throws InterruptedException {
    	if(order.getWaiter() == null) {
    		close();
    		return;
    	}
    	theLogger.log(Level.INFO, getName() + " is about to prepare dish for table " + order.getTable().getTableNum());
    	Thread.sleep(PREP_TIME);
		theLogger.log(Level.INFO, getName() + " finished making the dish for table " + order.getTable().getTableNum());
    	callWaiterForDish(order);
	}
    	
    private void callWaiterForDish(Order order) throws InterruptedException {
		WaiterRequest request = new WaiterRequest(eType.TAKE_DISH_TO_CUSTOMER, this, order);
		order.getWaiter().addRequest(request);
    }
    
    public void takeNewOrder(Order order) throws InterruptedException {
    	if(order.getWaiter() != null) {
        	theLogger.log(Level.INFO, getName() + " got the order from " +  order.getWaiter().getWaiterName() + " for table " + order.getTable().getCustomer().getCustomerName());
    	}
    	orders.put(order);	
    }
    


}
