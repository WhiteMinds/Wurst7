/*
 * Copyright (c) 2014-2020 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.AutoCraftPackedItemHack;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin
	extends HandledScreen<CraftingScreenHandler>
	implements RecipeBookProvider
{
	private final AutoCraftPackedItemHack autoCraftPackedItemHack =
		WurstClient.INSTANCE.getHax().autoCraftPackedItemHack;
	
	public CraftingScreenMixin(WurstClient wurst, CraftingScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
	}
	
	@Override
	public void init()
	{
		super.init();
		
		if(!WurstClient.INSTANCE.isEnabled())
			return;

		if(autoCraftPackedItemHack.isEnabled()) {
			addButton(new ButtonWidget(x + backgroundWidth - 108, y + 4, 50, 12,
			new LiteralText("PackItem"), b -> startAutoCraftPacked()));
		}
	}
	
	private void startAutoCraftPacked()
	{
		runInThread(() -> autoCraftPacked());
	}
	
	private void runInThread(Runnable r)
	{
		new Thread(() -> {
			try
			{
				r.run();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}).start();
	}
	
	private void autoCraftPacked()
	{
		int endIdx = handler.getCraftingSlotCount() - 1;
		int lackCount = autoCraftPackedItemHack.packCount.getValueI();

		// 移除不需要或多出的物品，统计还差几个目标物品
		for (int i = 0; i <= endIdx; i++) {
			if (i == handler.getCraftingResultSlotIndex())
				continue;

			Slot slot = handler.getSlot(i);
			ItemStack stack = slot.getStack();
			if (stack.isEmpty())
				continue;

			if (stack.getItem().equals(autoCraftPackedItemHack.heldItem.getItem()) && lackCount > 0) {
				lackCount--;
			} else {
				// 移除
				// IMC.getInteractionManager().windowClick_QUICK_MOVE(slot.id);
				onMouseClick(slot, slot.id, 0, SlotActionType.QUICK_MOVE);
				// TODO: 如果 lackCount > 0，将换入目标物品
				// IMC.getInteractionManager().windowClick_SWAP(slot.id);
				waitForDelay(100);
			}
		}

		for (int i = endIdx + 1; i < handler.slots.size(); i++) {
			if (lackCount <= 0)
				break;

			Slot slot = handler.getSlot(i);
			ItemStack stack = slot.getStack();
			if (stack.isEmpty())
				continue;

			if (!stack.getItem().equals(autoCraftPackedItemHack.heldItem.getItem()))
				continue;

			// IMC.getInteractionManager().windowClick_QUICK_MOVE(slot.id);
			System.out.println("quick move");
			System.out.println(slot);
			System.out.println("quick move id");
			System.out.println(slot.id);
			onMouseClick(slot, slot.id, 0, SlotActionType.QUICK_MOVE);

			// List<Slot> emptySlots = handler.slots.stream().filter(s -> s.getStack().isEmpty()).collect(Collectors.toList());
			// if (emptySlots.size() <= 0)
			// 	break;

			// IMC.getInteractionManager().windowClick_PICKUP(slot.id);
			// IMC.getInteractionManager().windowClick_PICKUP(emptySlots.get(0).id);

			lackCount--;
			waitForDelay(100);

			if (client.currentScreen == null) break;
		}
	}
	
	private void waitForDelay(int time)
	{
		try
		{
			Thread.sleep(time);
			
		}catch(InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
}
