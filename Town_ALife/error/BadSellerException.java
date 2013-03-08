package error;

import people.Corporation;
import people.Person;

public class BadSellerException extends Exception {
	private static final long serialVersionUID = 3186313975271130605L;
	public Person buyer, seller;
	public Corporation corp;
	
	public BadSellerException(Person buyer, Person seller,
			Corporation corporation) {
		this.buyer = buyer;
		this.seller = seller;
		corp = corporation;
	}
	

}
