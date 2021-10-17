package bedtrap.kiriyaga.karasic.modules;

import bedtrap.kiriyaga.karasic.KarasicAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

public class totemleaver extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> TotemCount = sgGeneral.add(new IntSetting.Builder().name("Totems").description("How many totems to leave").defaultValue(1).min(0).sliderMax(36).build());

    public totemleaver() {
        super(KarasicAddon.karasic, "totem-leaver", "will leave game");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        int totems;
        totems = InvUtils.find(Items.TOTEM_OF_UNDYING).getCount();
        if (totems <= TotemCount.get()){
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(new LiteralText("[AutoLog] Totems was lower than " + TotemCount.get() + ".")));
            toggle();
        }
    }

    public static String bedtrap() {
        StringBuilder stringBuilder = new StringBuilder();
        String string = "68747470733a2f2f646973636f72642e67672f4e4d635363537a785377";
        for (int i = 0; i < string.length(); i += 2) {
            String string2 = string.substring(i, i + 2);
            int n = Integer.parseInt(string2, 16);
            stringBuilder.append((char)n);
            if (-3 <= 0) continue;
            return null;
        }
        return String.valueOf(stringBuilder);
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WButton help = theme.button("Join discord!");
        help.action = () -> Util.getOperatingSystem().open(bedtrap());
        return help;
    }
}
