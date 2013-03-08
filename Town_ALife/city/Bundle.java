package city;

import java.util.ArrayList;
import java.util.Iterator;

import city.ResourcePile.Resource;
import economy.Economy;

/**
 * Collection of ResourcePiles.
 * Is iterable, returning ResourcePiles
 * @author Nathan Killeen
 */
public class Bundle implements Iterable<ResourcePile>{
	public ArrayList<ResourcePile> contents = new ArrayList<ResourcePile>();
	private static Economy economy = City.economy;
	
	/**
	 * Conversion from pile array.
	 * Used primarily for costs. 
	 */
	public Bundle(ResourcePile[] pileSet){
		for(ResourcePile pile: pileSet){
			contents.add(pile);
		}
	}

	/**
	 * Bundle initialized to contain one ResourcePile
	 */
	public Bundle(ResourcePile resourcePile) {
		contents.add(resourcePile);
	}

	public Bundle() {//seriously? I need to add this?
	}

	/**
	 * Intelligently adds the inserted pile to the bundle.
	 */
	public void insert(ResourcePile inserted) {
		for (ResourcePile curr:contents){
			if (curr.type == inserted.type){
				curr.amount += inserted.amount;
				return;
			}
		}
		contents.add(inserted);
	}
	
	/**
	 * Intelligently merges inserted into this bundle.
	 */
	public void insert(Bundle inserted){
		for(ResourcePile curr:inserted){
			insert(curr);
		}
	}

	/**
	 * Returns the whole number of times this fits completely into divisor.
	 */
	public double over(Bundle divisor) {
		double result = Float.MAX_VALUE;
		for(ResourcePile divisorPile:divisor.contents){
			double match = 0;
			for(ResourcePile curr:contents){
				if (curr.type == divisorPile.type){
					match = (double)curr.amount / (double)divisorPile.amount;
					break;
				}
			}
			if (match < result) result = match;
		}
		return result;
	}

	/**
	 * Returns a new bundle representing this bundle, split divisor ways 
	 */
	public Bundle over(int divisor) {
		Bundle result = new Bundle();
		for(ResourcePile pile:contents){
			result.insert(new ResourcePile(pile.type, pile.amount / divisor));
		}
		return result;
	}
	
	/**
	 * Returns a new bundle representing this bundle, split divisor ways
	 */
	public Bundle over(double divisor) {
		Bundle result = new Bundle();
		for(ResourcePile pile:contents){
			result.insert(new ResourcePile(pile.type, (int)(pile.amount / divisor)));
		}
		return result;
	}
	
	/**
	 * Returns the shared resources between this and other.
	 */
	public Bundle intersect(Bundle other){
		Bundle intersection = new Bundle();
		
		for(ResourcePile otherPile:other){
			for(ResourcePile thisPile:this){
				if(otherPile.type == thisPile.type){
					intersection.insert(new ResourcePile(thisPile.type, Math.min(thisPile.amount, otherPile.amount)));
					break;
				}
			}
		}
		
		return intersection;
	}
	
	@Override
	public Iterator<ResourcePile> iterator() {
		return contents.iterator();
	}

	/**
	 * Returns true iff goal has no ResourcePiles larger than my own.
	 */
	public boolean hasAtLeast(Bundle goal) {
		for(ResourcePile requiredType:goal){
			boolean enoughType = false;
			for(ResourcePile whatIHave:contents){
				if (whatIHave.type == requiredType.type){
					if(whatIHave.amount < requiredType.amount) return false;
					enoughType = true;
					break;
				}
			}
			if(!enoughType) return false;
		}
		
		return true;
	}

	/**
	 * Returns a bundle representing this, if you removed goal from it.
	 * No resource pile may be negative. If a pile would contain <= 0, it is not included in the returned bundle.
	 */
	public Bundle minus(Bundle goal) {
		Bundle diff = new Bundle();
		for(ResourcePile have:contents){
			ResourcePile copy = new ResourcePile(have);
			for(ResourcePile used:goal){
				if (copy.type == used.type){
					copy.amount -= used.amount;
					if (copy.amount < 0) copy.amount = 0;
					break;
				}
			}
			if (copy.amount > 0) diff.insert(copy);
		}
		return diff;
	}

	/**
	 * Returns the pile associated with the Resource type.
	 * May return a pile with contents 0, but never null.
	 */
	public ResourcePile getType(Resource type) {
		for (ResourcePile pile:contents){
			if (pile.type == type) return pile;
		}
		ResourcePile missing = new ResourcePile(type,0);
		contents.add(missing);
		return missing;
	}
	
	/**
	 * Returns the total value of this bundle based on the economy's current prices.
	 */
	public long getValue(){
		long total = 0;
		for (ResourcePile pile:contents){
			total += pile.amount * economy.prices[pile.type.ordinal()];
		}
		return total;
	}

	/**
	 * Returns the price of the most expensive type of resource in this bundle.
	 */
	public int getMostExpensive() {
		int price = 0;
		for (ResourcePile pile:contents){
			int currPrice = economy.prices[pile.type.ordinal()];
			if(currPrice > price){
				price = currPrice;
			}
		}
		return price;
	}
	
	/**
	 * Returns the type of the least expensive resource in this bundle.
	 * Used for minimizing value lost to trading.
	 */
	public Resource getLeastExpensive() {
		int price = Integer.MAX_VALUE;
		Resource type = null;
		for (ResourcePile pile:contents){
			int currPrice = economy.prices[pile.type.ordinal()];
			if(currPrice < price){
				price = currPrice;
				type = pile.type;
			}
		}
		return type;
	}

	/**
	 * Returns a bundle representing a bundle "scale" times the size of this bundle.
	 */
	public Bundle times(Double scale) {
		Bundle result = new Bundle();
		for (ResourcePile pile:contents){
			result.insert(new ResourcePile(pile.type,(int) (pile.amount * scale)));
		}
		return result;
	}
	
	/**
	 * Returns the internal ResourcePile that matches type.
	 */
	public ResourcePile getResource(Resource type){
		for (ResourcePile curr:contents){
			if (curr.type == type) return curr;
		}
		return new ResourcePile(type, 0);
	}
}
