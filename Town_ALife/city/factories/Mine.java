package city.factories;

import people.Person.SkillType;
import city.Bundle;
import city.Factory;
import city.ResourcePile;
import city.ResourcePile.Resource;
import city.Task;

public class Mine extends Factory {

	static Bundle costs = new Bundle(new ResourcePile(Resource.wood,250)); 
	
	public Mine(int ordinal)
	{
		super(costs, 5,ordinal);
		name = "Mine";
		tasks.add(new Task(Resource.metal, 5, 20,SkillType.miner));	//2x efficiency from homestead. Still slow.
	}

	public Mine(Mine clone){
		super(costs, 5,clone.ordinal, clone.tasks);
		name = clone.name;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends Factory> E shallowClone() {
		// TODO Auto-generated method stub
		return (E) new Mine(this);
	}
	
}
