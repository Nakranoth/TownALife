package city;

import java.util.LinkedList;

import people.Person;
import people.Person.SkillType;
import city.ResourcePile.Resource;

public class Task {
	private	Resource input, output;
	private int maxWorkers;
	private double efficency, matEfficency;
	private SkillType skill;
	
	private double inputQuantity = 0;
	private double outputQuantity;
	
	public int lastUpdated = 0;
	
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
		
		outputQuantity=getReturns(maxWorkers * Math.max(1,City.avgSkill[skill.ordinal()]));
		inputQuantity=getCosts(outputQuantity);
	}
	
	public Task(Task task) {	//copy constructor. Used for making factories understand themselves better.
		this(task.output, task.input, task.maxWorkers, task.efficency, task.matEfficency, task.skill);
	}

	/*
	 * This pair designed for evaluating this task's net output.
	 */
	public ResourcePile getInput(){
		if(input == null) return new ResourcePile(Resource.wood,0);
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
	public ResourcePile doWork(LinkedList<Person> workers, ResourcePile availResources){
		lastUpdated++;	//updated for next year. Keeps real values, rather than guessing estimates.
		double skillPool = 0;
		double maxSkill = 0;	//Used to detect throttling
		for(Person worker:workers){
			skillPool += worker.doWork(skill);
		}
		if(input != null){
			maxSkill = skillPool;
			skillPool = Math.min(getThrottle(availResources.amount), skillPool);	//throttle if necessary. Cannot be larger than int skillpool.
		}
		
		outputQuantity = getReturns(skillPool);
		inputQuantity = getCosts(outputQuantity);
		
		if(input != null){
			availResources.amount -= inputQuantity;
		}
		
		double localOutput = outputQuantity;
		
		if(maxSkill > skillPool){//causes future math to consider cost and gains as if enough resources had been present.
			outputQuantity = getReturns(maxSkill);
			inputQuantity = getCosts(outputQuantity);
		}
		
		
		return new ResourcePile(output,localOutput);
	}
	
	/**
	 * Forces task to guess the output based on the average skill in the work force. 
	 * @param resources 
	 */
	public void reGuess(Bundle resources){
		if(City.year > lastUpdated){
			double throttled = getThrottle(resources.getType(input).amount);
			outputQuantity = Math.min(throttled, getReturns(maxWorkers * Math.max(1,City.avgSkill[skill.ordinal()])));
			inputQuantity = getCosts(outputQuantity);
			lastUpdated = City.year;
		}
	}
	
	public void reGuess() {
		if(City.year > lastUpdated){
			outputQuantity = getReturns(maxWorkers * Math.max(1,City.avgSkill[skill.ordinal()]));
			inputQuantity = getCosts(outputQuantity);
			lastUpdated = City.year;
		}
		
	}
	
	public int getMaxWorkers(){
		return maxWorkers;
	}
	
	public double getThrottle(double availResources){	//returns the maximum amount of effective OUTPUT given an available resource pool.
		if (matEfficency == 0) return Double.POSITIVE_INFINITY;
		return availResources * matEfficency;
	}
	
	private double getReturns(double skillPool){
		return (skillPool * efficency);
	}
	
	private double getCosts(double returns){
		if (matEfficency <= 0) return 0;
		return returns / matEfficency; //matEfficency is less than or equal to 1.
	}

	public SkillType getSkill() {
		return skill;
	}

}
