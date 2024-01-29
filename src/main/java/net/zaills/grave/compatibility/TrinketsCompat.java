package net.zaills.grave.compatibility;

import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrinketsCompat {
	public List<ItemStack> getInv(PlayerEntity entity){
		List<ItemStack> iS = new ArrayList<>();
		Map<String, Map<String, TrinketInventory>> Tinv = TrinketsApi.getTrinketComponent(entity).get().getInventory();
		for (Map<String, TrinketInventory> map : Tinv.values()){
			for (TrinketInventory inv : map.values()){
				for (int i = 0; i < inv.size(); i++){
					iS.add(inv.getStack(i));
				}
			}
		}
		return iS;
	}

	public void setInv(List<ItemStack> stk, PlayerEntity entity){
		for (ItemStack iS : stk){
			Map<String, Map<String, TrinketInventory>> invent = TrinketsApi.getTrinketComponent(entity).get().getInventory();
			for (Map<String, TrinketInventory> map : invent.values()){
				for (TrinketInventory inv : map.values()){
					for (int i = 0; i < inv.size(); i++){
						if (inv.getStack(i).isEmpty()){
							inv.setStack(i, iS);
							continue;
						}
						entity.getInventory().insertStack(iS);
					}
				}
			}
		}
	}

	public int getInvSize(PlayerEntity entity){
		return TrinketsApi.getTrinketComponent(entity).get().getInventory().size();
	}

	public static void clearInv(PlayerEntity entity){
		TrinketsApi.getTrinketComponent(entity).get().getInventory().clear();
	}
}
