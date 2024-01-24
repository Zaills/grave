package net.zaills.grave.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class Grave_notype extends GraveBlock{
	public Grave_notype(Settings settings) {
		super(settings);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx){
		VoxelShape vs = VoxelShapes.cuboid(.25f, 0f, .25f, .75f, .5f, .75f);
		VoxelShape nvs = switch (state.get(Properties.HORIZONTAL_FACING)) {
			case NORTH ->
					VoxelShapes.cuboid(((double) 1 / 16), 0f, ((double) 12/ 16), ((double) 15 / 16), ((double) 13 / 16), ((double) 15 / 16));
			case SOUTH ->
					VoxelShapes.cuboid(((double) 1 / 16), 0f, ((double) 1 / 16), ((double) 15 / 16), ((double) 13 / 16), ((double) 4 / 16));
			case EAST ->
					VoxelShapes.cuboid(((double) 1 / 16), 0f, ((double) 1 / 16), ((double) 4 / 16), ((double) 13 / 16), ((double) 15 / 16));
			case WEST ->
					VoxelShapes.cuboid(((double) 12 / 16), 0f, ((double) 1 / 16), ((double) 15 / 16), ((double) 13 / 16), ((double) 15 / 16));
			default -> null;
		};
		nvs = VoxelShapes.combineAndSimplify (nvs, switch (state.get(Properties.HORIZONTAL_FACING)){
			case NORTH ->
				VoxelShapes.cuboid(((double) 2 / 16), ((double) 13 / 16), ((double) 12/ 16), ((double) 14 / 16), ((double) 14 / 16), ((double) 15 / 16));
			case SOUTH ->
				VoxelShapes.cuboid(((double) 2 / 16), ((double) 13 / 16), ((double) 1 / 16), ((double) 14 / 16), ((double) 14 / 16), ((double) 4 / 16));
			case EAST ->
				VoxelShapes.cuboid(((double) 1 / 16), ((double) 13 / 16), ((double) 2 / 16), ((double) 4 / 16), ((double) 14 / 16), ((double) 14 / 16));
			case WEST ->
				VoxelShapes.cuboid(((double) 12 / 16), ((double) 13 / 16), ((double) 2 / 16), ((double) 15 / 16), ((double) 14 / 16), ((double) 14 / 16));
			default -> null;
		}, BooleanBiFunction.OR) ;
		return VoxelShapes.combineAndSimplify(vs, nvs, BooleanBiFunction.OR);
	}

}
