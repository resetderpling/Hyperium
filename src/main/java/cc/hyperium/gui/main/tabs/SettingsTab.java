package cc.hyperium.gui.main.tabs;

import cc.hyperium.Hyperium;
import cc.hyperium.config.Category;
import cc.hyperium.config.SelectorSetting;
import cc.hyperium.config.Settings;
import cc.hyperium.config.ToggleSetting;
import cc.hyperium.cosmetics.Deadmau5Cosmetic;
import cc.hyperium.cosmetics.HyperiumCosmetics;
import cc.hyperium.cosmetics.wings.WingsCosmetic;
import cc.hyperium.gui.GuiBlock;
import cc.hyperium.gui.Icons;
import cc.hyperium.gui.main.HyperiumMainGui;
import cc.hyperium.gui.main.HyperiumOverlay;
import cc.hyperium.gui.main.components.AbstractTab;
import cc.hyperium.gui.main.components.OverlaySelector;
import cc.hyperium.gui.main.components.SettingItem;
import cc.hyperium.netty.NettyClient;
import cc.hyperium.netty.packet.packets.serverbound.ServerCrossDataPacket;
import cc.hyperium.purchases.PurchaseApi;
import cc.hyperium.utils.JsonHolder;
import net.minecraft.client.gui.Gui;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/*
 * Created by Cubxity on 20/05/2018
 */
public class SettingsTab extends AbstractTab {
    private final HyperiumOverlay general = new HyperiumOverlay();
    private final HyperiumOverlay integrations = new HyperiumOverlay();
    private final HyperiumOverlay improvements = new HyperiumOverlay();
    private final HyperiumOverlay cosmetics = new HyperiumOverlay();
    private final HyperiumOverlay spotify = new HyperiumOverlay();
    private final HyperiumOverlay wings = new HyperiumOverlay();
    private final HashMap<Field, Consumer<Object>> callback = new HashMap<>();
    private final HashMap<Field, Supplier<String[]>> customStates = new HashMap<>();
    private int offsetY = 0;
    private GuiBlock block;
    private int y, w;

    {
        try {
            Field earsField = Settings.class.getField("EARS_STATE");
            callback.put(earsField, o -> {
                boolean yes = ((String) o).equalsIgnoreCase("YES");
                NettyClient.getClient().write(ServerCrossDataPacket.build(new JsonHolder().put("internal", true).put("ears", yes)));
            });
            customStates.put(earsField, () -> {
                Hyperium instance = Hyperium.INSTANCE;
                if (instance != null) {
                    HyperiumCosmetics cosmetics1 = instance.getCosmetics();
                    if (cosmetics1 != null) {
                        Deadmau5Cosmetic deadmau5Cosmetic = cosmetics1.getDeadmau5Cosmetic();
                        if (deadmau5Cosmetic != null) {
                            if (deadmau5Cosmetic.isSelfUnlocked()) {
                                return new String[]{"YES", "NO"};
                            }
                        }
                    }
                }
                return new String[]{"NOT PURCHASED"};
            });
            callback.put(Settings.class.getField("wingsSELECTED"), o -> {
                JsonHolder purchaseSettings = PurchaseApi.getInstance().getSelf().getPurchaseSettings();
                if (!purchaseSettings.has("wings"))
                    purchaseSettings.put("wings", new JsonHolder());
                purchaseSettings.optJSONObject("wings").put("type", o.toString());
                NettyClient.getClient().write(ServerCrossDataPacket.build(new JsonHolder().put("internal", true).put("wings", o.toString())));
            });


        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        for (Field f : Settings.class.getFields()) {
            ToggleSetting ts = f.getAnnotation(ToggleSetting.class);
            SelectorSetting ss = f.getAnnotation(SelectorSetting.class);
            Consumer<Object> objectConsumer = callback.get(f);
            if (ts != null) {
                getCategory(ts.category()).addToggle(ts.name(), f, objectConsumer);
            } else if (ss != null)
                try {
                    Supplier<String[]> supplier = customStates.get(f);
                    Supplier<String[]> supplier1 = supplier != null ? supplier : ss::items;
                    String current = String.valueOf(f.get(null));
                    if (!ArrayUtils.contains(supplier1.get(), current))
                        current = supplier1.get()[0];
                    getCategory(ss.category()).getComponents().add(new OverlaySelector<>(ss.name(), current, si -> {
                        if (objectConsumer != null)
                            objectConsumer.accept(si);
                        try {
                            f.set(null, si);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }, supplier1));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
        }
        Settings.save();
    }

    public SettingsTab(int y, int w) {
        block = new GuiBlock(0, w, y, y + w);
        this.y = y;
        this.w = w;

        items.add(new SettingItem(() -> HyperiumMainGui.INSTANCE.setOverlay(general), Icons.SETTINGS.getResource(), "General", "General settings for Hyperium", "Click to configure", 0, 0));

        items.add(new SettingItem(() -> HyperiumMainGui.INSTANCE.setOverlay(integrations), Icons.EXTENSION.getResource(), "Integrations", "Hyperium integrations", "Click to configure", 1, 0));

        items.add(new SettingItem(() -> HyperiumMainGui.INSTANCE.setOverlay(improvements), Icons.TOOL.getResource(), "Improvements", "Improvements and bug fixes", "Click to configure", 2, 0));

        items.add(new SettingItem(() -> HyperiumMainGui.INSTANCE.setOverlay(cosmetics), Icons.COSMETIC.getResource(), "Cosmetics", "Bling out your Minecraft Avatar", "Click to configure", 0, 1));

        items.add(new SettingItem(() -> HyperiumMainGui.INSTANCE.setOverlay(spotify), Icons.SPOTIFY.getResource(), "Spotify", "Hyperium Spotify Settings", "Click to configure", 1, 1));

        //TODO fix this method being async
        WingsCosmetic wingsCosmetic = Hyperium.INSTANCE.getCosmetics().getWingsCosmetic();
        if (wingsCosmetic.isSelfUnlocked()) {
            items.add(new SettingItem(() -> HyperiumMainGui.INSTANCE.setOverlay(wings), Icons.COSMETIC.getResource(), "wings", "Hyperium wings Settings", "Click to configure", 2, 1));
        }
    }

    private HyperiumOverlay getCategory(Category category) {
        switch (category) {
            case GENERAL:
                return general;
            case IMPROVEMENTS:
                return improvements;
            case INTEGRATIONS:
                return integrations;
            case COSMETICS:
                return cosmetics;
            case SPOTIFY:
                return spotify;
            case WINGS:
                return wings;
        }
        return general;
    }

    @Override
    public void drawTabIcon() {
        Icons.SETTINGS.bind();
        Gui.drawScaledCustomSizeModalRect(5, y + 5, 0, 0, 144, 144, w - 10, w - 10, 144, 144);
    }

    @Override
    public GuiBlock getBlock() {
        return block;
    }

    @Override
    public void drawHighlight(float s) {
        Gui.drawRect(0, (int) (y + s * (s * w / 2)), 3, (int) (y + w - s * (w / 2)), Color.WHITE.getRGB());
    }

    @Override
    public void draw(int mouseX, int mouseY, int topX, int topY, int containerWidth, int containerHeight) {
        super.draw(mouseX, mouseY, topX, topY + offsetY, containerWidth, containerHeight);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        if (HyperiumMainGui.INSTANCE.getOverlay() != null) return;
        int i = Mouse.getEventDWheel();

        if (i < 0)
            offsetY -= 5;
        else if (i > 0)
            offsetY += 5;
    }
}
