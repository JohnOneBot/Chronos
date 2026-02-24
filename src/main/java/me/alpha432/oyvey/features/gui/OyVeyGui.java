package me.alpha432.oyvey.features.gui;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.gui.items.Item;
import me.alpha432.oyvey.features.gui.items.buttons.ModuleButton;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class OyVeyGui extends Screen {
    private static OyVeyGui INSTANCE;
    private static Color colorClipboard = null;

    static {
        INSTANCE = new OyVeyGui();
    }

    private final ArrayList<Widget> widgets = new ArrayList<>();
    private long openedAtMs = System.currentTimeMillis();
    private boolean closing = false;
    private long closingAtMs = -1L;

    public OyVeyGui() {
        super(Component.literal("Chronos"));
        setInstance();
        load();
    }

    public static OyVeyGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OyVeyGui();
        }
        return INSTANCE;
    }

    public static OyVeyGui getClickGui() {
        return OyVeyGui.getInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    private void load() {
        int x = -84;
        for (Module.Category category : OyVey.moduleManager.getCategories()) {
            Widget panel = new Widget(category.getName(), x += 90, 4, true);
            OyVey.moduleManager.stream()
                    .filter(m -> m.getCategory() == category && !m.hidden)
                    .map(ModuleButton::new)
                    .forEach(panel::addButton);
            this.widgets.add(panel);
        }
        this.widgets.forEach(components -> components.getItems().sort(Comparator.comparing(Feature::getName)));
    }


    @Override
    protected void init() {
        super.init();
        openedAtMs = System.currentTimeMillis();
        closing = false;
        closingAtMs = -1L;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Item.context = context;
        int w = context.guiWidth();
        int h = context.guiHeight();

        float fadeIn = Math.min(1.0f, (System.currentTimeMillis() - openedAtMs) / 520.0f);
        float easedFade = 1.0f - (float) Math.pow(1.0f - fadeIn, 3.0f);
        if (closing) {
            float fadeOut = Math.min(1.0f, (System.currentTimeMillis() - closingAtMs) / 360.0f);
            float easedOut = 1.0f - (float) Math.pow(1.0f - fadeOut, 2.0f);
            easedFade = Math.max(0.0f, 1.0f - easedOut);
            if (easedFade <= 0.01f) {
                mc.setScreen(null);
                return;
            }
        }

        int top = new Color(35, 18, 64, (int) (150 * easedFade)).getRGB();
        int bottom = new Color(18, 8, 38, (int) (185 * easedFade)).getRGB();
        context.fillGradient(0, 0, w, h, top, bottom);

        float t = (System.currentTimeMillis() % 120000L) / 1000.0f;
        for (int i = 0; i < 110; i++) {
            float driftX = (float) Math.sin((t * 0.18f) + (i * 0.31f)) * 18.0f;
            float driftY = (float) Math.cos((t * 0.14f) + (i * 0.43f)) * 12.0f;
            int seedX = (int) (((i * 73) + (t * (8 + (i % 5)))) % Math.max(1, w));
            int seedY = (int) (((i * 47) + (t * (5 + (i % 3)))) % Math.max(1, h));
            int x = Math.floorMod((int) (seedX + driftX), Math.max(1, w));
            int y = Math.floorMod((int) (seedY + driftY), Math.max(1, h));
            int twinkle = (int) ((Math.sin((t * 2.2f) + i) + 1.0f) * 55.0f);
            int alpha = (int) (Math.min(220, 35 + twinkle) * easedFade);
            int size = (i % 7 == 0) ? 2 : 1;
            int color = new Color(255, 210, 255, alpha).getRGB();
            context.fill(x, y, x + size, y + size, color);
        }

        this.widgets.forEach(components -> components.drawScreen(context, mouseX, mouseY, delta));

        if (easedFade < 1.0f) {
            int overlay = new Color(6, 4, 12, (int) ((1.0f - easedFade) * 95)).getRGB();
            context.fill(0, 0, w, h, overlay);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (!closing) this.widgets.forEach(components -> components.mouseClicked((int) click.x(), (int) click.y(), click.button()));
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (!closing) this.widgets.forEach(components -> components.mouseReleased((int) click.x(), (int) click.y(), click.button()));
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0) {
            this.widgets.forEach(component -> component.setY(component.getY() - 10));
        } else if (verticalAmount > 0) {
            this.widgets.forEach(component -> component.setY(component.getY() + 10));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.input() == GLFW.GLFW_KEY_ESCAPE) {
            if (!closing) {
                closing = true;
                closingAtMs = System.currentTimeMillis();
            }
            return true;
        }

        if (!closing) {
            this.widgets.forEach(component -> component.onKeyPressed(input.input()));
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        this.widgets.forEach(component -> component.onKeyTyped(input.codepointAsString(), input.modifiers()));
        return super.charTyped(input);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }//ignore 1.21.8 blur thing

    public final ArrayList<Widget> getComponents() {
        return this.widgets;
    }

    public int getTextOffset() {
        return -6;
    }

    public static Color getColorClipboard() {
        return colorClipboard;
    }

    public static void setColorClipboard(Color color) {
        colorClipboard = color;
    }
}
