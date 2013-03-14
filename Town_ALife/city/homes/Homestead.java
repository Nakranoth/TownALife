package city.homes;

import city.Bundle;
import city.Home;
import city.ResourcePile;
import city.ResourcePile.Resource;

public class Homestead extends Home {

	static ResourcePile[] costPiles = { new ResourcePile(Resource.stone,75), new ResourcePile(Resource.wood, 150), new ResourcePile(Resource.metal, 50), new ResourcePile(Resource.tools, 10)};
	static Bundle costs = new Bundle(costPiles);
	
	public Homestead()
	{
		super(costs, 5, 2.3, 3.0);
		
		name = "Homestead";
	}

}
