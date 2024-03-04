package net.zaills.grave;

import dev.emi.trinkets.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.zaills.grave.block.GraveBlock;
import net.zaills.grave.block.Grave_notype;
import net.zaills.grave.block.entity.GraveBlockEntity;
import net.zaills.grave.config.GraveConfig;

import org.apache.commons.compress.compressors.lz77support.LZ77Compressor.Block.BlockType;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public class Grave implements ModInitializer {

	public static final GraveBlock GRAVE = new Grave_notype(QuiltBlockSettings.of(Material.DECORATION).strength(0.8f, -1f));
	public static BlockEntityType<GraveBlockEntity> GRAVE_ENTITY;

	public static final GraveConfig CONFIG = GraveConfig.createAndLoad();

	@Override
	public void onInitialize(ModContainer mod) {
		LoggerFactory.getLogger("grave").info("Grave Initializing");
		Registry.register(Registry.BLOCK, new Identifier("grave", "grave"), GRAVE);
		GRAVE_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "grave:grave", QuiltBlockEntityTypeBuilder.create(GraveBlockEntity::new, GRAVE).build(null));
	}

	public static void Place(World world, Vec3d pos, PlayerEntity player) {
		if (world.isClient) {
			return;
		}

		final int worldMin = world.getDimension().minY();
		final int worldMax = world.getDimension().height() - worldMin;

		// Create a default grave state for placing
		BlockState graveState = GRAVE
			.getDefaultState()
			.with(Properties.HORIZONTAL_FACING, player.getHorizontalFacing());

		BlockPos originBlock = new BlockPos(pos.x, (pos.y < worldMin) ? worldMin : pos.y, pos.z);

		DefaultedList<ItemStack> inv = DefaultedList.of();
		inv.addAll(player.getInventory().main);
		inv.addAll(player.getInventory().armor);
		inv.addAll(player.getInventory().offHand);
		
		for (Pair<SlotReference, ItemStack> pair : TrinketsApi.getTrinketComponent(player).get().getAllEquipped()) {
			inv.add(pair.getRight());
		}

		for (BlockPos offsetBlock : BlockPos.iterateOutwards(originBlock, 5, 5, 5)) {
			// Not within world bounds, then skip
			if (worldMin > offsetBlock.getY() || offsetBlock.getY() > worldMax) {
				continue;
			}

			// Check to see if the block is within the world border
			if (!world.getWorldBorder().contains(offsetBlock)) {
				continue;
			}

			// Look for air block to place on from offset
			Optional<BlockPos> airBlock = place_top(offsetBlock, world);

			// If no valid position found, then skip offset
			if (airBlock.isEmpty()) {
				continue;
			}

			// Get the placing block
			BlockPos placingBlock = airBlock.get();

			// Attempt to place block
			if (!world.setBlockState(placingBlock, graveState)) {
				continue;
			}

			// Set block data 
			GraveBlockEntity graveBlockEntity = new GraveBlockEntity(placingBlock, graveState);
			graveBlockEntity.setInv(inv);
			graveBlockEntity.setOwner(player.getGameProfile());
			graveBlockEntity.setXp(player.totalExperience);
			graveBlockEntity.setLocation(placingBlock);
			graveBlockEntity.setType(get_type(world.getBlockState(placingBlock).getBlock()));
			graveBlockEntity.markDirty();
			
			world.addBlockEntity(graveBlockEntity);

			System.out.println(player.getName() + "'s grave spawn at: " + placingBlock.getX() + ", " + placingBlock.getY() + ", " + placingBlock.getZ());
			if (CONFIG.Get_grave_coord()) {
				player.sendMessage(Text.of("Grave spawn at: " + placingBlock.getX() + ", " + placingBlock.getY() + ", " + placingBlock.getZ()), false);
			}

			return;
		}

		// Unable to place grave enywhere (skill issue), drop items
		player.getInventory().dropAll();
	}

	static int get_type(Block bl){
		if (!Objects.equals(bl.getName().getString(), "Air")){
			return 1;
		}

		return 0;
	}

	static Optional<BlockPos> place_top(BlockPos pos, World world) {
		BlockPos npos = pos;

		while (world.getBlockState(npos).getMaterial() != Material.AIR) {
			npos = npos.add(0, 1, 0);

			if (npos.getY() > world.getDimension().height()) {
				return Optional.empty();
			}
		}

		return Optional.of(npos);
	}
}
