package fr.iban.lands.objects;

import fr.iban.lands.enums.LandType;

public class SystemLand extends Land {

	public SystemLand(int id, String name) {
		super(id, name);
		setType(LandType.SYSTEM);
	}

}
