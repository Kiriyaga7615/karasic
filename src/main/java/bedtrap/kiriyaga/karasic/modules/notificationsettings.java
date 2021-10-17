package bedtrap.kiriyaga.karasic.modules;

import bedtrap.kiriyaga.karasic.KarasicAddon;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Util;

import java.util.Random;
import java.util.UUID;

import static bedtrap.kiriyaga.karasic.modules.hud.hudnotifications.messages;


public class notificationsettings extends Module {


    private final SettingGroup sgTotemPop = settings.createGroup("Totem Pops");
    private final SettingGroup sgArmorBreak = settings.createGroup("Armor Break");


    // Pops
    private final Setting<Boolean> totemPop = sgTotemPop.add(new BoolSetting.Builder().name("pops").description("allow pop notif").defaultValue(true).build());
    private final Setting<Boolean> totemSelfIgnore = sgTotemPop.add(new BoolSetting.Builder().name("self-ignore").description("ignore self pops").defaultValue(false).build());
    private final Setting<Boolean> totemFriendIgnore = sgTotemPop.add(new BoolSetting.Builder().name("friend-ignore").description("ignore friends pops").defaultValue(false).build());

    //armor break
    private final Setting<Boolean> armorDurability = sgArmorBreak.add(new BoolSetting.Builder().name("armor-durability").description("yes").defaultValue(true).build());
    private final Setting<Integer> damage = sgArmorBreak.add(new IntSetting.Builder().name("damage").description("What damage should an armor item have to be notified.").defaultValue(100).visible(armorDurability::get).build());
    private final Setting<Integer> msgNum = sgArmorBreak.add(new IntSetting.Builder().name("messages").description("What damage should an armor item have to be notified.").defaultValue(5).visible(armorDurability::get).build());

    private final Object2IntMap<UUID> totemPopMap = new Object2IntOpenHashMap<>();
    private final Object2IntMap<UUID> chatIdMap = new Object2IntOpenHashMap<>();

    private final Random random = new Random();
    private int num = 0;
    private boolean bBoots = false;
    private boolean bLeggings = false;
    private boolean bArmor = false;
    private boolean bHead = false;

    public notificationsettings() {
        super(KarasicAddon.karasic, "notification-setting", "Notifs u in hud.");
    }

    // Pops

    @Override
    public void onActivate() {
        totemPopMap.clear();
        chatIdMap.clear();
        num = 1;
        bBoots = false;
        bLeggings = false;
        bArmor = false;
        bHead = false;
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        totemPopMap.clear();
        chatIdMap.clear();
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (!totemPop.get()) return;
        if (!(event.packet instanceof EntityStatusS2CPacket)) return;

        EntityStatusS2CPacket p = (EntityStatusS2CPacket) event.packet;
        if (p.getStatus() != 35) return;

        Entity entity = p.getEntity(mc.world);

        if (!(entity instanceof PlayerEntity)) return;

        if ((entity.equals(mc.player) && totemSelfIgnore.get()) || (!Friends.get().isFriend(((PlayerEntity) entity)) && totemFriendIgnore.get())
        ) return;

        synchronized (totemPopMap) {
            int pops = totemPopMap.getOrDefault(entity.getUuid(), 0);
            totemPopMap.put(entity.getUuid(), ++pops);

            messages.add(entity.getEntityName() + " got " + pops + " pop's!");

        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!totemPop.get()) return;
        synchronized (totemPopMap) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (!totemPopMap.containsKey(player.getUuid())) continue;

                if (player.deathTime > 0 || player.getHealth() <= 0) {
                    int pops = totemPopMap.removeInt(player.getUuid());

                    messages.add(player.getEntityName() + " died after " + pops + " pop's!");
                    chatIdMap.removeInt(player.getUuid());
                }
            }
        }

        assert mc.player != null;
        assert mc.world != null;
        ItemStack boots = mc.player.getInventory().getArmorStack(0);
        int durabilityBoots = boots.getMaxDamage() - boots.getDamage();
        ItemStack leggings = mc.player.getInventory().getArmorStack(1);
        int durabilityLeggings = leggings.getMaxDamage() - leggings.getDamage();
        ItemStack armour = mc.player.getInventory().getArmorStack(2);
        int durabilityArmor = armour.getMaxDamage() - armour.getDamage();
        ItemStack head = mc.player.getInventory().getArmorStack(3);
        int durabilityHead = head.getMaxDamage() - head.getDamage();

        if (boots.isEmpty() && leggings.isEmpty() && armour.isEmpty() && head.isEmpty()) return;

        if (durabilityBoots < damage.get() && !bBoots) {
            messages.add("Your Boots is about to break.");
            if (num < msgNum.get()) {
                num++;
                return;
            }
            bBoots = true;
            num = 1;
        } else if (durabilityBoots > damage.get() && bBoots) {
            bBoots = false;
            num = 1;
        }
        if (durabilityLeggings < damage.get() && !bLeggings) {
            messages.add("Your Leggings is about to break.");
            if (num < msgNum.get()) {
                num++;
                return;
            }
            bLeggings = true;
            num = 1;
        } else if (durabilityLeggings > damage.get() && bLeggings) {
            bLeggings = false;
            num = 1;
        }
        if (durabilityArmor < damage.get() && !bArmor) {
            messages.add("Your Chestplate is about to break.");
            if (num < msgNum.get()) {
                num++;
                return;
            }
            bArmor = true;
            num = 1;
        } else if (durabilityArmor > damage.get() && bArmor) {
            bArmor = false;
            num = 1;
        }
        if (durabilityHead < damage.get() && !bHead) {

            messages.add("Your Helmet is about to break.");
            if (num < msgNum.get()) {
                num++;
                return;
            }
            bHead = true;
            num = 1;
        } else if (durabilityHead > damage.get() && bHead) {
            bHead = false;
            num = 1;
        }
    }

    private int getChatId(Entity entity) {
        return chatIdMap.computeIntIfAbsent(entity.getUuid(), value -> random.nextInt());
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
