package net.zaills.gravefabric.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.zaills.gravefabric.block.entity.GraveBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;

@Environment(EnvType.CLIENT)
public class GraveBlockEntityRenderer implements BlockEntityRenderer<GraveBlockEntity> {
	private static PlayerEntityModel modelToRender;

	public GraveBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		modelToRender = new PlayerEntityModel(context.getLayerModelPart(EntityModelLayers.PLAYER), true);
	}

	@Override
	public void render(GraveBlockEntity entity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
		if (entity.getWorld() == null || entity.getSavedOwner() == null) return;

		MinecraftClient client = MinecraftClient.getInstance();
		ProfileComponent profile = entity.getSavedOwner();

		RenderLayer renderLayer = SkullBlockEntityRenderer.getTranslucentRenderLayer(
				client.getSkinProvider().getSkinTextures(profile.gameProfile()).texture());
		VertexConsumer consumer = vertexConsumers.getBuffer(renderLayer);
		//modelToRender.thinArms = isSlim(profile.gameProfile());

		int lightLevel = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up());

		matrices.push();

		matrices.translate(0.5, 2.25, 0.5);
		matrices.scale(1f, -1f, 1f);

		modelToRender.render(matrices, consumer, lightLevel, overlay);

		matrices.pop();
	}

	/*
	This was written in a state of delirium
	It works, don't touch it,
	Don't question it
	- Cup
	 */
	public static boolean isSlim(GameProfile profile) {
		if (profile == null) return false;

		Property texturesProperty = profile.getProperties().get("textures").stream().findFirst().orElse(null);
		if (texturesProperty == null) return false;

		try {
			String base64 = texturesProperty.value().trim().replace("\"", "");
			byte[] decoded = Base64.getDecoder().decode(base64);

			String json = new String(decoded);
			JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
			JsonObject skin = obj.getAsJsonObject("textures").getAsJsonObject("SKIN");

			if (skin.has("metadata")) {
				JsonObject metadata = skin.getAsJsonObject("metadata");
				return "slim".equalsIgnoreCase(metadata.get("model").getAsString());
			}
		} catch (IllegalArgumentException e) {
			System.err.println("Failed to decode skin texture: " + e.getMessage());
		}

		return false;
	}
}
