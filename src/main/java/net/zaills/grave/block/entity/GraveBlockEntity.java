package net.zaills.grave.block.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.zaills.grave.Grave;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.enchantment.EnchantmentHelper.createNbt;

public class GraveBlockEntity extends BlockEntity {
	private DefaultedList<ItemStack> inv;
	private	int xp;
	private GameProfile Owner;

	public GraveBlockEntity(BlockPos pos, BlockState state) {
		super(Grave.GRAVE_ENTITY, pos, state);

		this.inv = DefaultedList.ofSize(41, ItemStack.EMPTY);
		this.xp = 0;
		this.Owner = null;
	}

	public void setInv(DefaultedList<ItemStack> inv){
		this.inv = inv;
		this.markDirty();
	}

	public void setXp(int xp){
		this.xp = xp;
		this.markDirty();
	}

	public void setOwner(GameProfile gameProfile){
		this.Owner = gameProfile;
		this.markDirty();
	}

	public DefaultedList<ItemStack> getInv(){
		return inv;
	}

	public int getXp(){
		return xp;
	}

	public GameProfile getOwner(){
		return Owner;
	}

	@Override
	public void	readNbt(NbtCompound tag){
		super.readNbt(tag);

		this.inv = DefaultedList.ofSize(tag.getInt("ItemCount"), ItemStack.EMPTY);
		Inventories.readNbt(tag.getCompound("Items"), this.inv);
		this.xp = tag.getInt("XP");

		if (tag.contains("Owner")) {
			this.Owner = NbtHelper.toGameProfile(tag.getCompound("Owner"));
		}
	}

	@Override
	public void	writeNbt(NbtCompound tag){
		super.writeNbt(tag);

		tag.putInt("ItemCount", this.inv.size());
		tag.put("Items", Inventories.writeNbt(new NbtCompound(), this.inv, true));
		tag.putInt("XP", xp);

		if (Owner != null){
			tag.put("Owner", NbtHelper.writeGameProfile(new NbtCompound(), Owner));
		}
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.of(this);
	}

}
