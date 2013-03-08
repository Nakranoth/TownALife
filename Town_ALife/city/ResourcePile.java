package city;
/**
 *	Wrapper for strong typing.
 *	Tracks how much of a resource is in a pool.
 */

public class ResourcePile{
	public static enum Resource{wood,stone,crops,metal,tools,crafts,goods,food};
	public Resource type;
	public int amount;
	public ResourcePile(Resource type, int amount){
		this.type = type;
		this.amount = amount;
	}
	public ResourcePile(ResourcePile have) {
		type = have.type;
		amount = have.amount;
	}
}