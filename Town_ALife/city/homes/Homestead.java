package city.homes;

import city.Bundle;
import city.Home;
import city.ResourcePile;
import city.ResourcePile.Resource;

public class Homestead extends Home {

	static ResourcePile[] costPiles = { new ResourcePile(Resource.stone,50), new ResourcePile(Resource.wood, 100), new ResourcePile(Resource.metal, 10)};
	static Bundle costs = new Bundle(costPiles);
	
	public Homestead()
	{
		super(costs, 5, 3.0);
		
		name = "Homestead";
	}

}
