package city.homes;

import city.Bundle;
import city.Home;
import city.ResourcePile;
import city.ResourcePile.Resource;

public class Villa extends Home {

	static ResourcePile[] costPiles = { new ResourcePile(Resource.stone,75), new ResourcePile(Resource.wood, 125), new ResourcePile(Resource.metal, 50), new ResourcePile(Resource.tools, 5)};
	static Bundle costs = new Bundle(costPiles);
	
	public Villa()
	{
		super(costs, 7, 2.7, 7.0);
		
		name = "Villa";
	}

}
