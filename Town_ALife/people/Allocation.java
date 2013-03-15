package people;

import city.Building;
import city.Bundle;
import city.City;
import city.Factory;
import city.ResourcePile;
import city.ResourcePile.Resource;
import economy.Economy;

/**
 * Container for handling all things to do with setting aside resources for a purpose.  
 * @author Nathan Killeen
 */
public class Allocation {
	private static Economy economy = City.economy;
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
		value += resources.minus(goal).getValue();	//include the value of the over-stocking. Excess supply MUST be included externally.
		if (value <= 0) return new Bundle();	//no demand if we can't afford it.
		Bundle demand = goal.minus(resources);
		if (demand.contents.isEmpty()) return demand;

		double mostExpensive = demand.getMostExpensiveValue();	//for use in reducing excess demand.
		
		double demandValue;
		while ((demandValue = demand.getValue()) > value)	//may need 2 passes depending on rounding.
		{
			demand = demand.over(Math.max(((double)demandValue /(double)value),1.0D/(double)mostExpensive));	//will reduce by at least 1 of the most expensive units.
		}
		
		Resource cheapest = goal.getLeastExpensiveType();
		double cheapPrice = economy.prices[cheapest.ordinal()];
		if (demandValue < value + cheapPrice){
			demand.insert(new ResourcePile(cheapest, (value-demandValue)/cheapPrice));	//add in some of the cheapest good to recover excess value.
		}
		
		return demand;
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
	 */
	public void planBuilding(int years, double genValue){
		Building planned = null;
		double profit = 0;
		
		if (genValue > 0){
			double maxPrice = resources.getValue() + (genValue * years);
			
			for (Factory curr: Factory.samples){
				double currCost = curr.getCost().getValue(); 
				if (currCost <= maxPrice){
					int yearsToBuild = (int) (curr.getCost().minus(resources).getValue() / genValue);
					curr.updateGuesses();
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
		}
		else{
			goal = new Bundle(); 
		}
	}
	
}
