package city.factories;

import people.Person.SkillType;
import city.Bundle;
import city.Factory;
import city.ResourcePile;
import city.ResourcePile.Resource;
import city.Task;

public class WorkmansHut extends Factory {

	private static Bundle cost = new Bundle(new ResourcePile(Resource.wood, 15));

	protected WorkmansHut()
	{
		super(cost, 3);
		name = "Workman's Hut";
		
		tasks.add(new Task(Resource.wood, 2, 25,SkillType.logger));		//Huts are Terrible sources of income.
		
		tasks.add(new Task(Resource.stone, 2, 25,SkillType.quarryer));
		
		tasks.add(new Task(Resource.metal, 1, 5,SkillType.miner));		//1 yr = 5 metal
		
		tasks.add(new Task(Resource.tools, Resource.metal, 1, 10, .5,SkillType.blackSmith));	//1 yr + 20 metal = 10 tools
		
		tasks.add(new Task(Resource.crops, 3, 30,SkillType.farmer));		//1 unskilled person can grow 30 units.
		
		tasks.add(new Task(Resource.food, Resource.crops, 2, 20, 0.667,SkillType.chef));	//30 crops to 20 food	
	}

}
