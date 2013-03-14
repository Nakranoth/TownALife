package city;
/**
 *	Wrapper for strong typing.
 *	Tracks how much of a resource is in a pool.
 */

public class ResourcePile implements Comparable<ResourcePile>{
	public static enum Resource{wood,stone,crops,metal,tools,crafts,goods,food};
	public Resource type;
	public Long amount;
	public ResourcePile(Resource type, long amount){
		this.type = type;
		this.amount = amount;
	}
	public ResourcePile(ResourcePile have) {
		type = have.type;
		amount = have.amount;
	}
	@Override
	public int compareTo(ResourcePile o) {
		return -type.compareTo(type);
	}
	public double getValue() {
		return amount * City.economy.prices[type.ordinal()];
	}
	
	public String toString(){
		return type.name() + "," + amount;
	}
}