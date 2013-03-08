package people;

import java.util.ArrayList;
import java.util.Random;

import city.Bundle;
import city.City;
import city.Home;
import city.ResourcePile;
import city.ResourcePile.Resource;
import city.homes.Homestead;
import city.homes.Shack;
import city.homes.Villa;
import economy.Economy;
import error.BadSellerException;

/**
 * The actual agents within the city. Contains all attributes and logic for division of resources.
 * @author Nathan Killeen
 */
public class Person{
	
	private static String[] allocSet = {"year0Alloc","yearIAlloc","yearJAlloc","yearKAlloc","upkeepAlloc", "childAlloc", "newHomeAlloc"},	//all resource allocation weights
			yearSet = {"yearI","yearJ","yearK"},	//all forward thinking durations.
			utilitySet = {"craftUtil", "goodUtil", "foodUtil", "homeUtil"};	//All sources of utility
	private static final Economy economy = City.economy;	//easy pointer.
	
	
	public static enum SkillType {logger,quarryer,farmer,miner,blackSmith,carpenter,chef};	//all types of skills. Nearly map to Resources, but blacksmith is shared by goods and tools
	
	
	public int effectiveAge = 0;	//incremented by age() function. determines chance of death.
	public int realAge = 0;			//incremented directly from logic loop in city. Tracks age for moving out and mate finding purposes.
	public boolean alive = true;	//used for cleanup portions of processing.
	public boolean male;			//gender.
	
	protected Preferences preferences;	//all inherited things.
	
	private static Random rand = new Random();
	
	//Holds how much I made last year, used for resource allocation.
	public Bundle income = new Bundle();	//Will be changed by this and Economy each pass.
	long incomeValue;						//Used internally to track the base amount I made.
	
	public Bundle demandedGoods = new Bundle();	//Total demand across all bundles.
												//Becomes the results from trading each year.
	
	public Allocation[] allocations = new Allocation[8];	//goods, upkeep, newHome, support, operationalCosts, I, J, K // In that order
	private int[] skills = new int[SkillType.values().length];
	private ArrayList<Corporation> ownerships = new ArrayList<Corporation>();
	private ArrayList<Home> realestate = new ArrayList<Home>();//all buildings not home.
	private Family family = new Family();
	public Home home = null;
	
	public double utility;	//Generated health/happiness this year.
	
	/**
	 * Default junk guess for first run. Used simply as starting point to get more meaningful numbers from system.
	 * @param thisConstructor meaningless flag
	 */
	public Person(boolean thisConstructor)
	{
		this();
		preferences = new Preferences();
		//TODO Load preferences
		preferences.put("stubbornness", 20.0);	//Reevaluate inferiority in 20 years.
		preferences.put("goodsAlloc", 0.3);
		preferences.put("yearI", 5.0);
		preferences.put("yearIAlloc", 0.3);
		preferences.put("yearJ", 10.0);
		preferences.put("yearJAlloc", 0.2);
		preferences.put("yearK", 20.0);
		preferences.put("yearKAlloc", 0.1);
		
		preferences.put("upkeepAlloc", 0.1);	//Spending on home maintenance. Rolled into saving for new home if overflowing cap.
		preferences.put("upkeepCap", 3.0);		//If spending more than cap/20 of cost on upkeep, roll to next home savings.
		
		preferences.put("newHomeAlloc", 0.01);	//Base amount to save for new home if not in a villa. Meaningless if home is a villa.
		
		preferences.put("childAlloc",0.1);		//Part spent on all of your children. Rolls to next year's income if childless.
		
		preferences.put("generosity", 1.0);	//percentage of median salary they want to offer.
		preferences.put("greed", 0.2);	//minimum percentage net income. Caps salary, ignoring generosity.
		
		preferences.put("craftUtil", 1.0);
		preferences.put("goodUtil", 1.0);
		preferences.put("foodUtil", 5.0);
		preferences.put("homeUtil", 1.0);
		
		preferences.normalize(utilitySet, 1, 20);
		preferences.normalize(allocSet, 0.01, 1.0);
		preferences.sort(yearSet);
		preferences.setMinimum(yearSet, 1.0);
	}
	
	/**
	 * Constructor for initializing a person from a loaded preference set.
	 */
	public Person(Preferences prefs)
	{
		this();
		preferences = prefs;
	}
	
	/**
	 * The standard way agents are born.
	 */
	public Person(Person father, Person mother)//Parent A will always be male.
	{
		this();
		
		family.father = father;
		family.mother = mother;
		
		preferences = Preferences.GenPrefs(father.preferences, mother.preferences); 
		preferences.normalize(utilitySet, 1, 20);
		preferences.normalize(allocSet, 0.01, 1.0);
		preferences.sort(yearSet);
		preferences.setMinimum(yearSet, 1.0);
	}
	
	private Person() {
		male = rand.nextBoolean();
	}

	/**
	 * Sets up all allocation demands for this year.
	 * Also consolidates all demands into demandedGoods.
	 */
	public void readyAllocations(){
		allocations[4].demand = computeOperationalCosts();	//Operational costs
		incomeValue = income.getValue() - allocations[4].demand.minus(allocations[4].resources).getValue();//we immediately lose the value of demanded operational costs.
		allocations[0].demand = computeGoodsBundle();	//goods
		
		if(allocations[1].building != home)
		{
			if (home == null || family.father.home == home || family.mother.home == home)
			{
				allocations[1].goal = new Bundle();	//empty goal. yay.
			}
			else {
				allocations[1].goal = computeUpkeepCap();
				allocations[1].building = home;
			}
		}
		
		int upkeepDemandValue = (int) (incomeValue * preferences.get("upkeepAlloc"));
		allocations[1].demand = allocations[1].getDemand(upkeepDemandValue);
		//the difference between upkeepDemandValue and allocations[1].demand.getValue() gets added to allocations[2] (savings)
		Bundle upkeepRefund = allocations[1].refundExcessSupply();
		income.insert(upkeepRefund);
		
		//Update planned home.
		updateHomePlan();
		
		int homeSavings = (int) (incomeValue * preferences.get("newHomeAlloc") + upkeepRefund.getValue());
		allocations[2].refreshGoal();
		allocations[2].demand = allocations[2].getDemand(homeSavings);
		income.insert(allocations[2].refundExcessSupply());	//The value is still in the demand.
		
		allocations[3].goal = computeChildSupport();
		allocations[3].demand = allocations[3].getDemand((int) (incomeValue * preferences.get("childAlloc"))); 
		
		//TODO: handle 5-7
		//TODO: demandedGoods
	}
	

	/**
	 * Appropriately handles each allocation post trade.
	 * Includes shifting goods from demand into allocations, building things, etc.
	 */
	public void handleAllocations(){
		//check each allocation for enough to build
		//Consume goods in income allocation
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
					curr.saleOfOwnership(this, family.spouse, curr.getShares(this));
				}
				catch(BadSellerException e){
					System.err.println(e.getMessage());
				}
			}
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
							i.saleOfOwnership(this, curr, i.getShares(this)/numChildren);
						}
						catch(BadSellerException e){
							System.err.println(e.getMessage());
						}
					}
				}
			}
			if (numChildren == 0){	//no surviving heirs
				for(Corporation i:ownerships){
					i.redistribute(this);
				}
			}
		}
		family = null;	//makes sure the dead are eventually forgotten when their immediate family dies.
		City.alive.remove(this);
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
	 * Gets the value of this person's talent in SkillType
	 */
	public double getSkill(SkillType type)
	{
		skills[type.ordinal()] += 1;
		
		return Math.sqrt(Math.max(0, skills[type.ordinal()] - (effectiveAge < 40?0:(40 - effectiveAge)/2)));	//effective skill slowly deteriorates after 40.
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
			wanted.insert(curr.operatingCost.over(curr.getShares(this)));
		}
		
		return wanted;
	}
	
	/**
	 * Gets the bundle representing the goods that this person wants to consume this year, limited by price and budget
	 */
	private Bundle computeGoodsBundle() {
		Bundle wanted = new Bundle();
		long budget = (long) (incomeValue * preferences.get("year0Alloc"));
		
		//utility functions.
		double ratios[] = new double[3];
		ratios[0] = preferences.get("craftUtil")*economy.prices[Resource.crafts.ordinal()] / preferences.get("goodUtil")*economy.prices[Resource.goods.ordinal()];// crafts/goods
		ratios[1] = preferences.get("goodUtil")*economy.prices[Resource.goods.ordinal()] / preferences.get("foodUtil")*economy.prices[Resource.food.ordinal()];// goods/food
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
		wanted = wanted.times(preferences.get("upkeepCap"));
		//long budget = (long) (incomeValue * preferences.get("upkeepAlloc"));
		return wanted;
	}
	
	/**
	 * Updates what this person wants to be building or buying as their next home. 
	 */
	private void updateHomePlan() {
		if(male){
			if(home == family.father.home || home == family.mother.home){	//we live at home and need to save up to make our own.
				if (realestate.size() == 0) allocations[2].setBuildingClass(new Shack());
				else{
					double improve = 0;
					for (Home house:realestate){
						if (house.currentImprovement > improve) improve = house.currentImprovement;
					}
					if(improve > 0){
						allocations[2].setBuildingClass(new Homestead());
					}
					if(improve > 2){
						allocations[2].setBuildingClass(new Villa());
					}
					if(improve > 5){
						allocations[2].setBuildingClass(null);
					}
				}
			}
			else{
				if (home == null) allocations[2].setBuildingClass(new Shack());
				else if (home.getClass() == Shack.class) allocations[2].setBuildingClass(new Homestead());
				else if (home.getClass() == Homestead.class) allocations[2].setBuildingClass(new Villa());
				else if (home.getClass() == Villa.class) allocations[2].setBuildingClass(null);
			}
		}
		else{
			allocations[2].setBuildingClass(null);
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
}
