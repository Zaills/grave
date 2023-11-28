package net.zaills.grave.client;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.zaills.grave.Grave;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class GraveClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		BlockEntityRendererRegistry.register(Grave.GRAVE_ENTITY, GraveRenderer::new);
	}
}
