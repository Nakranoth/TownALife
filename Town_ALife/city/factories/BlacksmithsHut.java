package city.factories;

import people.Person.SkillType;
import city.Bundle;
import city.Factory;
import city.ResourcePile;
import city.ResourcePile.Resource;
import city.Task;

public class BlacksmithsHut extends Factory {

	static ResourcePile[] costPiles = { new ResourcePile(Resource.stone,50), new ResourcePile(Resource.wood, 75)};
	static Bundle costs = new Bundle(costPiles); 
	
	public BlacksmithsHut(int ordinal)
	{
		super(costs, 1,ordinal);
		name = "Blacksmith's Hut";
//tasks.add(new Task(Resource.food, Resource.crops, 2, 20, 0.667,SkillType.chef));	//30 crops to 20 food	
		tasks.add(new Task(Resource.tools, Resource.metal, 4, 30, .5,SkillType.blackSmith));	//1 yr + 20 metal = 10 tools
		tasks.add(new Task(Resource.goods, Resource.metal, 3, 20, .5,SkillType.blackSmith));	//Very expensive.
	}

	public BlacksmithsHut(BlacksmithsHut clone){
		super(costs, 5,clone.ordinal, clone.tasks);
		name = clone.name;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends Factory> E shallowClone() {
		// TODO Auto-generated method stub
		return (E) new BlacksmithsHut(this);
	}
}
