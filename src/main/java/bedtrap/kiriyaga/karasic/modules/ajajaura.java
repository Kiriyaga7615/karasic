package bedtrap.kiriyaga.karasic.modules;

import bedtrap.kiriyaga.karasic.KarasicAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.minecraft.entity.effect.StatusEffects.HASTE;

public class ajajaura extends Module {

    public enum HpMode {toggle, pause}

    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgRanges = settings.createGroup("Ranges");
    private final SettingGroup sgRage = settings.createGroup("Rage");
    private final SettingGroup sgAuto = settings.createGroup("Auto");
    private final SettingGroup sgRefill = settings.createGroup("Refill");
    private final SettingGroup sgMisc = settings.createGroup("Misc");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //place
    private final Setting<Integer> delay = sgPlace.add(new IntSetting.Builder().name("delay").description("delay to place beds").defaultValue(10).min(0).sliderMax(15).build());
    private final Setting<Boolean> antiSelf = sgPlace.add(new BoolSetting.Builder().name("anti-self").description("").defaultValue(false).build());
    private final Setting<Boolean> selfFillDetect = sgPlace.add(new BoolSetting.Builder().name("rubber-fill-detect").description("pauses if target burrowed etc").defaultValue(true).build());
    private final Setting<Boolean> antiSneak = sgPlace.add(new BoolSetting.Builder().name("anti-sneak").description("").defaultValue(false).build());
    //ranges
    private final Setting<Double> targetRange = sgRanges.add(new DoubleSetting.Builder().name("target-range").description("The range at which players can be targeted.").defaultValue(7).min(0).sliderMax(9).build());
    private final Setting<Double> ineractRange = sgRanges.add(new DoubleSetting.Builder().name("interact-range").description("The max range to break beds (set it lower than target range)").defaultValue(5.5).min(0).sliderMax(7).build());
    private final Setting<Boolean> interactRender = sgRanges.add(new BoolSetting.Builder().name("only-interact-render").description("will render beds only if it in interact range").defaultValue(true).build());
    //rage!!!
    private final Setting<Boolean> rage = sgRage.add(new BoolSetting.Builder().name("rage").description("rage mode").defaultValue(false).build());
    private final Setting<Boolean> fastFirstBreak = sgRage.add(new BoolSetting.Builder().name("fast-first-break").description("no delay after first break").visible(rage::get).defaultValue(true).build());
    private final Setting<Boolean> bedSaver = sgRage.add(new BoolSetting.Builder().name("bed-saver(test)").description("stop bed placing if it cant damage enemy").visible(rage::get).defaultValue(false).build());
    private final Setting<Boolean> bedCleaner = sgRage.add(new BoolSetting.Builder().name("bed-cleaner").description("will break uselles beds").visible(rage::get).defaultValue(true).build());
    private final Setting<Integer> horizontalRage = sgRage.add(new IntSetting.Builder().name("horizontal-rage").description("will rage").defaultValue(7).min(1).sliderMin(1).sliderMax(15).visible(rage::get).build());
    private final Setting<Integer> upRage = sgRage.add(new IntSetting.Builder().name("up-rage").description("will rage while enemy Y higher than your Y").defaultValue(3).min(1).sliderMin(1).sliderMax(10).visible(rage::get).build());
    private final Setting<Integer> downRage = sgRage.add(new IntSetting.Builder().name("down-rage").description("will rage while enemy Y lower than your Y").defaultValue(3).min(1).sliderMin(1).sliderMax(10).visible(rage::get).build());
    //auto
    private final Setting<Boolean> placeObbsidian = sgAuto.add(new BoolSetting.Builder().name("place-obsidian").description("will place obsidian to prevent enemy escape").defaultValue(false).build());
    private final Setting<Boolean> trapBreaker = sgAuto.add(new BoolSetting.Builder().name("trap-breaker").description("will break self trap blocks!").defaultValue(false).build());
    private final Setting<Boolean> burrowBreaker = sgAuto.add(new BoolSetting.Builder().name("burrow-breaker").description("will break burrow blocks!").defaultValue(false).build());
    private final Setting<Boolean> stringBreaker = sgAuto.add(new BoolSetting.Builder().name("string-breaker").description("will break strings!").defaultValue(false).build());
    private final Setting<Boolean> speedMine = sgAuto.add(new BoolSetting.Builder().name("speed-mine").description("will give you haste effect!").defaultValue(false).build());
    //refill
    private final Setting<Boolean> hotbarRefill = sgRefill.add(new BoolSetting.Builder().name("hotbar-refill").description("auto bed refill").defaultValue(true).build());
    private final Setting<Integer> hotbarRefillSlot = sgRefill.add(new IntSetting.Builder().name("hotbar-refill-slot").description("slot to bed refill").defaultValue(7).min(1).max(9).sliderMin(1).sliderMax(9).visible(hotbarRefill::get).build());
    //pause
    private final Setting<Boolean> pauseOnEat = sgMisc.add(new BoolSetting.Builder().name("pause-on-eat").description("Pauses while eating.").defaultValue(true).build());
    private final Setting<Boolean> pauseOnDrink = sgMisc.add(new BoolSetting.Builder().name("pause-on-drink").description("Pauses while drinking.").defaultValue(true).build());
    private final Setting<Boolean> pauseOnMine = sgMisc.add(new BoolSetting.Builder().name("pause-on-mine").description("Pauses while mining.").defaultValue(false).build());
    private final Setting<Boolean> disableOnNoBeds = sgMisc.add(new BoolSetting.Builder().name("disable-on-no-beds").description("will disable if u havent beds").defaultValue(false).build());
    private final Setting<Boolean> hp = sgMisc.add(new BoolSetting.Builder().name("hp-disable").description("auto disable at certain hp").defaultValue(false).build());
    public final Setting<ajajaura.HpMode> hpMode = sgMisc.add(new EnumSetting.Builder<ajajaura.HpMode>().name("Hp-Mode").defaultValue(HpMode.pause).build());
    private final Setting<Integer> hpInt = sgMisc.add(new IntSetting.Builder().name("health").description("").defaultValue(12).min(1).sliderMin(1).sliderMax(36).visible(hp::get).build());
    //render
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder().name("render").description("Renders the block where it is placing a bed.").defaultValue(true).build());
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color for positions to be placed.").defaultValue(new SettingColor(30, 35, 122, 75)).build());
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color for positions to be placed.").defaultValue(new SettingColor(30, 35, 122)).build());

    private PlayerEntity target;
    private int lmaoo;
    private int tickRate;
    private boolean huita;

    public ajajaura() {
        super(KarasicAddon.karasic, "ajaj-aura", "bed aura by ajaj#7615");
    }


    //no
    //no
    //no
    //no
    //no
    //no
}
