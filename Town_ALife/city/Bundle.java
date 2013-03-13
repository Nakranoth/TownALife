package city;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import city.ResourcePile.Resource;

/**
 * Collection of ResourcePiles.
 * Is iterable, returning ResourcePiles
 * @author Nathan Killeen
 */
public class Bundle implements Iterable<ResourcePile>{
	public ArrayList<ResourcePile> contents = new ArrayList<ResourcePile>();
	
	/**
	 * Conversion from pile array.
	 * Used primarily for costs. 
	 */
	public Bundle(ResourcePile[] pileSet){
		for(ResourcePile pile: pileSet){
			if (pile.amount > 0) contents.add(new ResourcePile(pile));	//new for separation safety.
		}
	}

	/**
	 * Bundle initialized to contain one ResourcePile
	 */
	public Bundle(ResourcePile resourcePile) {
		if (resourcePile.amount > 0)	contents.add(new ResourcePile(resourcePile));
	}

	public Bundle() {//seriously? I need to add this?
	}
	
	/**
	 * Intelligently adds the inserted pile to the bundle.
	 */
	public void insert(ResourcePile inserted) {
		if(inserted.amount <=0) return;	//avoid empty piles.
		for (ResourcePile curr:contents){
			if (curr.type == inserted.type){
				curr.amount += inserted.amount;
				return;
			}
		}
		contents.add(new ResourcePile(inserted));	//We do NOT want multiple bundles accidently attached to each other.
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
	 * Removes the bundle from this.
	 */
	public void extract(Bundle removed){
		ResourcePile curr;
		for(Iterator<ResourcePile> i = removed.iterator();i.hasNext();){
			curr = i.next();
			extract(curr);
		}
		for(Iterator<ResourcePile> i = removed.iterator();i.hasNext();){
			curr = i.next();
			if (curr.amount <= 0) i.remove();
		}
	}
	
	/**
	 * Removes a single pile from this.
	 */
	private void extract(ResourcePile removed) {
		for (ResourcePile curr:contents){
			if (curr.type == removed.type){
				curr.amount -= removed.amount;	
				//if (curr.amount <= 0) contents.remove(curr);// NO NEGATIVES!
				return;
			}
		}
	}

	/**
	 * Returns the number of times this fits into divisor as constrained by the smallest difference.
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
					if (copy.amount < 0) copy.amount = 0L;
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
	public double getValue(){
		double total = 0;
		for (ResourcePile pile:contents){
			total += pile.amount * City.economy.prices[pile.type.ordinal()];
		}
		return total;
	}

	/**
	 * Returns the price of the most expensive type of resource in this bundle.
	 */
	public double getMostExpensiveValue() {
		double price = 0;
		for (ResourcePile pile:contents){
			double currPrice = City.economy.prices[pile.type.ordinal()];
			if(currPrice > price){
				price = currPrice;
			}
		}
		return price;
	}
	
	/**
	 * Returns the Nth most expensive resource pile, where 0 is the most expensive.
	 */
	private ResourcePile getNthMostExpensive(int n){
		Collections.sort(contents);	
		return contents.get(n);
	}
	
	/**
	 * Returns the type of the least expensive resource in this bundle.
	 * Used for minimizing value lost to trading.
	 */
	public Resource getLeastExpensiveType() {
		double price = Double.POSITIVE_INFINITY;
		Resource type = null;
		for (ResourcePile pile:contents){
			double currPrice = City.economy.prices[pile.type.ordinal()];
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
			ResourcePile insert = new ResourcePile(pile.type,(int) (pile.amount * scale));
			if(insert.amount > 0){
				result.insert(insert);
			}
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

	/**
	 * Returns nearly the cheapest bundle worth at least maxPrice containted in this bundle.
	 * Also remove that bundle from this one.
	 */
	public Bundle worthAtLeast(double maximumPrice) {
		double value = getValue();
		if (value < maximumPrice) return null;	//I want to cause null pointers if this ever happens.
		Bundle divvy = this.times(maximumPrice/value);	//good starting guess.
		Bundle leftover = this.minus(divvy);//What I have left to allocate.
		
		int n = 0;
		while(n < leftover.contents.size() && divvy.getValue() < maximumPrice){
			ResourcePile nth = leftover.getNthMostExpensive(n);
			if (divvy.getValue() + nth.getValue() < maximumPrice){
				divvy.insert(nth);
			}
			else{
				divvy.insert(new ResourcePile(nth.type,(int) (nth.amount*(City.economy.prices[nth.type.ordinal()]/(maximumPrice-divvy.getValue())))));
			}
			n++;
		}
		leftover = this.minus(divvy);//updated for stuffs shoved in.
		n = leftover.contents.size() - 1;
		while(n >= 0 && divvy.getValue() < maximumPrice){
			divvy.insert(leftover.getNthMostExpensive(n));//give it all leftovers until we have enough. Last ditch effort.
			n--;
		}
		if (divvy.getValue() < maximumPrice) return null;	//This should not be possible.

		extract(divvy);
		return divvy;
	}

	/**
	 * Gets the ratio of each Resource in this over denominator
	 */
	public double[] ratios(Bundle denominator) {
		double[] ratios = new double[Resource.values().length];
		for(int i = 0; i < Resource.values().length; i++)	ratios[i] = 0.0;
	
		for(ResourcePile pile:this){
			long denAmount = denominator.getResource(pile.type).amount;
			if(denAmount != 0){
				ratios[pile.type.ordinal()] = (double)pile.amount / (double)denAmount;
			}
			else{
				ratios[pile.type.ordinal()] = Double.POSITIVE_INFINITY;
			}
		}
		return ratios;
	}

	/**
	 * Gets a bundle with each of this bundle's resources scaled by ratio[]. 
	 */
	public Bundle times(double[] ratios) {
		Bundle times = new Bundle();
		for(ResourcePile pile:this){
			times.insert(new ResourcePile(pile.type,(int) (pile.amount*ratios[pile.type.ordinal()])));
		}
		return times;
	}
}
