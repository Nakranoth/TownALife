package city.homes;

import city.Bundle;
import city.Home;
import city.ResourcePile;
import city.ResourcePile.Resource;

public class Shack extends Home {

	static ResourcePile[] costPiles = { new ResourcePile(Resource.stone,25), new ResourcePile(Resource.wood, 50)};	//3 man/years of solid homestead work.
	static Bundle costs = new Bundle(costPiles);
	
	public Shack()
	{
		super(costs,3, 2.1, 1.0);
		
		name = "Shack";
	}

}
