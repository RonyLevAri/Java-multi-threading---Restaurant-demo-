package config;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import bl.Customer;
import bl.RestaurantManager;
import bl.Waiter;
import blExceptions.IdException;
import blExceptions.NameException;
import blExceptions.NoAvailableWaitersException;

public class LoadConfiguration {

	private File xmlFile;
	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	private Document doc;
	private RestaurantManager theRestautant;
	private Logger theLogger;
	private Condition cond;
	private Lock lockRest;

	public LoadConfiguration(Lock lockRest, Condition cond, Logger theLogger) throws SecurityException, NameException {

		try {
			xmlFile = new File("src/ConfigFile.xml");
			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(xmlFile);
			// New instance of not config resturant
			this.theLogger = theLogger;
			this.lockRest = lockRest;
			this.cond = cond;
			// initRestaurant();
			// this.theRestautant = new RestaurantManager(lockRest, cond,
			// this.theLogger);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
	}

	public RestaurantManager loadParametersFronXML() throws SecurityException,
			NameException, NoAvailableWaitersException, IOException, IdException,
			InterruptedException {
		initRestaurant();
		//initWaiters();
		//initCustomers();
		return theRestautant;
	}

	public void initRestaurant() throws NameException, SecurityException,	IOException {
		NodeList nList = doc.getElementsByTagName("Resturant");
		for (int i = 0; i < nList.getLength(); i++) {
			Node resturantNode = nList.item(i);
			String name = resturantNode.getAttributes().getNamedItem("name")
					.getNodeValue();
			// theRestautant.setName(name);
			int maxCustomers = Integer.parseInt(resturantNode.getAttributes()
					.getNamedItem("maxCustomerPerDay").getNodeValue());
			// theRestautant.setMaxCustomers(maxCustomers);
			int numberOfTables = Integer.parseInt(resturantNode.getAttributes()
					.getNamedItem("numberOfTables").getNodeValue());
			// theRestautant.setNumberOfTables(numberOfTables);
			// initTables(numberOfTables);
			int maxWaitersInShift = Integer.parseInt(resturantNode
					.getAttributes().getNamedItem("maxWaiterInShift")
					.getNodeValue());
			int numberOfDishes = Integer.parseInt(resturantNode.getAttributes()
					.getNamedItem("numberOfDishes").getNodeValue());
			// theRestautant.setNumberOfDishes(numberOfDishes);
			// theRestautant.setNumberOfWaitersInShift(numberOfWaitersInShift);

			this.theRestautant = new RestaurantManager(lockRest, cond,
					this.theLogger, name, maxCustomers, numberOfTables,
					maxWaitersInShift, numberOfDishes);
			//theRestautant.open();
			//return theRestautant;
		}
		//return theRestautant;
	}

	public void initWaiters() throws NameException, IdException,
			SecurityException, IOException, InterruptedException {
		NodeList nList = doc.getElementsByTagName("Waiters");
		Node waitersNode = nList.item(0);
		int maxTableToServe = Integer.parseInt(waitersNode.getAttributes()
				.getNamedItem("maxCustomersPerWaiter").getNodeValue());
		int numberOfWaiters = Integer.parseInt(waitersNode.getAttributes()
				.getNamedItem("numberOfWaitres").getNodeValue());
		for (int i = 0; i <= numberOfWaiters; i++) {
			Waiter theWaiter = new Waiter("Waiter", theLogger, maxTableToServe);
			//theWaiter.setNumberOfTablesToServe(maxTableToServe);
			theRestautant.addWaiter(theWaiter);
		}
	}

	public void initCustomers() throws NameException, IdException,
			NoAvailableWaitersException, InterruptedException,
			SecurityException, IOException {
		NodeList nList = doc.getElementsByTagName("customer");
		for (int i = 0; i < nList.getLength(); i++) {
			Node customerNode = nList.item(i);
			String name = customerNode.getAttributes().getNamedItem("name")
					.getNodeValue();
			Customer theCustomer = new Customer(name, theLogger);
			String whileWaitingActivity = customerNode.getAttributes()
					.getNamedItem("whileWaiting").getNodeValue();
			theCustomer.setActivity(whileWaitingActivity);
			theRestautant.addCustomer(theCustomer);
		}
	}
}
