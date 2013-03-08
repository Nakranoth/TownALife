package city;

import java.util.ArrayList;

import people.Person;

public abstract class Building {
	private int capacity;
	ArrayList<Person> occupant;	//used to determine outputs.
	private Bundle cost;
	protected String name;
	public boolean derilict = false;
	public static enum BuildingType {CobblersHut,ForestryHut,Homestead}
	
	protected Building(Bundle cost, int capacity)
	{
		this.cost = cost;
		this.capacity = capacity;
	}
	
	public void trimToCapacity()
	{
		while(occupant.size() > capacity)
		{
			//TODO: Remove this from person's pool.
			occupant.remove(occupant.size() - 1);	//trim last person from list until small enough
			System.err.println("Building Over CAP! Logic has failed!");
		}
	}

	public String toString()
	{
		return name;
	}

	public Bundle getCost() {
		return cost;
	}
	
	public int getCapacity(){
		return capacity;
	}

}
