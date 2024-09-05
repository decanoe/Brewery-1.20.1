package decanoe.brewery.brewing_utils;

import java.io.InputStream;
import java.util.Map.Entry;

import decanoe.brewery.Brewery;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class DataLoader implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return new Identifier("brewery", "data_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        for (Entry<Identifier, Resource> entry : manager.findResources("ingredient_data", id->id.toString().endsWith(".json")).entrySet()) {
            Brewery.LOGGER.info("try loading " + entry.getKey().toString());
            try(InputStream stream = entry.getValue().getInputStream()) {
                Brewery.LOGGER.info("json " + entry.getKey().toString() + " loaded");
            } catch (Exception e) {
                Brewery.LOGGER.info("Error while loading json " + entry.getKey().toString(), e);
            }
        }
    }
}
