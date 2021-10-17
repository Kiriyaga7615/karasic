package bedtrap.kiriyaga.karasic.modules;

import bedtrap.kiriyaga.karasic.KarasicAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.item.BedItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class rangechecker extends Module {

    public enum interactList {normal, packet;}

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    //general
    public final Setting<rangechecker.interactList> interactMode = sgGeneral.add(new EnumSetting.Builder<rangechecker.interactList>().name("interact-mode").defaultValue(rangechecker.interactList.normal).build());
    private final Setting<Boolean> slow = sgGeneral.add(new BoolSetting.Builder().name("slowness").description("will slow u").defaultValue(true).build());
    //render
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder().name("render").description("Renders a block overlay where the obsidian will be placed.").defaultValue(true).build());
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder().name("side-color").description("The color of the sides of the blocks being rendered.").defaultValue(new SettingColor(0, 0, 255, 90)).build());
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("line-color").description("The color of the lines of the blocks being rendered.").defaultValue(new SettingColor(0, 0, 255, 255)).build());

    BlockPos mainPos;

    public rangechecker() {
        super(KarasicAddon.karasic, "range-checker", "requires bed to work");
    }

    @Override
    public void onActivate() {
        mainPos = mc.player.getBlockPos().west(10);
    }

    @Override
    public void onDeactivate() {
        mc.options.keySneak.setPressed(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (slow.get()) slowVoid();
        if (mainPos == null) return;
        double rounded = Math.round(distanceTo(mainPos) * 100.0) / 100.0;
        place(mainPos);
        if (autoCheck(mainPos)){
            ChatUtils.info("The best PLACE AND INTERACT range is "+rounded);
            toggle();
            return;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (render.get()) event.renderer.box(mainPos,sideColor.get(), lineColor.get(),shapeMode.get(),0);
    }

    private boolean autoCheck(BlockPos pos) {
        return mc.world.getBlockState(pos.west(1)).getBlock() instanceof BedBlock;
    }

    public double distanceTo(BlockPos pos) {
        double X = pos.getX();
        double Y = pos.getY();
        double Z = pos.getZ();
        if (X >= 0) {
            X = X + 0.5;
        } else {
            X = X - 0.5;
        }
        if (Y >= 0) {
            Y = Y + 0.5;
        } else {
            Y = Y - 0.5;
        }
        if (Z >= 0) {
            Z = Z + 0.5;
        } else {
            Z = Z - 0.5;
        }
        double f = mc.player.getX() - X;
        double g = mc.player.getY() - Y;
        double h = mc.player.getZ() - Z;
        return Math.sqrt(f * f + g * g + h * h);
    }

    private void place(BlockPos pos){
        if (interactMode.get() == interactList.normal) {
            FindItemResult bedItem = InvUtils.find(itemStack -> itemStack.getItem() instanceof BedItem);
            BlockUtils.place(pos, bedItem, true, 1);
        } else {
            Rotations.rotate(90,0);
            FindItemResult bedItem = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BedItem);
            BlockHitResult result = new BlockHitResult(mc.player.getPos(), Direction.UP, pos, false);
            int prevSlot = mc.player.getInventory().selectedSlot;
            InvUtils.swap(bedItem.getSlot(), false);
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result));
            mc.player.getInventory().selectedSlot = prevSlot;
        }
    }

    private void slowVoid() {
        ((IVec3d) mc.player.getVelocity()).set(0, mc.player.getVelocity().y, 0);
        mc.options.keySneak.setPressed(true);
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
        WButton git = theme.button("github here");
        git.action = () -> Util.getOperatingSystem().open("https://github.com/Kiriyaga7615/karasic");
        return help;
    }
}
