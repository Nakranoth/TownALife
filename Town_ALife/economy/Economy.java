package economy;

import java.util.ArrayList;

import people.Person;
import city.Bundle;
import city.City;
import city.ResourcePile.Resource;

/**
 * Tracks prices, and matches quantities between supplies and demands. 
 * @author Nathan Killeen
 */
public class Economy {
	public int[] prices = new int[Resource.values().length];
	Bundle demand, supply;
	
	public Economy(){
		for (Resource init:Resource.values()){
			prices[init.ordinal()] = 125;	//All same price to start. Should quickly adjust to market.
		}
	}
	
	/**
	 * Cycles through everyone in the city, summing their supply and demand to generate prices for next year.
	 * Should be run only after each person handles their allocations, and collect's their demands.
	 */
	public void updatePrices(){
		demand = demand(City.alive);
		supply = supply(City.alive);
		for(Resource type:Resource.values()){
			prices[type.ordinal()] *= (double)demand.getType(type).amount / Math.max((double)supply.getType(type).amount,0.5);
			if (prices[type.ordinal()] < 100) prices[type.ordinal()] = 100;
		}
	}

	//Run after having each person update their allocations.
	private Bundle demand(ArrayList<Person> living){
		Bundle demand = new Bundle();
		
		for (Person curr:living){
			demand.insert(curr.demandedGoods);
		}
		return demand;
	}
	
	//Run after having each person update their allocations.
	private Bundle supply(ArrayList<Person> living){
		Bundle supply = new Bundle();
		
		for (Person curr:living){
			supply.insert(curr.income);
		}
		
		return supply;
	}
}
