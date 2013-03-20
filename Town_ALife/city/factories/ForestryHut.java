package city.factories;

import people.Person.SkillType;
import city.Bundle;
import city.Factory;
import city.ResourcePile;
import city.ResourcePile.Resource;
import city.Task;

public class ForestryHut extends Factory {

	static Bundle costs = new Bundle(new ResourcePile(Resource.wood, 125));	//1 man/years of solid homestead work.

	public ForestryHut(int ordinal) {
		super(costs, 5, ordinal);
		
		name = "Forestry Hut";
		tasks.add(new Task(Resource.wood, 3, 25,SkillType.logger));	//2x efficiency from homestead. Still slow.
		tasks.add(new Task(Resource.crafts, Resource.wood, 2, 50, 0.25, SkillType.carpenter));
	}

	public ForestryHut(ForestryHut clone) {
		super(costs,5,clone.ordinal,clone.tasks);
		name = clone.name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Factory> E shallowClone() {
		return (E) new ForestryHut(this);
	}


}
