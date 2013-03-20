package city;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import people.Person;
import people.Person.SkillType;

public abstract class Factory extends Building {

	private static final Random rand = new Random();
	protected ArrayList<Task> tasks = new ArrayList<Task>();
	protected Task mostProfitable = null;
	public int lastUpdated = 0;
	public int ordinal;
	
	private int disuse;	//incremented each consecutive year of disuse. Causes chance of collapse.	
	
	public static ArrayList<Factory> samples = new ArrayList<Factory>();
	
	protected Factory(Bundle cost, int capacity, int ordinal) {
		super(cost, capacity);
		this.ordinal = ordinal;
	}

	public Factory(Bundle cost, int capacity, int ordinal, ArrayList<Task> tasks) {
		this(cost, capacity, ordinal);
		for(Task task:tasks){
			this.tasks.add(new Task(task));
		}
	}

	public abstract <E extends Factory> E shallowClone();
	
	public ArrayList<Task> getTasks() {
		return tasks;
	}
	

	public double getProfitability(Bundle resources){
		lastUpdated = City.year;
		Task profitable = null;
		double forecast = 0;
		double cumulativeWeight = 0;	//used to allow sequential computation of probabilities. 
		
		for(Task curr:tasks){
			curr.reGuess(resources,1.0);
			double currOut = City.economy.prices[curr.getOutput().type.ordinal()] * curr.getOutput().amount;
			double currCost = City.economy.prices[curr.getInput().type.ordinal()] * curr.getInput().amount;
			double currProfit = currOut - currCost;
			if (currProfit + 1 > currProfit) 
				cumulativeWeight += currProfit;
			if (currProfit > 0 && rand.nextDouble() < currProfit / cumulativeWeight ){
				profitable = curr;
				forecast = currProfit;
			}
		}
		if (profitable != null){
			profitable.reGuess(resources,cumulativeWeight/forecast);
			forecast = City.economy.prices[profitable.getOutput().type.ordinal()] * profitable.getOutput().amount 
						- City.economy.prices[profitable.getInput().type.ordinal()] * profitable.getInput().amount;
		} 
		mostProfitable = profitable;
		return forecast;
	}
	
	public void updateGuesses(){
		for (Task curr:tasks){
			curr.reGuess();
			curr.reGuess(new Bundle(curr.getUnthrottledInput()), 1.0);	//adjusts for market saturation.
		}
		lastUpdated = City.year;
	}
	
	/**
	 * Returns whether or not this should collapse this year.
	 * Called every year. 
	 */
	public boolean decay(){
		disuse++;
		if (disuse == 0) return false; //typical case.
		if (rand.nextInt(20) <= disuse){
			ordinal = -1;	//lets the corp know it died. Will refund operating costs next year.
			return true;
		}
		else return false;
	}
	
	/**
	 * Wrapper through to the task's do work function. Also updates disuse info.
	 */
	public ResourcePile doWork(LinkedList<Person> workers, Bundle availResources){
		disuse = -1;
		return mostProfitable.doWork(workers, availResources.getResource(mostProfitable.getInput().type));
	}

	/**
	 * Returns the skilltype used by the most profitable task.
	 * Assumes that the most profitable has already been computed.
	 */
	public SkillType getRelevantSkill() {
		if (mostProfitable == null) return null;
		return mostProfitable.getSkill();
	}

	public Bundle getOperatingCost() {
		Bundle operatingCost = new Bundle();
		
		for(Task task:tasks){
			task.reGuess();
			operatingCost.insert(task.getUnthrottledInput());
		}
		
		return operatingCost;
	}
	
	public int getTaskAdjustedCapacity(){
		if (mostProfitable == null) getProfitability();
		if (mostProfitable == null) return 0;
		
		return mostProfitable.getWorkers();
	}

	public double getProfitability() {
		return getProfitability(getOperatingCost());
	}

}
