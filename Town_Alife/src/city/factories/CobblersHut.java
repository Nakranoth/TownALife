package city.factories;

import city.Factory;
import city.Resource;
import city.Task;

public class CobblersHut extends Factory {

	static Resource[] costs = {new Resource("Wood",25)}; 
	
	public CobblersHut()
	{
		super(costs,5);
		name = "Cobbler's Hut";
		Resource[] output = {new Resource("Stone", 1)};
		addTask(new Task(output, 1, 5, .5));	//2x efficiency from homestead. Still slow.
	}
	
}
