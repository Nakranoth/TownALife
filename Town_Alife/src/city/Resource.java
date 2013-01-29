package city;
/**
 *	Wrapper for strong typing.
 *	Tracks how much of a resource is in a pool.
 */

public class Resource{
	String name;
	int amount;
	public Resource(String name, int amount){
		this.name = name;
		this.amount = amount;
	}
}