package city.factories;

import city.Factory;
import city.Resource;
import city.Task;

public class ForestryHut extends Factory {

	static Resource[] costs = { new Resource("Wood", 25)};	//1 man/years of solid homestead work.

	public ForestryHut() {
		super(costs, 5);
		
		name = "Forestry Hut";
		Resource[] output = {new Resource("Wood", 1)};
		addTask(new Task(output, 1, 5, .5));	//2x efficiency from homestead. Still slow.
	}


}
