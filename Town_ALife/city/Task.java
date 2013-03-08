package city;

import java.util.ArrayList;

import people.Person;
import people.Person.SkillType;
import city.ResourcePile.Resource;

public class Task {
	private	Resource input, output;
	private int maxWorkers;
	private double efficency, matEfficency;
	private SkillType skill;
	
	private int inputQuantity = 0;
	private int outputQuantity = 0;
	
	public Task(Resource output, int maxWorkers, double efficency, SkillType skill) {
		this(output, (Resource)null, maxWorkers, efficency, 0.0D, skill);
	}
	
	public Task(Resource output, Resource input, int maxWorkers, double efficency, double matEfficency, SkillType skill){
		this.input = input;
		this.output = output;
		this.maxWorkers = maxWorkers;
		this.efficency = efficency;
		this.matEfficency = matEfficency;
		this.skill = skill;
		
		outputQuantity=getReturns(maxWorkers);
		inputQuantity=getCosts(outputQuantity);
	}
	
	/*
	 * This pair designed for evaluating this task's net output.
	 */
	public ResourcePile getInput(){
		return new ResourcePile(input,inputQuantity);
	}
	
	public ResourcePile getOutput(){
		return new ResourcePile(output,outputQuantity);
	}
	
	/**
	 * Handles all computation for actually fufilling a task.
	 * @param workers: The list of workers operating on the task
	 * @param availResources: The portion of resource bundle of the correct type. Is modified by this function
	 * @return the resulting resource pile.
	 */
	public ResourcePile doWork(ArrayList<Person> workers, ResourcePile availResources){
		int skillPool = 0;
		for(Person worker:workers){
			skillPool += worker.getSkill(skill);
		}
		if(input != null){
			skillPool = Math.min(getThrottle(availResources.amount), skillPool);	//throttle if necessary.
		}
		
		outputQuantity = getReturns(skillPool);
		inputQuantity = getCosts(outputQuantity);
		
		if(input != null){
			availResources.amount -= inputQuantity;
		}
		
		return new ResourcePile(output,outputQuantity);
	}
	
	/**
	 * Forces task to guess the output based on the average skill in the work force. 
	 * @param skill: Average skill in the work force.
	 */
	public void reGuess(int skill){
		outputQuantity = getReturns(skill);
		inputQuantity = getCosts(outputQuantity);
	}
	
	public int getMaxWorkers(){
		return maxWorkers;
	}
	
	public int getThrottle(int availResources){	//returns the maximum amount of effective skill given an available resource pool.
		return (int) ((int) (availResources * matEfficency) / efficency);
	}
	
	private int getReturns(int skill){
		return (int) (skill * efficency);
	}
	
	private int getCosts(int returns){
		if (matEfficency <= 0) return 0;
		return (int) (returns / matEfficency);
	}

	public Enum<SkillType> getSkill() {
		return skill;
	}
}
