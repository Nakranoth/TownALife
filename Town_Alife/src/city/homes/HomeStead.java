package city.homes;

import city.Home;
import city.Resource;
import city.Task;

public class HomeStead extends Home {

	static Resource[] costs = { new Resource("Stone",25), new Resource("Wood", 50)};	//3 man/years of solid homestead work. 
	
	public HomeStead()
	{
		super(costs,3);
		
		Resource[] output1 = {new Resource("Wood",1)};		//always use 1 for amount in tasks.
		addTask(new Task(output1, 1, 2, .25));		//Homesteads are Terrible sources of income.
		
		Resource[] output2 = {new Resource("Stone",1)};
		addTask(new Task(output2, 1, 2, .25));
		
		Resource[] output3 = {new Resource("Metal",1)};
		addTask(new Task(output3, 1, 1, .05));		//1 yr = 5 metal
		
		Resource[] output4 = {new Resource("Tool",1)};
		Resource[] input4 = {new Resource("Metal",1)};
		addTask(new Task(output4, input4, 1, 1, .1, .5));	//1 yr + 20 metal = 10 tools
	}

	@Override
	public String toString() {
		return "Homestead";
	}
}
