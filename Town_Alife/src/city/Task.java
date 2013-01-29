package city;

public class Task {
	private	Resource input, output;
	private int minWorkers, maxWorkers;
	private double efficency, matEfficency;
	
	public Task(Resource output, int minWorkers, int maxWorkers, double efficency) {
		init(output, (Resource)null, minWorkers, maxWorkers, efficency, 0);
	}
	
	public Task(Resource output, Resource input, int minWorkers, int maxWorkers, double efficency, double matEfficency) {
		init(output,input, minWorkers, maxWorkers, efficency, matEfficency);
	}
	
	private void init(Resource output, Resource input, int minWorkers, int maxWorkers, double efficency, double matEfficency){
		this.input = input;
		this.output = output;
		this.minWorkers = minWorkers;
		this.maxWorkers = maxWorkers;
		this.efficency = efficency;
		this.matEfficency = matEfficency;
	}
	
	public Resource getInput(){
		return input;
	}
	
	public Resource getOutput(){
		return output;
	}
	
	public double getMatEff(){
		return matEfficency;
	}
	
	public int getMinWorkers(){
		return minWorkers;
	}
	
	public int getMaxWorkers(){
		return maxWorkers;
	}
	
	public int getThrottle(int availResources){	//returns the maximum number of effective ticks given an available resource pool.
		return (int) ((int) (availResources * matEfficency) / efficency);
	}
	
	public int getReturns(int ticks){
		return (int) (ticks * efficency);
	}
	
	public int getCosts(int returns){
		if (matEfficency <= 0) return 0;
		return (int) (returns / matEfficency);
	}
}
