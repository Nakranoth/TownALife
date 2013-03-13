package city.factories;

import people.Person.SkillType;
import city.Bundle;
import city.Factory;
import city.ResourcePile;
import city.ResourcePile.Resource;
import city.Task;

public class BakersHut extends Factory {

	static ResourcePile[] costPiles = { new ResourcePile(Resource.stone,50), new ResourcePile(Resource.wood, 75)};
	static Bundle costs = new Bundle(costPiles); 
	
	public BakersHut(int ordinal)
	{
		super(costs, 2,ordinal);
		name = "Baker's Hut";
		tasks.add(new Task(Resource.food, Resource.crops, 2, 60, 0.667,SkillType.chef));	//60 crops to 40 food	
	}

	public BakersHut(BakersHut clone){
		super(costs, 5,clone.ordinal, clone.tasks);
		name = clone.name;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends Factory> E shallowClone() {
		// TODO Auto-generated method stub
		return (E) new BakersHut(this);
	}
}
