package decanoe.brewery;

import decanoe.brewery.brewing_utils.DataLoader;
import decanoe.brewery.brewing_utils.ModPotionUtils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Brewery implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "brewery";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// use "./gradlew genSources" once to setup the project
	// run with "./gradlew runClient"
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModPotionUtils.register_potions();
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DataLoader());
	}
	public static Identifier makeID(String name) {
		return Identifier.of(MOD_ID, name);
	}
}