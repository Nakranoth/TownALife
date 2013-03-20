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
	
	private double unthrottledOutputQuantity;
	private double unthrottledInputQuantity;
	
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
	 * Evaluates this task's net output.
	 */
	public ResourcePile getInput(){
		if(input == null) return new ResourcePile(Resource.wood,0);
		return new ResourcePile(input,inputQuantity);
	}
	
	public ResourcePile getUnthrottledInput() {
		if(input == null) return new ResourcePile(Resource.wood,0);
		return new ResourcePile(input,unthrottledInputQuantity);
	}
	
	public ResourcePile getOutput(){
		return new ResourcePile(output,outputQuantity);
	}
	
	public ResourcePile getUnthrottledOutput() {
		return new ResourcePile(output,unthrottledOutputQuantity);
	}
	
	/**
	 * Handles all computation for actually fufilling a task.
	 * @param workers: The list of workers operating on the task
	 * @param availResources: The portion of resource bundle of the correct type. Is modified by this function
	 * @return the resulting resource pile.
	 */
	public ResourcePile doWork(LinkedList<Person> workers, ResourcePile availResources){
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
	 * @param resources Available resources for throttling
	 * @param scale Used to upscale the throttle
	 */
	public void reGuess(Bundle resources, double scale){
		double throttled = getThrottle(resources.getResource(input).amount);
		outputQuantity = Math.min(throttled, getReturns(maxWorkers * Math.max(1,City.avgSkill[skill.ordinal()])));
		outputQuantity = Math.min(outputQuantity, (City.economy.getExcessDemand(output) * outputQuantity / City.economy.getMaxSuppply(output.ordinal())))*scale;
		inputQuantity = getCosts(outputQuantity);
	}
	
	/**
	 * Completely unthrottled version for finding max output
	 */
	public void reGuess() {
		unthrottledOutputQuantity = getReturns(maxWorkers * Math.max(1,City.avgSkill[skill.ordinal()]));
		unthrottledInputQuantity = getCosts(unthrottledOutputQuantity);
	}
	
	/**
	 * @return The number of workers required to get the appropriate output.
	 */
	public int getWorkers(){
		return Math.min(maxWorkers,(int)Math.ceil(outputQuantity / efficency / City.avgSkill[skill.ordinal()]));
	}
	
	/**
	 * Returns the maximum amount of effective OUTPUT given an available resource pool.
	 */
	public double getThrottle(double availResources){	
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
