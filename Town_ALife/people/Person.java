package people;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import people.Preferences.Preference;
import city.Bundle;
import city.City;
import city.Factory;
import city.Home;
import city.ResourcePile;
import city.ResourcePile.Resource;
import city.homes.Shack;
import economy.Economy;
import economy.Listing;
import error.BadSellerException;

/**
 * The actual agents within the city. Contains all attributes and logic for division of resources.
 * @author Nathan Killeen
 */
public class Person{
	
	private static Preference[] allocSet = {Preference.goodsAlloc,Preference.incomeAlloc,Preference.upkeepAlloc,Preference.childAlloc,Preference.newHomeAlloc},	//all resource allocation weights
			//yearSet = {"yearI","yearJ","yearK"},	//all forward thinking durations.
			utilitySet = {Preference.craftUtil,Preference.goodUtil,Preference.foodUtil,Preference.homeUtil};	//All sources of utility
	private static final Economy economy = City.economy;	//easy pointer.
	
	
	public static enum SkillType {logger,quarryer,farmer,miner,blackSmith,carpenter,chef};	//all types of skills. Nearly map to Resources, but blacksmith is shared by goods and tools
	
	public int effectiveAge = 0;	//incremented by age() function. determines chance of death.
	public int realAge = 0;			//incremented directly from logic loop in city. Tracks age for moving out and mate finding purposes.
	public boolean alive = true;	//used for cleanup portions of processing.
	public boolean male;			//gender.
	
	public int minimumWage;			//The least we'll work for
	
	public Preferences preferences;	//all inherited things. Public for printing at end.
	
	private static Random rand = new Random();
	
	//Holds how much I made last year, used for resource allocation.
	public Bundle income = new Bundle();	//Will be changed by this and Economy each pass.
	double incomeValue;						//Used internally to track the base amount I made.
	
	public Bundle demandedGoods = new Bundle();	//Total demand across all bundles.
												//Becomes the results from trading each year.
	
	public Allocation[] allocations = new Allocation[6];	//goods, upkeep, newHome, support, operationalCosts, factory // In that order
	private int[] skills = new int[SkillType.values().length];
	private ArrayList<Corporation> ownerships = new ArrayList<Corporation>();
	private ArrayList<Home> realestate = new ArrayList<Home>();//all buildings not home.
	private Family family = new Family();
	public Home home = null;
	
	public double utility;	//Generated health/happiness this year.
	
	public ArrayList<Listing> listings = new ArrayList<Listing>();
	
	/**
	 * Default junk guess for first run. Used simply as starting point to get more meaningful numbers from system.
	 * @param thisConstructor meaningless flag
	 */
	public Person(boolean isMale)
	{
		this();
		preferences = new Preferences();
		//TODO Load preferences
		preferences.put(Preference.stubbornness, rand.nextDouble()*10);	//Reevaluate inferiority in yearly.
		preferences.put(Preference.timeScale, rand.nextDouble()*20);		//how far we plan ahead in terms of income's value
		preferences.put(Preference.incomeAlloc, 0.3);	//how much we spend on making more

		preferences.put(Preference.goodsAlloc, 0.3);		//What we spend on consumer goods.
		
		preferences.put(Preference.upkeepAlloc, 0.1);	//Spending on home maintenance. Rolled into saving for new home if overflowing cap.
		preferences.put(Preference.upkeepCap, 3.0);		//If spending more than cap/20 of cost on upkeep, roll to next home savings.
		
		preferences.put(Preference.newHomeAlloc, 0.2);	//Base amount to save for new home if not in a villa. Meaningless if home is a villa.
		
		preferences.put(Preference.childAlloc,0.1);		//Part spent on all of your children. Rolls to next year's income if childless.
		
		//preferences.put(Preference.generosity, 1.0);	//percentage of mean salary they want to offer.
		preferences.put(Preference.greed, 0.5+rand.nextDouble()/2);		//Maximum percentage income to spend on wage. Caps salary, ignoring generosity.
													//Also, 1-greed is percent of building value to sell at.
		
		preferences.put(Preference.craftUtil, 1.0);
		preferences.put(Preference.goodUtil, 1.0);
		preferences.put(Preference.foodUtil, 1.1);
		preferences.put(Preference.homeUtil, 1.0);
		
		preferences.put(Preference.need, 3.0);	//If I can't afford at least this many of each good, I won't work.
		
		preferences.put(Preference.love, 0.05);	//How similar utility needs to be for marriage.
		
		preferences.put(Preference.smallestAlloc, 0.01);	//The "small" number in alloc normalization
		
		tweakPrefs();
		
		male = isMale;
	}
	
	/**
	 * Constructor for initializing a person from a loaded preference set.
	 */
	public Person(Preferences prefs)
	{
		this();
		preferences = prefs;
		tweakPrefs();
	}
	
	public static Person loadPerson(File prefSet){
		return new Person(Preferences.loadPref(prefSet));
	}
	
	/**
	 * The standard way agents are born.
	 */
	public Person(Person father, Person mother)//Parent A will always be male.
	{
		this();
		
		family.father = father;
		family.mother = mother;
		home = father.home;
		if (home != null){
			home.occupants.add(this);
		}
		
		utility = mother.utility;	//First year is free at parent's level.

		father.family.children.add(this);
		mother.family.children.add(this);
		
		preferences = Preferences.GenPrefs(father.preferences, mother.preferences); 
		tweakPrefs();
	}
	
	/**
	 * Makes adjustments as needed to preference set.
	 */
	private void tweakPrefs() {
		preferences.normalize(utilitySet, 1, 20);
		preferences.normalize(allocSet, preferences.get(Preference.smallestAlloc), 1.0);
		preferences.setMinimum(Preference.timeScale, 1.0);
		preferences.setMaximum(Preference.greed, 1.0);
	}

	private Person() {
		male = rand.nextBoolean();
		
		for(int i = 0; i < 6;i++){
			allocations[i] = new Allocation();
		}
	}

	public void preWorkInit(){
		minimumWage = (int) (preferences.get(Preference.need) * (economy.prices[Resource.food.ordinal()] + economy.prices[Resource.crafts.ordinal()] + economy.prices[Resource.goods.ordinal()]));
		for(Corporation held:ownerships){	//injects resources into each corp until I run out of resources.
			Bundle insert = held.operatingCost.goal.minus(held.operatingCost.resources).over(held.getShares(this));
			insert = allocations[4].resources.intersect(insert); //Gets what I have to give to this corp.
			allocations[4].resources.extract(insert);
			held.operatingCost.resources.insert(insert);
		}
	}
	
	/**
	 * Sets up all allocation demands for this year.
	 * Also consolidates all demands into demandedGoods.
	 */
	public void readyAllocations(){
		//demandedGoods = new Bundle(); Handled in allocation.
		
		allocations[4].demand = computeOperationalCosts();	//Operational costs
		incomeValue = income.getValue() - allocations[4].demand.minus(allocations[4].resources).getValue();//we immediately lose the value of demanded operational costs.
		//if (incomeValue < 0) incomeValue = Long.MAX_VALUE;//We overflowed... Wowza.	We CAN have negatives here.
		allocations[0].demand = computeGoodsBundle();	//goods
		
		if(allocations[1].building != home)
		{
			if (home == null || (family.father != null && family.father.home == home) || (family.mother != null && family.mother.home == home))
			{
				allocations[1].goal = new Bundle();	//empty goal. yay.
			}
			else {
				allocations[1].goal = computeUpkeepCap();
				allocations[1].building = home;
			}
		}
		
		long upkeepDemandValue = (long) (incomeValue * preferences.get(Preference.upkeepAlloc));
		allocations[1].demand = allocations[1].getDemand(upkeepDemandValue);
		//the difference between upkeepDemandValue and allocations[1].demand.getValue() gets added to allocations[2] (savings)
		Bundle upkeepRefund = allocations[1].refundExcessSupply();
		income.insert(upkeepRefund);
		
		//Update planned home.
		updateHomePlan();
		
		int homeSavings = (int) (incomeValue * preferences.get(Preference.newHomeAlloc) + upkeepRefund.getValue());
		allocations[2].refreshGoal();
		allocations[2].demand = allocations[2].getDemand(homeSavings);
		income.insert(allocations[2].refundExcessSupply());	//The value is still in the demand.
		
		allocations[3].goal = computeChildSupport();
		allocations[3].demand = allocations[3].getDemand((int) (incomeValue * preferences.get(Preference.childAlloc)));
		income.insert(allocations[3].refundExcessSupply());
		
		if(realAge % preferences.get(Preference.stubbornness) == 0){
			allocations[5].planBuilding(preferences.get(Preference.timeScale).intValue(),(long) (incomeValue * preferences.get(Preference.incomeAlloc)));
		}
		allocations[5].demand = allocations[5].getDemand((int) (incomeValue * preferences.get(Preference.incomeAlloc)));
		income.insert(allocations[5].refundExcessSupply());
		
		for(int i = 0; i < allocSet.length; i++){
			demandedGoods.insert(allocations[i].demand.times(2-preferences.get(Preference.greed)));	//Makes them demand more than they have.
		}
	}
	

	/**
	 * Appropriately handles each allocation post trade.
	 * Includes shifting goods from demand into allocations, building things, etc.
	 */
	public void handleAllocations(){
		//shift resources into allocations. 
		
		Bundle curr = allocations[4].goal.intersect(income);//operating costs get taken if they're there.
		allocations[4].resources.insert(curr);
		income.extract(curr);
		demandedGoods.extract(curr);

		Bundle usefulStock = income.intersect(demandedGoods);	//Resources I have that I want.
		//income.extract(usefulStock);	//keep goods I'm not using right now.
		double ratios[] = usefulStock.ratios(demandedGoods);	//useful/demanded

		curr = allocations[0].goal.times(ratios);//goods
		allocations[0].resources.insert(curr);
		income.extract(curr);
		
		curr = allocations[1].goal.times(ratios);//upkeep
		allocations[1].resources.insert(curr);
		income.extract(curr);
		
		curr = allocations[2].goal.times(ratios);//home
		allocations[2].resources.insert(curr);
		income.extract(curr);
		
		curr = allocations[3].goal.times(ratios);//support
		allocations[3].resources.insert(curr);
		income.extract(curr);
		
		curr = allocations[5].goal.times(ratios);//income
		allocations[5].resources.insert(curr);
		income.extract(curr);

		demandedGoods = new Bundle();
		
		//consume a[0]
		computeUtility(allocations[0].resources);
		allocations[0].resources = new Bundle();	//actually uses the whole bundle.
		
		//check against a[1]
		upkeepHome(allocations[1].resources);
		checkNewHome(allocations[2]);
		move();	//between building a home and possibly selling it.
		supportChildren(allocations[3].resources);
		checkNewIncome(allocations[5]);
		
		refreshListings();	//After handling everything, make sure that property is correctly for sale.
	}
	
	/**
	 * Checks to either build the goal factory, or buy shares.
	 */
	private void checkNewIncome(Allocation newIncomeBudget) {
		double buildingRatio = (double) (newIncomeBudget.building==null?Double.MAX_VALUE:newIncomeBudget.building.getCost().getValue()) / (double)newIncomeBudget.adjustedProfitability;
		boolean madePurchase = false;
		for (Corporation seller:City.allCorps){
			double budgetValue = newIncomeBudget.resources.getValue();
			if(seller.profitRatio > buildingRatio) break;	//once the stock is too expensive, we stop.
			while (seller.profitRatio < buildingRatio && budgetValue > seller.cheapestStock && seller.cheapestStock > 0){	//the stock is more valuable than the building
				madePurchase = true;
				seller.buyShares(this, newIncomeBudget.resources);
				budgetValue = newIncomeBudget.resources.getValue();
			}
		}
		if (madePurchase) Collections.sort(City.allCorps, new CorpRatioComp());//resort after purchases.
		
		if(newIncomeBudget.hasEnough() && newIncomeBudget.building != null){
			Corporation newCorp = new Corporation(((Factory) newIncomeBudget.building).shallowClone(), this);
			City.allCorps.add(newCorp);
			City.places.add(newCorp.holding);
			ownerships.add(newCorp);
		}
	}

	/**
	 * Divides bundle amongst the children. Also gives extra real estate to children in need.
	 * Should be empty if childless.
	 */
	private void supportChildren(Bundle resources) {
		int survivingChildren = 0;
		for (Person child:family.children) if (child.alive) survivingChildren++;
		
		Bundle portion = resources.over(survivingChildren);
		
		for (Person child:family.children){
			if (child.alive){
				child.income.insert(portion);
				if(child.realAge >= 16 && child.home == home && realestate.size() > 0){
					child.realestate.add(realestate.get(0));//give them old home
					realestate.remove(0);
					child.move();	//get them to move into it.
				}
			}
		}
		resources.extract(resources);//empty the bundle.
	}

	/**
	 * Tries to either build the goal or buy an equivalent one.
	 */
	private void checkNewHome(Allocation newHomeBudget) {
		if(newHomeBudget.building == null){	//we live in a freaking villa. Refund that shiznit.
			income.insert(newHomeBudget.resources);
			newHomeBudget.resources.extract(newHomeBudget.resources);
			return;
		}
		double budget = newHomeBudget.resources.getValue();
		boolean bought = false;
		for(Listing listing:City.listings){
			if (listing.price > budget) break;	//cannot afford.
			if(listing.property.getRank() >= ((Home) newHomeBudget.building).getRank()){
				listing.buy(newHomeBudget.resources.worthAtLeast(listing.price), this);
				bought = true;
				break;
			}
		}
		
		if(newHomeBudget.hasEnough()&&!bought){
			City.places.add(newHomeBudget.building);
			realestate.add((Home) newHomeBudget.building);
			newHomeBudget.building = null;	//make sure this thing doesn't get used elsewhere.
		}
	}

	/**
	 * Repairs home and mutates input.
	 */
	private void upkeepHome(Bundle resources) {
		if(home != null){
			home.maintain(resources);
		}
		else{
			income.insert(resources);
			resources.extract(resources);
		}
	}

	public Boolean hasSpouse() 
	{
		return family.spouse != null;
	}

	public void setSpouse(Person spouse) 
	{
		family.spouse = spouse;
	}
	
	/**
	 * Handles all logic necessary at an agent's death.
	 */
	private void die()
	{
		alive = false;
		preferences = null;
		//shift all holdings into income.
		for(Allocation curr:allocations){
			income.insert(curr.resources);//pull the resources out of each allocation. Don't worry about cleaning up the allocations.
		}
		allocations = null;
		//Give all income, allocations, and ownerships to others. If there is no one to give to, destroy resources, and balance corps.
		if (family.spouse != null && family.spouse.alive){
			family.spouse.income.insert(income);
			for(Corporation curr:ownerships){
				try{
					curr.partialTransferOfOwnership(this, family.spouse, curr.getShares(this));
				}
				catch(BadSellerException e){
					System.err.println(e.getMessage());
				}
			}
			family.spouse.realestate.addAll(realestate);
		}
		
		else {
			int numChildren = 0;
			for (Person curr:family.children){
				if (curr.alive) numChildren++;
			}
			if(numChildren > 0){
				income = income.over(numChildren);
			}
			for (Person curr:family.children){
				if (curr.alive){
					curr.income.insert(income);
					for(Corporation i:ownerships){
						try{
							i.partialTransferOfOwnership(this, curr, i.getShares(this)/numChildren);
						}
						catch(BadSellerException e){
							System.err.println(e.getMessage());
						}
					}
				}
			}
			for (Person curr:family.children){
				if (curr.alive){
					curr.realestate.add(home);
					curr.realestate.addAll(realestate);
				}
			}
			if (numChildren == 0){	//no surviving heirs
				for(Corporation i:ownerships){
					i.redistribute();
				}
			}
		}
		ownerships = null;
		if(home != null){
			home.occupants.remove(this);
		}
		home = null;
		realestate = null;
		family = null;	//makes sure the dead are eventually forgotten when their immediate family dies.
	}

	public void addCorp(Corporation corporation) 
	{
		ownerships.add(corporation);
	}
	
	public void dropCorp(Corporation corporation) 
	{
		ownerships.remove(corporation);
	}
	
	/**
	 * Gets the value of this person's talent in SkillType while incrementing it.
	 */
	public double doWork(SkillType type)
	{
		skills[type.ordinal()] += 1;
		
		return Math.sqrt(Math.max(1, skills[type.ordinal()] - (effectiveAge < 40?0:(40 - effectiveAge)/2)));	//effective skill slowly deteriorates after 40.
	}
	
	public double getSkill(SkillType type)
	{
		return Math.sqrt(Math.max(1, skills[type.ordinal()] - (effectiveAge < 40?0:(40 - effectiveAge)/2)));	//effective skill slowly deteriorates after 40.
	}
	
	/**
	 * Increments effective age, with a chance to kill this person.
	 * May be called more than once in a year if living in significantly below average conditions. 
	 */
	public void age()
	{
		effectiveAge++;
		if(rand.nextFloat() < 0.002 * Math.pow(2.718281828459045, effectiveAge*0.0627738191)) die();
	}
	
	/**
	 * Gets the bundle representing the goods that need to be set aside for next year's production.
	 */
	private Bundle computeOperationalCosts() {
		
		Bundle wanted = new Bundle();
		for(Corporation curr:ownerships){
			wanted.insert(curr.operatingCost.goal.minus(curr.operatingCost.resources).over(curr.getShares(this)));
		}
		
		return wanted;
	}
	
	/**
	 * Gets the bundle representing the goods that this person wants to consume this year, limited by price and budget
	 */
	private Bundle computeGoodsBundle() {
		Bundle wanted = new Bundle();
		double budget = incomeValue * preferences.get(Preference.goodsAlloc);
		
		//utility functions.
		double ratios[] = new double[3];
		double craftUtil = preferences.get(Preference.craftUtil);
		double craftPrice = economy.prices[Resource.crafts.ordinal()];
		double goodUtil = preferences.get(Preference.goodUtil);
		double goodPrice = economy.prices[Resource.goods.ordinal()];
		double foodUtil = preferences.get(Preference.foodUtil);
		double foodPrice = economy.prices[Resource.food.ordinal()];
		ratios[0] = (craftUtil*craftPrice) / (goodUtil*goodPrice);// crafts/goods
		ratios[1] = (goodUtil * goodPrice) / (foodUtil*foodPrice);// goods/food
		ratios[2] = ratios[0]/ratios[1]; // crafts/food
		
		//m = p1x1 + p2x2 + p3x3...
		double crafts = Math.cbrt(((double)budget * ratios[0] * ratios[2]) / (economy.prices[Resource.goods.ordinal()]*economy.prices[Resource.crafts.ordinal()]*economy.prices[Resource.food.ordinal()]));
		wanted.insert(new ResourcePile(Resource.crafts,(int)crafts));
		wanted.insert(new ResourcePile(Resource.goods, (int) (crafts/ratios[0])));
		wanted.insert(new ResourcePile(Resource.food, (int) (crafts/ratios[2])));
		
		return wanted;
	}
	
	/**
	 * Gets the bundle representing the "goal" of upkeep.
	 */
	private Bundle computeUpkeepCap() {
		Bundle wanted = home.getCost().over(20);
		wanted = wanted.times(preferences.get(Preference.upkeepCap));
		return wanted;
	}
	
	/**
	 * Updates what this person wants to be building or buying as their next home. 
	 */
	private void updateHomePlan() {
		if((family.father != null && home == family.father.home) || (family.mother != null && home == family.mother.home)){	//we live at home and need to save up to make our own.
			if (realestate.size() == 0) allocations[2].setBuildingClass(new Shack());
			else{
				int rank = 0;
				for (Home house:realestate){
					if (house != null && house.getRank() > rank) rank = house.getRank();
				}
				allocations[2].setBuildingClass(Home.getRankedHome(rank+1));
			}
		}
		else{
			if (home == null) allocations[2].setBuildingClass(new Shack());
			else allocations[2].setBuildingClass(Home.getRankedHome(home.getRank()+1));
		}
	}
	
	/**
	 * Gets either the bundle representing all the goods this agent has or an empty bundle.
	 * Is used as the maximum limit on what this person will give to their children this year.
	 */
	private Bundle computeChildSupport() {
		Bundle support = new Bundle();
		for(Person child:family.children){	//determine weights of required support. Always give the allocation your children if they exist.
			if (child.alive){
				support.insert(income);
				break;
			}
		}
		return support;
	}

	public boolean willWorkFor(int wage) {
		
		return (wage > minimumWage);
	}

	/**
	 * Moves self, wife, and children living at home to better home, if available.
	 */
	public void move() {
		int homeRank = (home == null?0:((realAge > 12 && livesAtHome())?0:home.getRank()));	//should encourage moving out of their parent's house.
		Home newHome = home; 
		Iterator<Home> i = realestate.iterator();
		while(i.hasNext()){
			Home estate = i.next();
			if(estate == null){	//Clean out any collapsed buildings.
				i.remove();
				continue;
			}
			int estateRank = estate.getRank(); 
			if (estateRank > homeRank || (estateRank == homeRank && estate.currentImprovement > (home == null? 0 :home.currentImprovement))){
				newHome = estate;
				homeRank = estateRank;
			}
		}
		if (newHome != home){
			if(family.spouse != null && family.spouse.alive) {
				family.spouse.home = newHome;	//move your spouse with you.
				newHome.occupants.add(family.spouse);
			}
			for (Person child: family.children){
				if (child.home == home) {
					child.home = newHome;	//If they live with me now, they move with me.
					newHome.occupants.add(child);
				}
			}
			newHome.occupants.add(this);


			if(home != null) realestate.add(home);
			home = newHome;
			realestate.remove(newHome);
		}
	}

	/**
	 * @return Whether this lives with its parents.
	 */
	public boolean livesAtHome() {
		return (home != null && ((family.father != null && home == family.father.home) || (family.mother != null && home == family.mother.home)));
	}
	
	/**
	 * @param homeowner Utility of the proposer
	 * @return Whether this accepts. 
	 */
	public boolean acceptProposal(Person homeowner){
		return (homeowner.utility > utility * preferences.get(Preference.love));	//if they make at least love * my income.
	}
	
	/**
	 * @return The maximum difference between utility acceptable.
	 */
	public double getUtilityThreshold() {
		return utility * preferences.get(Preference.love);
	}
	
	private void computeUtility(Bundle resources) {
		utility = 0.0;
		for (ResourcePile pile:resources){
			if (pile.type == Resource.food) utility += Math.max(0.0, preferences.get(Preference.foodUtil)*Math.log(pile.amount)+0.2);
			else if (pile.type == Resource.goods) utility += Math.max(0.0, preferences.get(Preference.goodUtil)*Math.log(pile.amount)+0.2);
			else if (pile.type == Resource.crafts) utility += Math.max(0.0, preferences.get(Preference.craftUtil)*Math.log(pile.amount)+0.2);
		}
		utility += (home == null?0:home.currentImprovement) * preferences.get(Preference.homeUtil);
	}
	
	private void refreshListings(){
		listings.clear();
		for(Home curr:realestate){
			if(curr.currentImprovement > 0){
				listings.add(new Listing((int) (curr.getCost().getValue() * (1-preferences.get(Preference.greed))), curr, this));
			}
		}
	}

	public void wed(Person homeowner) {
		homeowner.setSpouse(this);
		setSpouse(homeowner);
		home.occupants.remove(this);	//leave old home
		if(!livesAtHome() && home != null){
			realestate.add(home);
		}
		home = homeowner.home;
		home.occupants.add(this);
	}

	public void dropListing(Listing sold){
		listings.remove(sold);
		realestate.remove(sold);
		City.listings.remove(sold);
	}

	public void newRealestate(Home property) {
		if (property != null)realestate.add(property);
	}

	public Person tryMate() {
		if (family.spouse.alive && home != null && home.occupants.size() < home.capacity && rand.nextDouble() < 0.85){
			return new Person(family.spouse, this);
		}
		return null;
	}
}