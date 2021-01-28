/*
 * Copyright (c) 2014-2020 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
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
	
	public CraftingScreenMixin(WurstClient wurst,
			CraftingScreenHandler container, PlayerInventory playerInventory,
		Text name)
	{
		super(container, playerInventory, name);
	}

	@Inject(at = {@At("TAIL")}, method = {"init()V"})
	protected void onInit(CallbackInfo ci)
	{	
		if(!WurstClient.INSTANCE.isEnabled())
			return;

		if (autoCraftPackedItemHack.heldItem != null) {
			addButton(new ButtonWidget(x + backgroundWidth - 56, y + 4, 50, 12,
					new LiteralText("Pack " + autoCraftPackedItemHack.heldItem.getName().getString()), b -> startAutoCraftPacked()));
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
		int resultSlotIdx = handler.getCraftingResultSlotIndex();

		// 移除不需要或多出的物品，统计还差几个目标物品
		for (int i = 0; i <= endIdx; i++) {
			if (i == resultSlotIdx)
				continue;

			Slot slot = handler.getSlot(i);
			ItemStack stack = slot.getStack();
			if (stack.isEmpty())
				continue;

			if (ItemStack.areEqual(stack, autoCraftPackedItemHack.heldItem) && lackCount > 0) {
				lackCount--;
			} else {
				// 移除
				// TODO: 如果 lackCount > 0，将换入目标物品，使用 SWAP 之类的
				onMouseClick(slot, slot.id, 0, SlotActionType.QUICK_MOVE);
				waitForDelay(getDelay());
			}
		}

		for (int i = endIdx + 1; i < handler.slots.size(); i++) {
			if (lackCount <= 0)
				break;

			Slot slot = handler.getSlot(i);
			ItemStack stack = slot.getStack();
			if (stack.isEmpty())
				continue;

			if (!ItemStack.areEqual(stack, autoCraftPackedItemHack.heldItem))
				continue;

			onMouseClick(slot, slot.id, 0, SlotActionType.QUICK_MOVE);
			lackCount--;
			waitForDelay(getDelay());

			if (client.currentScreen == null) break;
		}
		
		Slot resultSlot = handler.getSlot(resultSlotIdx);
		if (lackCount <= 0 && !resultSlot.getStack().isEmpty()) {
			waitForDelay(500);
			onMouseClick(resultSlot, resultSlot.id, 0, SlotActionType.QUICK_MOVE);
			
			waitForDelay(500);
			autoCraftPacked();
		}
	}
	
	private int getDelay() {
		return 50;
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
