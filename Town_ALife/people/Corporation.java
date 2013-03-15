package people;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import people.Person.SkillType;
import people.Preferences.Preference;
import city.Bundle;
import city.City;
import city.Factory;
import city.ResourcePile;
import error.BadSellerException;

public class Corporation{
	//private Economy economy = City.economy;
	
	private ArrayList<CorpMember> members = new ArrayList<CorpMember>();
	public Factory holding = null;
	public ResourcePile income = null;
	public Allocation operatingCost = new Allocation();
	public LinkedList<Person> workers = new LinkedList<Person>();

	//private LinkedList<Double> annualProfits = new LinkedList<Double>();
	public double annualProfitability;	//How much a corp expects to be able to make every year, if it can hire workers.
	public double cheapestStock;	//The cheapest stock available for sale.
	public Double profitRatio = Double.POSITIVE_INFINITY;
	public CorpMember cheapestSeller = null;
	
	public Double wage;	//the value of the wage, rather than the number of the good.
	
	public Corporation(Factory holding, Person owner){
		this.holding = holding;
		operatingCost.goal = holding.getOperatingCost();
		members.add(new CorpMember(owner, 100));
		cheapestSeller = members.get(0);
		annualProfitability = holding.getProfitability(operatingCost.goal);;//smoothed profit function.
	}
	
	/**
	 * Computes, stores, and returns the wage this corp will pay out.
	 * If wage is zero, predicted profit was zero or less, so wages should fall.
	 */
	public double setWage(){
		wage = 0D;
		
		double currProfit = holding.getProfitability(operatingCost.resources);
		annualProfitability += (currProfit - annualProfitability);///5; already smoothed via prices.
		updateCheapest();
		
		for(CorpMember owner:members){
			if(owner.person.alive){	//ignore the dead.
				double greed = owner.person.preferences.get(Preference.greed);
				wage += Math.max(currProfit * Math.max(0.001,greed),0D)*owner.ownership/100D;
			}
		}
		wage = wage / holding.getTaskCapacity();	//better to store as per person.
		return wage;
	}
	
	/**
	 * Hires workers for this year.
	 * @param workPool The collection of unemployed people old enough to work. Modified by this call.
	 * @return Whether we hired anyone.
	 */
	public int hire(ArrayList<Person> workPool){
		SkillType active = holding.getRelevantSkill();
		if (active == null) return 0;	//no active skill is bad.
		workers.clear();
		int capacity = holding.getTaskCapacity();
		
		for (Person worker:workPool){
			//do an insertion Sort.
			ListIterator<Person> i = workers.listIterator();
			while(i.hasNext()){
				Person next = i.next();
				if(next.getSkill(active) > worker.getSkill(active)){
					i.previous();
					i.add(worker);
					break;
				}
			}
			if(!i.hasNext()){	//we reached the end of the list.
				i.add(worker);
			}
			if (workers.size() > capacity) workers.pop();
		}
		for(Person hired:workers){
			workPool.remove(hired);	//happens in corp loop. No error!
		}
		return workers.size();
	}
	
	/**
	 * Handles all work and payments.
	 */
	public void doWork(){	//Must be called on everyone to cause profits to be updated.
		income = holding.doWork(workers, operatingCost.resources);
		ResourcePile income = this.income;	//leave this.income in tact. incomeValue = City.economy.prices[income.type.ordinal()] * income.amount;
		ResourcePile payment = new ResourcePile(income.type, (int) (wage/City.economy.prices[income.type.ordinal()]));

		for(Person employee:workers){
			employee.income.insert(payment);
		}
		income.amount -= Math.min(payment.amount * workers.size(),income.amount);

		for (CorpMember toPay:members){
			toPay.person.income.insert(new ResourcePile(income.type,(int) (income.amount*(toPay.ownership / 100.0D))));	//Rounding errors are okay. Just means loss to inefficiency.
		}
	}
	

	private void updateCheapest() {
		cheapestSeller = null;
		cheapestStock = Double.POSITIVE_INFINITY;
		for(CorpMember seller:members){
			if (seller.person.alive){
				double sellerPrice = seller.person.preferences.get(Preference.timeScale) * annualProfitability / 100;
				if (sellerPrice < cheapestStock){
					cheapestSeller = seller;
					cheapestStock = sellerPrice;
				}
			}
		}
		profitRatio = cheapestStock/(annualProfitability/100D);
	}

	/**
	 * Directly barters for as many stocks as the buyer wants.
	 * @param buyer The person trying to buy
	 * @param available The "resources" bundle from the relevant allocation.
	 */
	public void buyShares(Person buyer, Bundle available){
		double worth = buyer.preferences.get(Preference.timeScale) * annualProfitability / 100;
		while (available.getValue() > cheapestStock && cheapestSeller.person != buyer && cheapestStock < worth){
			int shares = (int) Math.min(cheapestSeller.ownership, available.getValue() / cheapestStock);
			double maximumPrice = cheapestStock * shares;
			try {
				transferOfOwnership(cheapestSeller.person, buyer, shares);
				Bundle barter = available.worthAtLeast(maximumPrice);
				cheapestSeller.person.income.insert(barter);//Give barter to seller.
			} catch (BadSellerException e) {
				System.err.println("Corp: sale");
				e.printStackTrace();
			}
			updateCheapest();
		}
	}
	
	
	public void partialTransferOfOwnership(Person seller, Person buyer, int shares) throws BadSellerException {
		// TODO Auto-generated method stub		
		CorpMember corpSeller = null, corpBuyer = null;
		for (CorpMember member: members)
		{
			if (member.person == seller) corpSeller = member;
			if (member.person == buyer) corpBuyer = member;
		}
		if (corpSeller == null || corpSeller.ownership < shares) 
			throw new BadSellerException(buyer, seller, this);
		if (corpBuyer == null)
		{
			corpBuyer = new CorpMember(buyer, shares);
			members.add(corpBuyer);
			corpSeller.ownership -= shares;
		}
		else{
			corpBuyer.ownership += shares;
			corpSeller.ownership -= shares;
		}
		if(corpSeller.ownership <= 0) {
			members.remove(corpSeller);	//No remote drop. This way we avoid concurrency errors during death.
		}
	}
	
	public void transferOfOwnership(Person seller, Person buyer, int shares) throws BadSellerException	{
		partialTransferOfOwnership(seller, buyer, shares);
		dropEmptyMembers();
	}
	
	public void dropEmptyMembers(){
		CorpMember member;
		for (Iterator<CorpMember> i = members.iterator(); i.hasNext();){
			member = i.next();
			if(member.ownership <= 0) {
				member.person.dropCorp(this);
				i.remove();
			}
		}
	}
	
	public int getShares(Person query){
		int shares = 0;
		for (CorpMember member: members)
		{
			if (member.person == query){
				shares = member.ownership;
				break;
			}
		}
		return shares;
	}
	
	/**
	 * Handles cleaning out dead members the rest of the way.
	 * Returns whether it should fold.
	 */
	public boolean redistribute(){
		if (members.size() == 0) return true;
		CorpMember diedMember = null;
		for (CorpMember member:members){
			if (!member.person.alive){
				diedMember = member;
				break;
			}
		}
		if (diedMember == null) return false; 
		members.remove(diedMember);	//get the dead one out the way.
		int total = 0;	//used to force 100 shares at end.
		if (members.size() != 0 && diedMember.ownership < 100){
			for(CorpMember member:members){
				float growth = member.ownership / (100 - diedMember.ownership);
				member.ownership += member.ownership * growth;
				total += member.ownership;
			}
			if (total != 100){	//Fudge back to 100 total.
				for(CorpMember member:members){
					if (member.ownership > total - 100){
						member.ownership += 100 - total;
						break;
					}
				}
			}
		}
		else{
			members.remove(diedMember);
			return true;	//This corp has folded, even though it's holding hasn't collapsed yet.
		}
		return false;
	}
	
	public boolean checkHoldings(){
		if (holding.ordinal == -1){	//refund this back to corp owners
			for(CorpMember member:members){
				member.person.income.insert(operatingCost.resources.times(member.ownership/100D));
			}
			return true;
		}
		return false;
	}
	
	public String toString(){
		return holding.toString()+annualProfitability;
	}
}
