package bedtrap.kiriyaga.karasic.modules.hud;

import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.HudElement;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import java.util.ArrayList;

public class hudnotifications extends HudElement {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> removeDelay = sgGeneral.add(new IntSetting.Builder().name("remove-delay").description("The delay to remove messages").defaultValue(300).min(1).sliderMax(2000).build());
    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder().name("text-color").description("color lmao").defaultValue(new SettingColor(255, 255, 255, 255)).build());
    private final Setting<SettingColor> backgroundColor = sgGeneral.add(new ColorSetting.Builder().name("background-color").description("color lmao").defaultValue(new SettingColor(0, 0, 0, 140)).build());

    public hudnotifications(HUD hud) {
        super(hud, "notificatiobs", "requires notificationsetting turned on", false);
    }

    public static ArrayList<String> messages = new ArrayList<>();
    int timer1;

    @Override
    public void update(HudRenderer renderer) {
        messageHelper();
        double width = 0;
        double height = 0;
        int i = 0;
        if (messages.isEmpty()) {
            String t = "Notifications";
            width = Math.max(width, renderer.textWidth(t));
            height += renderer.textHeight();
        } else {
            for (String mes : messages) {
                width = Math.max(width, renderer.textWidth(mes));
                height += renderer.textHeight();
                if (i > 0) height += 2;
                i++;
            }
        }
        box.setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        messageHelper();
        double x = box.getX();
        double y = box.getY();

        int w = (int) box.width;
        int h = (int) box.height;

        if (isInEditor()) {
            renderer.text("Notifications", x, y, textColor.get());
            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(x, y, w, h, backgroundColor.get());
            Renderer2D.COLOR.render(null);
            return;
        }

        if (!messages.isEmpty()){
            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(x, y, w, h, backgroundColor.get());
            Renderer2D.COLOR.render(null);
        }
        int i = 0;
        if (messages.isEmpty()) {
            String t = "";
            renderer.text(t, x + box.alignX(renderer.textWidth(t)), y, textColor.get());
        } else {
            for (String mes: messages) {


                renderer.text(mes, x + box.alignX(renderer.textWidth(mes)), y, textColor.get());
                y += renderer.textHeight();
                if (i > 0) y += 2;
                i++;
            }
        }
    }

    public void messageHelper() {

        if (timer1 >= removeDelay.get() && !messages.isEmpty()){
            messages.remove(0);
            timer1 = 0;
        } else if (!messages.isEmpty()) timer1++;

    }

}
