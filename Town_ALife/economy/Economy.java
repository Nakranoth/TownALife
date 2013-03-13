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
	public Double[] prices = new Double[Resource.values().length];
	//public double[] wage = null;	//represents the percent of output value to be paid out. Real pay is rounded down. Is adjusted per type.
	//public int[] wageRaiseHelper = null;	//number of this type that wanted to operate, but found no workers.
	//public int[] wageReduceHelper = null;	//number of this type that refused to operate because of negative profits.
	Bundle demand, supply = new Bundle();
	
	public Economy(){
		for (Resource init:Resource.values()){
			prices[init.ordinal()] = 1.0;	//All same price to start. Should quickly adjust to market.
		}
	}
	
	/**
	 * Updates prices for next year. Run after trading.
	 */
	public void updatePrices(){
		double[] ratios = new double[Resource.values().length];
		for(Resource type:Resource.values()){
			double top = Math.max((double)demand.getType(type).amount,0.01D);
			double bottom = Math.max((double)supply.getType(type).amount,0.01D);
			ratios[type.ordinal()] = Math.min(1000D, Math.max(0.0001, top / bottom));
		}
		for(int i = 0; i < Resource.values().length; i++){
			prices[i] = ratios[i] / ratios[Resource.goods.ordinal()];	//People always want some goods.
			//System.out.print(Resource.values()[i].name() + ":" + prices[i] + ", ");
		}
		//System.out.print(type.name() + ":" + prices[type.ordinal()] + ", ");
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
				sold.insert(new ResourcePile(pile.type,(int) (pile.amount * leftover[pile.type.ordinal()])));
			}
			for(ResourcePile pile:reciever.demandedGoods){
				metDemand.insert(new ResourcePile(pile.type,(int) (pile.amount * bought[pile.type.ordinal()])));
			}
			reciever.income = reciever.income.minus(sold);	//leftovers from sales
			reciever.income.insert(metDemand);	//new things bought.
		}
		
	}

	public String getSupplyString() {
		String supplyString = new String();
		
		Long supplies[] = new Long[Resource.values().length];
		
		for(ResourcePile pile:supply){
			supplies[pile.type.ordinal()] = pile.amount;
		}
		for (int i = 0; i < Resource.values().length; i++){
			supplyString += supplies[i].toString() +",";
		}
		return supplyString;
	}
	
	public String getDemandString() {
		String demandString = new String();
		
		Long demands[] = new Long[Resource.values().length];
		
		for(ResourcePile pile:demand){
			demands[pile.type.ordinal()] = pile.amount;
		}
		for (int i = 0; i < Resource.values().length; i++){
			demandString += demands[i].toString() +",";
		}
		return demandString;
	}
	
	public String getPriceString() {
		String priceString = new String();
		
		for (int i = 0; i < Resource.values().length; i++){
			priceString += prices[i].toString();
		}
		return priceString;
	}
}
