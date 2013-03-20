package economy;

import java.util.ArrayList;

import people.Person;
import city.Bundle;
import city.City;
import city.Factory;
import city.ResourcePile;
import city.ResourcePile.Resource;
import city.Statistics;
import city.Task;

/**
 * Tracks prices, and matches quantities between supplies and demands. 
 * @author Nathan Killeen
 */
public class Economy {
	public Double[] prices = {1.2,0.28,0.8,1.1,0.19,1.0,1.0,0.19};//new Double[Resource.values().length];
	private Double[] smoothedRatios = {1.2,0.28,0.8,1.1,0.19,1.0,1.0,0.19};//new Double[Resource.values().length];
	public Bundle demand = new Bundle(), supply = new Bundle();
	private double[] marketMaxSupply = null;
	
	/**
	 * Must run after loading Factory.samples
	 */
	public Economy(){
		marketMaxSupply = new double[Resource.values().length];
	}
	
	/**
	 * Updates prices for next year. Run after trading.
	 */
	public void updatePrices(){
		Double[] ratios = new Double[Resource.values().length];
		Bundle excess = demand.intersect(supply);
		for(Resource type:Resource.values()){
			double top = Math.max((double)demand.getResource(type).amount - excess.getResource(type).amount,0.01D);
			double bottom = Math.max((double)supply.getResource(type).amount - excess.getResource(type).amount,0.01D);
			ratios[type.ordinal()] = Math.min(1000D, Math.max(0.001, top / bottom));
		}

		for (int i = 0; i < Resource.values().length;i++){
			smoothedRatios[i] += (ratios[i] - smoothedRatios[i]) / 5;	//Smoothing
		}

		
		for(int i = 0; i < Resource.values().length; i++){
			prices[i] = smoothedRatios[i] / smoothedRatios[Resource.goods.ordinal()];	//People always want some goods.
			//System.out.print(Resource.values()[i].name() + ":" + prices[i] + ", ");
		}
		//System.out.println();
	}

	/**
	 * Updates the supply maximums per factory type.
	 */
	public void updateMarketMaxSupply(){
		marketMaxSupply = new double[Resource.values().length];
		for (int i = 0; i < Factory.samples.size(); i++){
			for (Task curr:Factory.samples.get(i).getTasks()){
				curr.reGuess();
				marketMaxSupply[curr.getOutput().type.ordinal()] += curr.getUnthrottledOutput().amount * Statistics.corpClass[i];
			}
		}
	}
	
	/**
	 * @param resourceOrdinal The ordinal of the resource in question
	 * @return how much the city could produce if enough workers.
	 */
	public double getMaxSuppply(int resourceOrdinal){
		return marketMaxSupply[resourceOrdinal];
	}
	
	/**
	 * Run before trading, after initializing allocations.
	 * @param living From City 
	 */
	public void updateSupplyDemand(ArrayList<Person> living){
		supply = new Bundle();
		demand = new Bundle();
		
		
		for (Person curr:living){
			demand.insert(curr.demandedGoods);
			supply.insert(curr.income);
		}
	}

	/**
	 * Matches supply with demand. It is assumed that demand is more important than value.
	 * Price is used only to drive demand.
	 */
	public void doTrading() {
		Bundle exchanged = supply.intersect(demand); //Ignore value. This is all the goods that can have moved.
		double leftover[] = exchanged.ratios(supply);
		double bought[] = exchanged.ratios(demand);
		
		for(Person reciever:City.alive){
			Bundle sold = new Bundle();
			Bundle metDemand = new Bundle();
			for(ResourcePile pile:reciever.income){
				sold.insert(new ResourcePile(pile.type,pile.amount * leftover[pile.type.ordinal()]));
			}
			for(ResourcePile pile:reciever.demandedGoods){
				metDemand.insert(new ResourcePile(pile.type,pile.amount * bought[pile.type.ordinal()]));
			}
			reciever.income = reciever.income.minus(sold);	//leftovers from sales
			reciever.income.insert(metDemand);	//new things bought.
		}
		
	}

	public String getSupplyString() {
		String supplyString = new String(City.year + ",");
		
		double supplies[] = new double[Resource.values().length];
		
		for(ResourcePile pile:supply){
			supplies[pile.type.ordinal()] = pile.amount;
		}
		for (int i = 0; i < Resource.values().length; i++){
			supplyString += ((Double)supplies[i]).toString() +",";
		}
		return supplyString;
	}
	
	public String getDemandString() {
		String demandString = new String(City.year + ",");
		
		double demands[] = new double[Resource.values().length];
		
		for(ResourcePile pile:demand){
			demands[pile.type.ordinal()] = pile.amount;
		}
		for (int i = 0; i < Resource.values().length; i++){
			demandString += ((Double)demands[i]).toString() +",";
		}
		return demandString;
	}
	
	public String getPriceString() {
		String priceString = new String(City.year + ",");
		
		for (int i = 0; i < Resource.values().length; i++){
			prices[i] *= 100;
			priceString += prices[i].toString() +",";
		}
		return priceString;
	}

	/**
	 * Gets the excess demand for the resource with ordinal
	 * Now tries to predict 2 year setting.
	 * @param type
	 * @return
	 */
	public double getExcessDemand(Resource type) {
		return demand.times(2.0).minus(supply.minus(supply.intersect(demand))).getResource(type).amount;
	}
}
