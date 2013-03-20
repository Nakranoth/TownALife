package people;

import city.Building;
import city.Bundle;
import city.Factory;
import city.ResourcePile;

/**
 * Container for handling all things to do with setting aside resources for a purpose.  
 * @author Nathan Killeen
 */
public class Allocation {
	public Building building = null;
	public Bundle resources = new Bundle();
	public Bundle goal = new Bundle();
	public Bundle demand = new Bundle();	//demanded bundle this year, based on allocation and prices.
	public double adjustedProfitability;	//Applies only to i,j,k. Saves time adjusted value for comparison against buying stock.
	
	/**
	 * Handles setting internals for building changes.
	 */
	public void setBuildingClass(Building type){
		building = type;
		goal = (type == null?new Bundle():type.getCost());
	}
	
	/**
	 * @return Whether or not there are enough resources to match the goal.
	 */
	public boolean hasEnough(){
		if(goal.getValue() == 0) return false;	//You can NEVER afford nothing!
		return resources.hasAtLeast(goal);
	}
	
	
	/**
	 * @return The bundle of goods within in excess of those required by the goal.
	 * @note Actually removes the excess from resources.
	 */
	public Bundle refundExcessSupply(){	//gets included backwards to income when determining total supply.
		Bundle excess = resources.minus(goal);
		resources = resources.minus(excess);
		return excess;  
	}
	
	/**
	 * Gets a demand based on the price of resources being pumped in. Does not change local demand.
	 * @param value The value of goods being sold to fund this demand.
	 * @return The bundle representing the new demand.
	 */
	public Bundle getDemand(double value){
		value += resources.getValue();	//include the value of leftovers.
		if (value <= 0) return new Bundle();	//no demand if we can't afford it.

		if (goal.contents.isEmpty()) return goal;
		
		double costPerDemand = goal.getValue();
		
		Bundle affordable = goal.over(costPerDemand/value).minus(resources);	//no need to re-demand the leftover portions.)
		
		//check for effectively fulfilled
		for(ResourcePile diff:affordable){
			if (diff.amount < 0.05){ //close enough, just give it to them.
				resources.insert(diff);
				affordable.extract(new Bundle(diff));
			}
		}
		
		return affordable;
	}

	/**
	 * Simply sets the goal based upon the planned building.
	 */
	public void refreshGoal() {
		goal = (building == null?new Bundle():building.getCost());
	}
	
	/**
	 * Sets up the planning of new buildings at a time in the future.
	 * @param years The duration in which profit should be maximal.
	 * @param genValue How much resources can be spent annually towards these plans.
	 * @return 
	 */
	public Bundle planBuilding(int years, double genValue, double greed){
		Building planned = null;
		double profit = 1 - greed; //must generate a minimum amount to be worth building
		
		if (genValue > 0){
			double maxPrice = resources.getValue() + (genValue * years);
			
			for (Factory curr: Factory.samples){
				double currCost = curr.getCost().getValue(); 
				if (currCost <= maxPrice){
					int yearsToBuild = (int) (curr.getCost().minus(resources).getValue() / genValue);
					//curr.updateGuesses();
					double currProfit = curr.getProfitability() * (years-yearsToBuild) - currCost;
					if (currProfit > profit){
						planned = curr;
						profit = currProfit;
					}
				}
			}
		}
		building = planned;
		adjustedProfitability = profit;
		if(planned != null){
			refreshGoal();
			return null;
		}
		else{
			goal = new Bundle(); 
			Bundle temp = resources;
			resources = new Bundle();
			return temp;
		}
		
	}
	
}
