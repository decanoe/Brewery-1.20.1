package decanoe.brewery.brewing_utils;

import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.google.gson.Gson;
import decanoe.brewery.Brewery;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return new Identifier("brewery", "reload_listener");
    }

    @Override
    public void reload(ResourceManager manager) {
        for (Entry<Identifier, Resource> entry : manager.findResources("ingredients", id->id.toString().endsWith(".json")).entrySet()) {
            try {
                Resource resource = entry.getValue();

                String content = new String(resource.getInputStream().readAllBytes());
                Map<String, ?> json = new Gson().fromJson(content, Map.class);

                if (json == null) {
                    Brewery.LOGGER.info("Error while loading ingredient : " + entry.getKey().toString());
                    return;
                }



            } catch (Exception e) {
                Brewery.LOGGER.info("Error while loading ingredient : " + entry.getKey().toString(), e);
            }
        }
    }
}
