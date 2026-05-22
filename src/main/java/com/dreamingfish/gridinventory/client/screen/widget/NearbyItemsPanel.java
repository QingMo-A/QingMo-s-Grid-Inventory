package com.dreamingfish.gridinventory.client.screen.widget;

import com.dreamingfish.gridinventory.client.config.GridInventoryClientConfig;
import com.dreamingfish.gridinventory.common.config.GridInventoryConfig;
import com.dreamingfish.gridinventory.common.network.ManualPickupItemPacket;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class NearbyItemsPanel {
    private static final int CELL = 18;
    private final List<Entry> entries = new ArrayList<>();
    private int left;
    private int top;
    private int columns;
    private int visibleRows;
    private int scrollRows;
    private int totalRows;

    public void setBounds(int left, int top) {
        this.left = left;
        this.top = top;
        this.columns = GridInventoryClientConfig.NEARBY_PANEL_COLUMNS.get();
        this.visibleRows = GridInventoryClientConfig.NEARBY_PANEL_VISIBLE_ROWS.get();
    }

    public int width() {
        return columns * CELL + 12;
    }

    public int height() {
        return visibleRows * CELL + 26;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!GridInventoryClientConfig.SHOW_NEARBY_ITEMS_PANEL.get()) {
            return;
        }
        refresh();
        Font font = Minecraft.getInstance().font;
        graphics.fill(left, top, left + width(), top + height(), 0xDD171717);
        graphics.fill(left, top, left + width(), top + 14, 0xFF2D2D2D);
        graphics.drawString(font, Component.translatable("screen.df_grid_inventory.nearby_items"), left + 5, top + 4, 0xFFFFFF, false);

        int gridLeft = left + 6;
        int gridTop = top + 20;
        graphics.enableScissor(gridLeft, gridTop, gridLeft + columns * CELL, gridTop + visibleRows * CELL);
        for (Entry entry : entries) {
            int drawY = gridTop + (entry.y - scrollRows) * CELL;
            if (entry.y + entry.view.gridHeight() <= scrollRows || entry.y >= scrollRows + visibleRows) {
                continue;
            }
            int drawX = gridLeft + entry.x * CELL;
            int color = isInside(mouseX, mouseY, drawX, drawY, entry.view.gridWidth() * CELL, entry.view.gridHeight() * CELL) ? 0x994D6EA8 : 0x77333333;
            graphics.fill(drawX, drawY, drawX + entry.view.gridWidth() * CELL, drawY + entry.view.gridHeight() * CELL, color);
            graphics.renderItem(entry.view.stack(), drawX + 2, drawY + 2);
            graphics.renderItemDecorations(font, entry.view.stack(), drawX + 2, drawY + 2);
        }
        graphics.disableScissor();

        hovered(mouseX, mouseY).ifPresent(entry -> graphics.renderTooltip(font, List.of(
                entry.view.stack().getHoverName(),
                Component.literal("Count: " + entry.view.stack().getCount()).withStyle(ChatFormatting.GRAY),
                Component.literal(String.format("Distance: %.1f", entry.view.distance())).withStyle(ChatFormatting.GRAY),
                Component.literal("Size: " + entry.view.gridWidth() + " x " + entry.view.gridHeight()).withStyle(ChatFormatting.GRAY),
                Component.translatable("tooltip.df_grid_inventory.pickup_hint").withStyle(ChatFormatting.YELLOW)
        ), Optional.empty(), mouseX, mouseY));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !GridInventoryClientConfig.SHOW_NEARBY_ITEMS_PANEL.get()) {
            return false;
        }
        Optional<Entry> hit = hovered((int) mouseX, (int) mouseY);
        hit.ifPresent(entry -> PacketDistributor.sendToServer(new ManualPickupItemPacket(entry.view.entityId())));
        return hit.isPresent() || isInside((int) mouseX, (int) mouseY, left, top, width(), height());
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        if (!isInside((int) mouseX, (int) mouseY, left, top, width(), height())) {
            return false;
        }
        int max = Math.max(0, totalRows - visibleRows);
        scrollRows = Math.max(0, Math.min(max, scrollRows - (int) Math.signum(deltaY)));
        return true;
    }

    private Optional<Entry> hovered(int mouseX, int mouseY) {
        int gridLeft = left + 6;
        int gridTop = top + 20;
        for (Entry entry : entries) {
            int drawX = gridLeft + entry.x * CELL;
            int drawY = gridTop + (entry.y - scrollRows) * CELL;
            if (isInside(mouseX, mouseY, drawX, drawY, entry.view.gridWidth() * CELL, entry.view.gridHeight() * CELL)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    private void refresh() {
        Minecraft minecraft = Minecraft.getInstance();
        entries.clear();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        double range = GridInventoryConfig.NEARBY_ITEMS_RANGE.get();
        AABB area = minecraft.player.getBoundingBox().inflate(range);
        List<NearbyGroundItemView> views = minecraft.level.getEntitiesOfClass(ItemEntity.class, area, item -> !item.isRemoved() && item.isAlive() && !item.getItem().isEmpty())
                .stream()
                .map(item -> {
                    ItemStack stack = item.getItem();
                    var size = GridItemSizeManager.getSize(stack);
                    return new NearbyGroundItemView(item.getId(), stack.copy(), item.distanceTo(minecraft.player), size.width(), size.height(), size.rotatable(), item.position());
                })
                .sorted(Comparator.comparingDouble(NearbyGroundItemView::distance))
                .toList();
        pack(views);
    }

    private void pack(List<NearbyGroundItemView> views) {
        List<boolean[]> occupied = new ArrayList<>();
        totalRows = visibleRows;
        for (NearbyGroundItemView view : views) {
            boolean placed = false;
            for (int y = 0; !placed && y < 128; y++) {
                ensureRows(occupied, y + view.gridHeight());
                for (int x = 0; x <= columns - view.gridWidth(); x++) {
                    if (fits(occupied, x, y, view.gridWidth(), view.gridHeight())) {
                        occupy(occupied, x, y, view.gridWidth(), view.gridHeight());
                        entries.add(new Entry(view, x, y));
                        totalRows = Math.max(totalRows, y + view.gridHeight());
                        placed = true;
                        break;
                    }
                }
            }
        }
    }

    private void ensureRows(List<boolean[]> occupied, int rows) {
        while (occupied.size() < rows) {
            occupied.add(new boolean[columns]);
        }
    }

    private boolean fits(List<boolean[]> occupied, int x, int y, int w, int h) {
        for (int yy = y; yy < y + h; yy++) {
            for (int xx = x; xx < x + w; xx++) {
                if (occupied.get(yy)[xx]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void occupy(List<boolean[]> occupied, int x, int y, int w, int h) {
        for (int yy = y; yy < y + h; yy++) {
            for (int xx = x; xx < x + w; xx++) {
                occupied.get(yy)[xx] = true;
            }
        }
    }

    private static boolean isInside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private record Entry(NearbyGroundItemView view, int x, int y) {
    }
}
