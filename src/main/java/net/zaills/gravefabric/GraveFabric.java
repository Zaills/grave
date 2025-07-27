package net.zaills.gravefabric;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.zaills.gravefabric.block.BaseGraveBlock;
import net.zaills.gravefabric.block.entity.GraveBlockEntity;
import net.zaills.gravefabric.config.GraveConfig;

public class GraveFabric implements ModInitializer {
	public static final String MOD_ID = "grave-fabric";

	public static final GraveConfig CONFIG = GraveConfig.createAndLoad();

	public static final Block BASE_GRAVE = new BaseGraveBlock(AbstractBlock.Settings.create().strength(0.8f, -1f));
	public static final BlockItem GRAVE_ITEM = new BlockItem(BASE_GRAVE, new net.minecraft.item.Item.Settings());
	public static BlockEntityType<GraveBlockEntity> GRAVE_ENTITY;

	@Override
	public void onInitialize() {

		Registry.register(Registries.BLOCK, RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "grave")), BASE_GRAVE);
		Registry.register(Registries.ITEM, RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "grave")), GRAVE_ITEM);
		GRAVE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, "grave"), FabricBlockEntityTypeBuilder.create(GraveBlockEntity::new, BASE_GRAVE).build());
	}
}