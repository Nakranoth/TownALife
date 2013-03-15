package economy;

import java.util.ArrayList;

import people.Person;
import city.Bundle;
import city.City;
import city.ResourcePile;
import city.ResourcePile.Resource;

/**
 * Tracks prices, and matches quantities between supplies and demands. 
 * @author Nathan Killeen
 */
public class Economy {
	public Double[] prices = {1.2,0.28,0.8,1.1,0.19,1.0,1.0,0.19};//new Double[Resource.values().length];
	//public double[] wage = null;	//represents the percent of output value to be paid out. Real pay is rounded down. Is adjusted per type.
	//public int[] wageRaiseHelper = null;	//number of this type that wanted to operate, but found no workers.
	//public int[] wageReduceHelper = null;	//number of this type that refused to operate because of negative profits.
	//private LinkedList<Double[]> ratioQueue = null;//
	private Double[] smoothedRatios = {1.2,0.28,0.8,1.1,0.19,1.0,1.0,0.19};//new Double[Resource.values().length];
	Bundle demand, supply = new Bundle();
	
	public Economy(){
		/*for (Resource init:Resource.values()){
			prices[init.ordinal()] = 1.0;	//All same price to start. Should quickly adjust to market.
		}*/
	}
	
	/**
	 * Updates prices for next year. Run after trading.
	 */
	public void updatePrices(){
		Double[] ratios = new Double[Resource.values().length];
		for(Resource type:Resource.values()){
			double top = Math.max((double)demand.getType(type).amount,0.01D);
			double bottom = Math.max((double)supply.getType(type).amount,0.01D);
			ratios[type.ordinal()] = Math.min(1000D, Math.max(0.0001, top / bottom));
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
		
		Double supplies[] = new Double[Resource.values().length];
		
		for(ResourcePile pile:supply){
			supplies[pile.type.ordinal()] = pile.amount;
		}
		for (int i = 0; i < Resource.values().length; i++){
			supplyString += supplies[i].toString() +",";
		}
		return supplyString;
	}
	
	public String getDemandString() {
		String demandString = new String(City.year + ",");
		
		Double demands[] = new Double[Resource.values().length];
		
		for(ResourcePile pile:demand){
			demands[pile.type.ordinal()] = pile.amount;
		}
		for (int i = 0; i < Resource.values().length; i++){
			demandString += demands[i].toString() +",";
		}
		return demandString;
	}
	
	public String getPriceString() {
		String priceString = new String(City.year + ",");
		
		for (int i = 0; i < Resource.values().length; i++){
			priceString += prices[i].toString() +",";
		}
		return priceString;
	}
}
