package net.zaills.grave;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Grave implements ModInitializer {

	public static  final GraveBlock GRAVE = new Grave_notype(QuiltBlockSettings.of(Material.DECORATION).strength(0.8f, -1f));
	public static BlockEntityType<GraveBlockEntity> GRAVE_ENTITY;

	public static final GraveConfig CONFIG = GraveConfig.createAndLoad();

	@Override
	public void onInitialize(ModContainer mod) {
		LoggerFactory.getLogger("grave").info("Grave Initializing");
		Registry.register(Registry.BLOCK, new Identifier("grave", "grave"), GRAVE);
		GRAVE_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "grave:grave", QuiltBlockEntityTypeBuilder.create(GraveBlockEntity::new, GRAVE).build(null));
	}

	public static void Place(World world, Vec3d pos, PlayerEntity player){
		if (world.isClient)
			return;

		BlockPos bp = new BlockPos(pos.x, pos.y - 1, pos.z);

		if (pos.y - 1 <= world.getDimension().minY()) {
			bp = new BlockPos(new BlockPos(pos.x, world.getDimension().minY(), pos.z));
		}

		DefaultedList<ItemStack> inv = DefaultedList.of();
		inv.addAll(player.getInventory().main);
		inv.addAll(player.getInventory().armor);
		inv.addAll(player.getInventory().offHand);

		boolean placed = false;

		for (BlockPos gP : BlockPos.iterateOutwards(bp.add(new Vec3i(0, 1, 0)), 5, 5, 5)){
			if (bp.getY() > world.getDimension().minY() || bp.getY() < world.getDimension().height() - world.getDimension().minY()){
				BlockState gS = GRAVE.getDefaultState().with(Properties.HORIZONTAL_FACING, player.getHorizontalFacing());

				gP = place_top(gP, world);
				placed = world.setBlockState(gP, gS);
				GraveBlockEntity graveBlockEntity = new GraveBlockEntity(gP, gS);
				graveBlockEntity.setInv(inv);
				graveBlockEntity.setOwner(player.getGameProfile());
				graveBlockEntity.setXp(player.totalExperience);
				graveBlockEntity.setLocation(gP);
				graveBlockEntity.setType(get_type(world.getBlockState(new BlockPos(pos)).getBlock()));
				world.addBlockEntity(graveBlockEntity);

				graveBlockEntity.markDirty();
				world.getBlockState(bp).getBlock().onBreak(world, bp, world.getBlockState(bp), player);

				player.totalExperience = 0;
				player.experienceLevel = 0;
				player.experienceProgress = 0;

				System.out.println(player.getName() + "'s grave spawn at: " + gP.getX() + ", " + gP.getY() + ", " + gP.getZ());
				if (CONFIG.Get_grave_coord())
					player.sendMessage(Text.of("Grave spawn at: " + gP.getX() + ", " + gP.getY() + ", " + gP.getZ()), false);

				break;
			}
		}

		if (!placed)
			player.getInventory().dropAll();
	}

	static int get_type(Block bl){
		if (!Objects.equals(bl.getName().getString(), "Air")){
			return 1;
		}
		return 0;
	}

	static BlockPos place_top(BlockPos pos, World world){
		BlockPos npos = pos;
		while(!Objects.equals(world.getBlockState(new BlockPos(npos)).getBlock().getName().getString(), "Air")){
			npos = new BlockPos(npos.getX(), npos.getY() + 1, npos.getZ());
			if (npos.getY() > world.getDimension().height())
				return pos;
		}
		return npos;
	}
}
