package taintedmagic.client;

import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import taintedmagic.*;

@EventBusSubscriber(modid = TaintedMagic.MODID, value = Dist.CLIENT)
public final class ClientSmoke {
    private static int ticks;
    private static boolean started;
    @SubscribeEvent static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("taintedmagic.clientSmoke")) return;
        Minecraft mc = Minecraft.getInstance();
        if (!started && mc.getOverlay() == null && mc.screen != null) { mc.setScreen(new Gallery()); started = true; }
        if (!started) return;
        ticks++;
        if (ticks == 60) Screenshot.grab(mc.gameDirectory, "tainted-magic-items.png", mc.getMainRenderTarget(), message -> {
            try { Files.writeString(Path.of(mc.gameDirectory.toString(), "client-smoke-result.txt"), "Rendered all " + Content.ALL.size() + " item forms and saved gallery screenshot. " + message.getString()); }
            catch (java.io.IOException exception) { throw new RuntimeException(exception); }
        });
        if (ticks >= 100 && Files.exists(Path.of(mc.gameDirectory.toString(), "client-smoke-result.txt"))) mc.stop();
    }
    private static final class Gallery extends Screen {
        Gallery() { super(Component.literal("Tainted Magic 1.21.1 — render validation")); }
        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xFF181222);
            graphics.drawCenteredString(font, title, width / 2, 12, 0xFFEECCFF);
            int index = 0;
            int columns = 10;
            int startX = width / 2 - columns * 18;
            for (var item : Content.ALL.values()) {
                int x = startX + (index % columns) * 36;
                int y = 38 + (index / columns) * 34;
                graphics.fill(x - 3, y - 3, x + 22, y + 22, 0xFF40334F);
                graphics.renderItem(new ItemStack(item.get()), x + 1, y + 1);
                index++;
            }
            graphics.drawCenteredString(font, Component.literal(index + " item forms"), width / 2, height - 15, 0xFFCCBBEE);
        }
    }
}
