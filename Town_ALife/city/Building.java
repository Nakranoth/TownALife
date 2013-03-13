package city;


public abstract class Building {
	public int capacity;
	private Bundle cost;
	protected String name;
	public static enum BuildingType {CobblersHut,ForestryHut,Homestead}
	
	protected Building(Bundle cost, int capacity)
	{
		this.cost = cost;
		this.capacity = capacity;
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

	public abstract boolean decay();
}
