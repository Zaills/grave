package net.zaills.grave;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.zaills.grave.block.GraveBlock;
import net.zaills.grave.block.entity.GraveBlockEntity;
import net.zaills.grave.config.GraveConfig;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Grave implements ModInitializer {
	public static final String MOD_ID = "grave";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static  final GraveBlock GRAVE = new GraveBlock(QuiltBlockSettings.of(Material.ORGANIC_PRODUCT).strength(0.8f, -1f));
	public static BlockEntityType<GraveBlockEntity> GRAVE_ENTITY;


	public static final GraveConfig CONFIG = GraveConfig.createAndLoad();

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Grave Initializing");
		Registry.register(Registry.BLOCK, new Identifier("grave", "grave"), GRAVE);
		GRAVE_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "grave:grave", QuiltBlockEntityTypeBuilder.create(GraveBlockEntity::new, GRAVE).build(null));
		Registry.register(Registry.ITEM, new Identifier("grave", "grave"), new BlockItem(GRAVE, new Item.Settings()));
	}

	public static void Place(World world, Vec3d pos, PlayerEntity player){
		if (world.isClient)
			return;
		BlockPos bp;
		if (pos.y - 1 <= world.getDimension().minY()) {
			bp = new BlockPos(new BlockPos(pos.x, world.getDimension().minY(), pos.z));
		}
		else {
			bp = new BlockPos(pos.x, pos.y - 1, pos.z);
		}

		DefaultedList<ItemStack> inv = DefaultedList.of();
		inv.addAll(player.getInventory().main);
		inv.addAll(player.getInventory().armor);
		inv.addAll(player.getInventory().offHand);

		boolean placed = false;

		for (BlockPos gP : BlockPos.iterateOutwards(bp.add(new Vec3i(0, 1, 0)), 5, 5, 5)){
			if (bp.getY() > world.getDimension().minY() || bp.getY() < world.getDimension().height() - world.getDimension().minY()){
				BlockState gS = Grave.GRAVE.getDefaultState().with(Properties.HORIZONTAL_FACING, player.getHorizontalFacing());

				placed = world.setBlockState(gP, gS);
				GraveBlockEntity graveBlockEntity = new GraveBlockEntity(gP, gS);
				graveBlockEntity.setInv(inv);
				graveBlockEntity.setOwner(player.getGameProfile());
				graveBlockEntity.setXp(player.totalExperience);
				world.addBlockEntity(graveBlockEntity);

				graveBlockEntity.markDirty();

				player.totalExperience = 0;
				player.experienceLevel = 0;
				player.experienceProgress = 0;

				System.out.println(player.getName() + " grave spawner at: " + gP.getX() + ", " + gP.getY() + ", " + gP.getZ());

				break;
			}
		}

		if (!placed){
			player.getInventory().dropAll();
		}
	}
}
