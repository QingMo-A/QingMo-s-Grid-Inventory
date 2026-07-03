package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.common.raid.config.RaidItemRequirementConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public final class RaidExtractionTriggerService {
    private RaidExtractionTriggerService() {}

    public static RaidExtractionTriggerResult triggerSwitch(
            MinecraftServer server, ServerPlayer player, RaidManifest manifest, String switchId) {
        String eligibility = validatePlayer(player, manifest);
        if (eligibility != null) return failure(eligibility);
        RaidExtractionSwitchActivation trigger = manifest.activeExtractionSwitches().stream()
                .filter(value -> value.id().equals(switchId)).findFirst().orElse(null);
        if (trigger == null) return failure("Active extraction switch not found.");
        Vec3 center = Vec3.atCenterOf(manifest.toWorldPos(trigger.localPos()));
        if (player.position().distanceTo(center) > trigger.radius()) return failure("Player is not near extraction switch.");
        for (RaidExtractionActivation extraction : manifest.activeExtractions()) {
            if (!extraction.trigger().isSwitch() || !switchId.equals(extraction.trigger().switchId())) continue;
            String canTrigger = RaidExtractionCountdownService.canTrigger(manifest, extraction.id());
            if (canTrigger != null) continue;
            String result = RaidExtractionCountdownService.trigger(server, manifest, extraction.id(), player);
            return new RaidExtractionTriggerResult(result.startsWith("Triggered"), result, extraction.id());
        }
        return failure("No available extraction uses this switch.");
    }

    public static RaidExtractionTriggerResult turnInItems(
            MinecraftServer server, ServerPlayer player, RaidManifest manifest,
            String extractionId, boolean simulate) {
        String eligibility = validatePlayer(player, manifest);
        if (eligibility != null) return failure(eligibility);
        RaidExtractionActivation extraction = manifest.activeExtractions().stream()
                .filter(value -> value.id().equals(extractionId)).findFirst().orElse(null);
        if (extraction == null) return failure("Extraction not found.");
        if (!extraction.trigger().isItemTurnIn()) return failure("Extraction is not an item turn-in.");
        if (!"global".equals(extraction.timer().type())) return failure("Extraction is not a global timer extraction.");
        Vec3 extractionCenter = Vec3.atCenterOf(manifest.toWorldPos(extraction.localPos()));
        if (player.position().distanceTo(extractionCenter) > extraction.radius()) {
            return failure("Player is not inside this extraction.");
        }
        String canTrigger = RaidExtractionCountdownService.canTrigger(manifest, extractionId);
        if (canTrigger != null) return failure(canTrigger);
        List<RaidItemRequirementConfig> requirements = extraction.trigger().requirements();
        if (requirements.isEmpty()) return failure("Extraction has no item requirements.");
        Map<ResourceLocation, Integer> required = aggregate(requirements);
        if (required.containsKey(null)) return failure("Extraction has an invalid item requirement.");
        for (Map.Entry<ResourceLocation, Integer> requirement : required.entrySet()) {
            if (count(player, requirement.getKey()) < requirement.getValue()) {
                return failure("Missing required item: " + requirement.getKey() + " x" + requirement.getValue());
            }
        }
        if (simulate) return new RaidExtractionTriggerResult(true,
                "Turn-in check passed: " + requirementSummary(required), extractionId);
        required.forEach((item, count) -> remove(player, item, count));
        String result = RaidExtractionCountdownService.trigger(server, manifest, extractionId, player);
        if (!result.startsWith("Triggered")) {
            required.forEach((item, count) -> player.getInventory().add(
                    new ItemStack(BuiltInRegistries.ITEM.get(item), count)));
            player.getInventory().setChanged();
            return failure(result);
        }
        player.getInventory().setChanged();
        return new RaidExtractionTriggerResult(true,
                "Turned in items and triggered extraction " + extractionId
                        + " countdown=" + extraction.timer().seconds() + "s", extractionId);
    }

    private static String validatePlayer(ServerPlayer player, RaidManifest manifest) {
        if (!manifest.isParticipant(player.getUUID())) return "Player is not a raid participant.";
        return player.serverLevel().dimension().location().toString().equals(manifest.dimensionId())
                ? null : "Player is not in raid dimension.";
    }
    private static int count(ServerPlayer player, ResourceLocation item) {
        return player.getInventory().items.stream()
                .filter(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(item))
                .mapToInt(ItemStack::getCount).sum();
    }
    private static void remove(ServerPlayer player, ResourceLocation item, int count) {
        int remaining = count;
        for (ItemStack stack : player.getInventory().items) {
            if (remaining <= 0) break;
            if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(item)) continue;
            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed); remaining -= removed;
        }
    }
    private static Map<ResourceLocation, Integer> aggregate(List<RaidItemRequirementConfig> requirements) {
        Map<ResourceLocation, Integer> result = new LinkedHashMap<>();
        requirements.forEach(value -> result.merge(value.item(), value.count(), Integer::sum));
        return result;
    }
    private static String requirementSummary(Map<ResourceLocation, Integer> requirements) {
        return String.join(", ", requirements.entrySet().stream()
                .map(value -> value.getKey() + " x" + value.getValue()).toList());
    }
    private static RaidExtractionTriggerResult failure(String message) {
        return new RaidExtractionTriggerResult(false, message, "");
    }
}
