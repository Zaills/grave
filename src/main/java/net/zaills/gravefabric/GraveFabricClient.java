package net.zaills.gravefabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.zaills.gravefabric.client.GraveBlockEntityRenderer;

@Environment(EnvType.CLIENT)
public class GraveFabricClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		BlockEntityRendererFactories.register(GraveFabric.GRAVE_ENTITY, GraveBlockEntityRenderer::new);
	}
}
