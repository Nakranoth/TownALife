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
		if (lastUpdated < City.year || mostProfitable == null){
			lastUpdated = City.year;
			Task profitable = null;
			double forecast = 0;
			
			for(Task curr:tasks){
				curr.reGuess(resources);
				double currOut = City.economy.prices[curr.getOutput().type.ordinal()] * curr.getOutput().amount;
				double currCost = City.economy.prices[curr.getInput().type.ordinal()] * curr.getInput().amount;
				if (currOut-currCost > forecast || (currOut-currCost == forecast && rand.nextBoolean())){
					profitable = curr;
					forecast = currOut-currCost;
				}
			}
			mostProfitable = profitable;
			return forecast;
		}
		else{
			double output = City.economy.prices[mostProfitable.getOutput().type.ordinal()] * mostProfitable.getOutput().amount;
			double cost = City.economy.prices[mostProfitable.getInput().type.ordinal()] * mostProfitable.getInput().amount;
			return Math.max(output - cost, 0);
		}
	}
	
	public void updateGuesses(){
		if(lastUpdated < City.year){
			for (Task curr:tasks){
				curr.reGuess();
			}
			lastUpdated = City.year;
		}
	}
	
	/**
	 * Returns whether or not this should collapse this year.
	 * Called every year. 
	 */
	public boolean decay(){
		disuse++;
		if (disuse == 0) return false; //typical case.
		if (rand.nextInt(10) <= disuse){
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
			operatingCost.insert(task.getInput());
		}
		
		return operatingCost;
	}
	
	public int getTaskCapacity(){
		if (mostProfitable == null) getProfitability();
		if (mostProfitable == null) return 0;
		return mostProfitable.getMaxWorkers();
	}

	public double getProfitability() {
		return getProfitability(getOperatingCost());
	}
}
