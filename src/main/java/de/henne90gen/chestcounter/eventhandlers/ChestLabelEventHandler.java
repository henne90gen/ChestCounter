package de.henne90gen.chestcounter.eventhandlers;

import java.util.ArrayList;
import java.util.List;

import de.henne90gen.chestcounter.ChestCounter;
import de.henne90gen.chestcounter.ChestDB;
import de.henne90gen.chestcounter.dtos.Chest;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChestLabelEventHandler {

	private final ChestCounter mod;
	private final ChestDB chestDB;

	private List<BlockPos> chestPositions;

	public ChestLabelEventHandler(ChestCounter mod) {
		this.mod = mod;
		this.chestPositions = new ArrayList<>();
		this.chestDB = new ChestDB(mod);
	}

	@SubscribeEvent
	public void interact(PlayerInteractEvent event) {
		if (mod.label == null) {
			return;
		}
		chestPositions = mod.getChestPositions(event);

		Chest chest = new Chest();
		chest.id = chestDB.createChestID(chestPositions);
		chest.chestContent.label = mod.label;
		mod.label = null;
		chestDB.updateLabel(chest);
	}
}
