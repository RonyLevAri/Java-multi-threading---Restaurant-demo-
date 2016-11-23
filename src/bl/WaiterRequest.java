package bl;

// Thread safe class - each request is created by one thread and is accessed by one thread only
public class WaiterRequest {
	
	public enum eType {ASK_MENU, ORDER_DISH, ASK_FOR_CHECK, TAKE_DISH_TO_CUSTOMER, POISON_PILL};

	private final eType request;
	private final WaiterRequestable requester;
	private Order order;
		
	public WaiterRequest(eType type, WaiterRequestable requester) {
		this.request = type;
		this.requester = requester;
		this.order = null;
	}
	
	public WaiterRequest(eType type, WaiterRequestable requester, Order order) {
		this(type, requester);
		this.order = order;
	}
	
	/** getters and setters */
	
	public WaiterRequestable getRequester() {
		return requester;
	}
	
	public int getRequest() {
		return request.ordinal();
	}
	
	public Order getOrder() {
		return order;
	}

	@Override
	public String toString() {
		return "waiteRequest: requester: " + requester + ", request: " + request + "]";
	}
	
	
}
