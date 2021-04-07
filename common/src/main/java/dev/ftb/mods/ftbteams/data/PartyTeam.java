package dev.ftb.mods.ftbteams.data;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyTeam extends Team {
	UUID owner;
	public final List<TeamMessage> messageHistory;

	public PartyTeam(TeamManager m) {
		super(m);
		owner = Util.NIL_UUID;
		messageHistory = new ArrayList<>();
	}

	@Override
	public TeamType getType() {
		return TeamType.PARTY;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = super.serializeNBT();
		tag.putString("owner", UUIDTypeAdapter.fromUUID(owner));
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag tag) {
		super.deserializeNBT(tag);
		owner = UUIDTypeAdapter.fromString(tag.getString("owner"));
	}

	@Override
	public void sendMessage(GameProfile from, Component text) {
		messageHistory.add(new TeamMessage(from, System.currentTimeMillis(), text));

		if (messageHistory.size() > 1000) {
			messageHistory.remove(0);
		}

		// Send packet here //

		TextComponent component = new TextComponent("<");
		component.append(from.equals(FTBTUtils.NO_PROFILE) ? new TextComponent("System").withStyle(ChatFormatting.LIGHT_PURPLE) : new TextComponent(from.getName()).withStyle(ChatFormatting.YELLOW));
		component.append(" @");
		component.append(getName());
		component.append("> ");
		component.append(text);

		for (ServerPlayer p : getOnlineMembers()) {
			p.displayClientMessage(component, false);
		}

		save();
	}

	@Override
	public TeamRank getHighestRank(UUID playerId) {
		if (owner.equals(playerId)) {
			return TeamRank.OWNER;
		}

		return super.getHighestRank(playerId);
	}

	public boolean isOwner(UUID profile) {
		return owner.equals(profile);
	}

	public UUID getOwner() {
		return owner;
	}

	@Nullable
	public ServerPlayer getOwnerPlayer() {
		return FTBTUtils.getPlayerByUUID(manager.server, owner);
	}
}
