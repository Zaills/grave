package net.zaills.grave.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.zaills.grave.Grave;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

@Environment(EnvType.CLIENT)
public class GraveClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		//BlockEntityRendererRegistry.register(Grave.GRAVE_ENTITY, GraveRenderer::new);
		BlockEntityRendererRegistryImpl.register(Grave.GRAVE_ENTITY, GraveRenderer::new);
	}
}
