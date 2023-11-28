package net.zaills.grave.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.zaills.grave.Grave;
import net.zaills.grave.block.entity.GraveBlockEntity;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

@Environment(EnvType.CLIENT)
public class GraveClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		BlockEntityRendererRegistry.register(Grave.GRAVE_ENTITY, (ctx) ->(BlockEntityRenderer<GraveBlockEntity>) (Object) new  GraveRenderer(ctx));
	}
}
