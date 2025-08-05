package net.zaills.gravefabric.block.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.zaills.gravefabric.GraveFabric;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GraveBlockEntity extends BlockEntity {
	private DefaultedList<ItemStack> inv;
	private int xp;
	private GameProfile Owner;

	public GraveBlockEntity(BlockPos pos, BlockState state) {
		super(GraveFabric.GRAVE_ENTITY, pos, state);

		this.inv = DefaultedList.ofSize(41, ItemStack.EMPTY);
		this.xp = 0;
		this.Owner = null;
	}

	public void setInv(DefaultedList<ItemStack> inv) {
		this.inv = inv;
		this.markDirty();
	}

	public void setXp(int xp) {
		this.xp = xp;
		this.markDirty();
	}

	public void setOwner(GameProfile profile) {
		this.Owner = profile;
		this.markDirty();
	}


	public DefaultedList<ItemStack> getInv() {
		return this.inv;
	}

	public int getXp() {
		return this.xp;
	}

	public GameProfile getOwner() {
		return this.Owner;
	}


	@Override
	protected void readData(ReadView view) {
		super.readData(view);

		this.inv = DefaultedList.ofSize(view.getInt("ItemCount", 0), ItemStack.EMPTY);
		if (!this.inv.isEmpty()) {
			Inventories.readData(view, this.inv);
		}
		this.xp = view.getInt("XP", 0);

		String OwnerID = view.getString("OwnerId", null);
		String OwnerName = view.getString("OwnerName", null);


		if (OwnerID != null && OwnerName != null) {
			this.Owner = new GameProfile(UUID.fromString(OwnerID), OwnerName);
		}
	}

	@Override
	protected void readComponents(ComponentsAccess components) {
		super.readComponents(components);
	}


	@Override
	public void writeData(WriteView view) {
		super.writeData(view);

		view.putInt("ItemCount", this.inv.size());
		Inventories.writeData(view, this.inv);
		view.putInt("XP", xp);

		if (Owner != null) {
			view.putString("OwnerId", Owner.getId().toString());
			view.putString("OwnerName", Owner.getName());
		}
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return createNbt(registryLookup);
	}
}