package economy;

import people.Person;
import city.Bundle;
import city.City;
import city.Home;

public class Listing implements Comparable<Listing>{
	public Integer price;
	public Home property;
	public Person owner;
	
	public Listing(int price, Home property, Person owner){
		this.price = price;
		this.owner = owner;
		this.property = property;
	}
	
	public void buy(Bundle available, Person buyer){
		if (available.getValue() >= price){
			Bundle payment = available.worthAtLeast(price);
			if (payment != null){
				owner.income.insert(payment);
				owner.dropListing(this);
				buyer.newRealestate(property);
				City.listings.remove(this);
			}
		}
	}

	@Override
	public int compareTo(Listing o) {
		return price.compareTo(o.price);
	}
}
