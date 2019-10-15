/*
 * Copyright (C) 2014 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.BlockBreakingProgressListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.BlockUtils;

@SearchTags({"auto tool", "AutoSwitch", "auto switch"})
public final class AutoToolHack extends Hack
	implements BlockBreakingProgressListener
{
	private final CheckboxSetting useSwords = new CheckboxSetting("Use swords",
		"Uses swords to break leaves,\n" + "cobwebs, etc.", false);
	
	private final CheckboxSetting useHands =
		new CheckboxSetting(
			"Use hands", "Uses an empty hand or a\n"
				+ "non-damageable item when\n" + "no applicable tool is found.",
			true);
	
	private final CheckboxSetting repairMode = new CheckboxSetting(
		"Repair mode", "Won't use tools that are about to break.", false);
	
	public AutoToolHack()
	{
		super("AutoTool", "Automatically equips the fastest applicable tool\n"
			+ "in your hotbar when you try to break a block.");
		
		setCategory(Category.BLOCKS);
		addSetting(useSwords);
		addSetting(useHands);
		addSetting(repairMode);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(BlockBreakingProgressListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(BlockBreakingProgressListener.class, this);
	}
	
	@Override
	public void onBlockBreakingProgress(BlockBreakingProgressEvent event)
	{
		BlockPos pos = event.getBlockPos();
		if(!BlockUtils.canBeClicked(pos))
			return;
		
		equipBestTool(pos, useSwords.isChecked(), useHands.isChecked(),
			repairMode.isChecked());
	}
	
	public void equipBestTool(BlockPos pos, boolean useSwords, boolean useHands,
		boolean repairMode)
	{
		ClientPlayerEntity player = MC.player;
		if(player.abilities.creativeMode)
			return;
		
		int bestSlot = getBestSlot(pos, useSwords, repairMode);
		if(bestSlot == -1)
		{
			ItemStack heldItem = player.getMainHandStack();
			if(!isDamageable(heldItem))
				return;
			
			if(repairMode && isTooDamaged(heldItem))
			{
				selectFallbackSlot();
				return;
			}
			
			if(useHands && isWrongTool(heldItem, pos))
			{
				selectFallbackSlot();
				return;
			}
			
			return;
		}
		
		player.inventory.selectedSlot = bestSlot;
	}
	
	private int getBestSlot(BlockPos pos, boolean useSwords, boolean repairMode)
	{
		ClientPlayerEntity player = MC.player;
		PlayerInventory inventory = player.inventory;
		ItemStack heldItem = MC.player.getMainHandStack();
		
		BlockState state = BlockUtils.getState(pos);
		float bestSpeed = getMiningSpeed(heldItem, state);
		int bestSlot = -1;
		
		for(int slot = 0; slot < 9; slot++)
		{
			if(slot == inventory.selectedSlot)
				continue;
			
			ItemStack stack = inventory.getInvStack(slot);
			
			float speed = getMiningSpeed(stack, state);
			if(speed <= bestSpeed)
				continue;
			
			if(!useSwords && stack.getItem() instanceof SwordItem)
				continue;
			
			if(repairMode && isTooDamaged(stack))
				continue;
			
			bestSpeed = speed;
			bestSlot = slot;
		}
		
		return bestSlot;
	}
	
	private float getMiningSpeed(ItemStack stack, BlockState state)
	{
		float speed = stack.getMiningSpeed(state);
		
		if(speed > 1)
		{
			int efficiency =
				EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
			if(efficiency > 0 && !stack.isEmpty())
				speed += efficiency * efficiency + 1;
		}
		
		return speed;
	}
	
	private boolean isDamageable(ItemStack stack)
	{
		return !stack.isEmpty() && stack.getItem().isDamageable();
	}
	
	private boolean isTooDamaged(ItemStack stack)
	{
		return stack.getMaxDamage() - stack.getDamage() <= 4;
	}
	
	private boolean isWrongTool(ItemStack heldItem, BlockPos pos)
	{
		BlockState state = BlockUtils.getState(pos);
		return getMiningSpeed(heldItem, state) <= 1;
	}
	
	private void selectFallbackSlot()
	{
		int fallbackSlot = getFallbackSlot();
		PlayerInventory inventory = MC.player.inventory;
		
		if(fallbackSlot == -1)
		{
			if(inventory.selectedSlot == 8)
				inventory.selectedSlot = 0;
			else
				inventory.selectedSlot++;
			
			return;
		}
		
		inventory.selectedSlot = fallbackSlot;
	}
	
	private int getFallbackSlot()
	{
		PlayerInventory inventory = MC.player.inventory;
		
		for(int slot = 0; slot < 9; slot++)
		{
			if(slot == inventory.selectedSlot)
				continue;
			
			ItemStack stack = inventory.getInvStack(slot);
			
			if(!isDamageable(stack))
				return slot;
		}
		
		return -1;
	}
}