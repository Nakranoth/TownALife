package city;

import java.util.ArrayList;
import java.util.Random;

public abstract class Factory extends Building {


	private static final Random rand = new Random();
	protected ArrayList<Task> tasks = new ArrayList<Task>();
	protected Task mostProfitable = null;
	
	protected Factory(Bundle cost, int capacity) {
		super(cost, capacity);
	}

	public ArrayList<Task> getTasks() {
		return tasks;
	}
	

	public int getProfitability(){
		Task profitable = null;
		int forecast = 0;
		
		for(Task curr:tasks){
			int currOut = City.economy.prices[curr.getOutput().type.ordinal()] * curr.getOutput().amount;
			int currCost = City.economy.prices[curr.getInput().type.ordinal()] * curr.getInput().amount;
			if (currOut-currCost > forecast || (currOut-currCost == forecast && rand.nextBoolean())){
				profitable = curr;
				forecast = currOut-currCost;
			}
		}
		mostProfitable = profitable;
		return forecast;
	}
	
	public void updateGuesses(){
		for (Task curr:tasks){
			curr.reGuess(City.avgSkill[curr.getSkill().ordinal()]);
		}
	}
	
}
