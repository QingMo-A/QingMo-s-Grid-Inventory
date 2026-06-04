package com.dreamingfish.gridinventory.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record GridItemSizeRule(Type type, String target, GridItemSize size) {
    public static final Codec<GridItemSizeRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Type.CODEC.fieldOf("type").forGetter(GridItemSizeRule::type),
            Codec.STRING.fieldOf("target").forGetter(GridItemSizeRule::target),
            Codec.INT.fieldOf("width").forGetter(rule -> rule.size.width()),
            Codec.INT.fieldOf("height").forGetter(rule -> rule.size.height()),
            Codec.BOOL.optionalFieldOf("rotatable", false).forGetter(rule -> rule.size.rotatable())
    ).apply(instance, (type, target, width, height, rotatable) -> new GridItemSizeRule(type, target, new GridItemSize(width, height, rotatable))));

    public enum Type {
        ITEM("item"),
        TAG("tag"),
        MODID("modid");

        public static final Codec<Type> CODEC = Codec.STRING.xmap(Type::byName, Type::serializedName);
        private final String serializedName;

        Type(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }

        public static Type byName(String name) {
            for (Type type : values()) {
                if (type.serializedName.equals(name)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown grid item size rule type: " + name);
        }
    }

    public ResourceLocation targetLocation() {
        return ResourceLocation.parse(target);
    }

    public boolean matchesModId(ItemStack stack) {
        ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key != null && key.getNamespace().equals(target);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeUtf(target);
        size.encode(buf);
    }

    public static GridItemSizeRule decode(FriendlyByteBuf buf) {
        return new GridItemSizeRule(buf.readEnum(Type.class), buf.readUtf(), GridItemSize.decode(buf));
    }
}
