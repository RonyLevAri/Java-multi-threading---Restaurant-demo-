package bl;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import blExceptions.IdException;
import blExceptions.NameException;
import blExceptions.NoAvailableWaitersException;
import config.LoadConfiguration;

public class main {

	public static Logger theLogger = Logger.getLogger("myLogger");
	public static Scanner in = new Scanner(System.in);
	public static final int MAX_OPTIONS = 7;
	private static RestaurantManager theResaurant;

	public static void main(String[] args) {

		Lock lockRest = new ReentrantLock();
		Condition cond = lockRest.newCondition();
		LoadConfiguration loader;
		try {
			loader = new LoadConfiguration(lockRest, cond, theLogger);
			 theResaurant = loader.loadParametersFronXML();
			loader.initWaiters();
			loader.initCustomers();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NameException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IdException e) {
			e.printStackTrace();
		} catch (NoAvailableWaitersException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		boolean didNotRequestToClose = true;
		int choice = -1;
		while(didNotRequestToClose){
			showMenu();
			choice = readChoice(); 
			if(choice == 7)
				didNotRequestToClose = false;
			try {
				executeChoice(choice, theResaurant);
			} catch (SecurityException | IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("End of Program");
	}

	private static void showMenu() {
		System.out
				.println(" 1). open restaurant \n "
						+ "2). add new Customer \n "
						+ "3). add new Waiter \n "
						+ "4). show all waiting customers\n "
						+ "5). show all tables \n "
						+ "6). show resturant profit \n "
						+ "7). close resturant and show statctics");
	}
	
	private static int readChoice() {
		int choice = 0;
		
		while(true){
			try{
				choice = in.nextInt();
				if(choice >= 1 && choice <= MAX_OPTIONS){
					return choice;
				}
				else{
					System.out.println("There is no such option, try again...");
				}
			}catch(InputMismatchException e){
				in.next();
				System.out.println("Please enter only a number!, try again...");
			}	
		}
	}
	
	private static String readName() {
		String name = "";
		
		while(true){
			try{
				name = in.nextLine();
				if(!name.isEmpty()){
					return name;
				}
				else{
					System.out.println("Please enter a name...");
				}
			}catch(InputMismatchException e){
				in.next();
				System.out.println("Please enter a name...");
			}	
		}
	}
	
	private static void executeChoice(int choice, RestaurantManager theRest) throws SecurityException, IOException, InterruptedException {
		
		switch (choice){
		case 1:
			theResaurant.open();
			break;
		case 2: 
			addCustomer();
		case 3:
			addWaiter();
			break;
		case 4:
			showWaitingCustomers();
			break;
		case 5:
			showTables();
			break;
		case 6:
			showProfits(false);
			break;
		case 7:
			theResaurant.close();	
			break;
	}
	}

	private static void addCustomer() {
		String customerName = readName();
		try {
			theResaurant.addCustomer(new Customer(customerName, theLogger));
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NameException e) {
			e.printStackTrace();
		} catch (IdException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void showProfits(boolean b) {
		System.out.println(theResaurant.showStatistics());
	}

	private static void showTables() {
		System.out.println(theResaurant.showAllTables());
	}

	private static void showWaitingCustomers() {
		System.out.println(theResaurant.showWaitingCustomers());
	}

	private static void addWaiter() {
		String waiterName = readName();
		try {
			theResaurant.addWaiter(new Waiter(waiterName, theLogger, 4));
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NameException e) {
			e.printStackTrace();
		} catch (IdException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
