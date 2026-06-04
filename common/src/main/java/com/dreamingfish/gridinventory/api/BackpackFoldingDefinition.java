package com.dreamingfish.gridinventory.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record BackpackFoldingDefinition(GridItemSizeRule.Type type, String target, int foldedWidth, int foldedHeight, String foldedModel) {
    public static final Codec<BackpackFoldingDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GridItemSizeRule.Type.CODEC.fieldOf("type").forGetter(BackpackFoldingDefinition::type),
            Codec.STRING.fieldOf("target").forGetter(BackpackFoldingDefinition::target),
            Codec.INT.fieldOf("folded_width").forGetter(BackpackFoldingDefinition::foldedWidth),
            Codec.INT.fieldOf("folded_height").forGetter(BackpackFoldingDefinition::foldedHeight),
            Codec.STRING.optionalFieldOf("folded_model", "square").forGetter(BackpackFoldingDefinition::foldedModel)
    ).apply(instance, BackpackFoldingDefinition::new));

    public BackpackFoldingDefinition {
        if (foldedWidth < 1 || foldedHeight < 1) {
            throw new IllegalArgumentException("Folded backpack size must be at least 1x1");
        }
    }

    public ResourceLocation targetLocation() {
        return ResourceLocation.parse(target);
    }

    public boolean usesRollModel() {
        return "roll".equals(foldedModel);
    }

    public boolean matchesModId(ItemStack stack) {
        ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key != null && key.getNamespace().equals(target);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeUtf(target);
        buf.writeVarInt(foldedWidth);
        buf.writeVarInt(foldedHeight);
        buf.writeUtf(foldedModel);
    }

    public static BackpackFoldingDefinition decode(FriendlyByteBuf buf) {
        return new BackpackFoldingDefinition(buf.readEnum(GridItemSizeRule.Type.class), buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readUtf());
    }
}
