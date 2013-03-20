package city.factories;

import people.Person.SkillType;
import city.Bundle;
import city.Factory;
import city.ResourcePile;
import city.ResourcePile.Resource;
import city.Task;

public class CobblersHut extends Factory {

	static Bundle costs = new Bundle(new ResourcePile(Resource.wood,125)); 
	
	public CobblersHut(int ordinal)
	{
		super(costs, 5,ordinal);
		name = "Cobbler's Hut";
		tasks.add(new Task(Resource.stone, 3, 25,SkillType.quarryer));	//2x efficiency from homestead. Still slow.
	}

	public CobblersHut(CobblersHut clone){
		super(costs, 5,clone.ordinal, clone.tasks);
		name = clone.name;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends Factory> E shallowClone() {
		// TODO Auto-generated method stub
		return (E) new CobblersHut(this);
	}
	
}
