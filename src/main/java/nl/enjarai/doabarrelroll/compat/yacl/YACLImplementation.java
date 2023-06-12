package nl.enjarai.doabarrelroll.compat.yacl;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nl.enjarai.doabarrelroll.DoABarrelRoll;
import nl.enjarai.doabarrelroll.ModKeybindings;
import nl.enjarai.doabarrelroll.config.ActivationBehaviour;
import nl.enjarai.doabarrelroll.config.ModConfig;

public class YACLImplementation {
    public static Screen generateConfigScreen(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(getText("title"))
                .category(ConfigCategory.createBuilder()
                        .name(getText("general"))
                        .option(getBooleanOption("general", "mod_enabled", false, false)
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("config.do_a_barrel_roll.general.mod_enabled.description",
                                                KeyBindingHelper.getBoundKeyOf(ModKeybindings.TOGGLE_ENABLED).getLocalizedText()))
                                        .build())
                                .binding(true, () -> ModConfig.INSTANCE.getModEnabled(), value -> ModConfig.INSTANCE.setModEnabled(value))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(getText("controls"))
                                .option(getBooleanOption("controls", "switch_roll_and_yaw", true, false)
                                        .binding(false, () -> ModConfig.INSTANCE.getSwitchRollAndYaw(), value -> ModConfig.INSTANCE.setSwitchRollAndYaw(value))
                                        .build())
                                .option(getBooleanOption("controls", "invert_pitch", true, false)
                                        .binding(false, () -> ModConfig.INSTANCE.getInvertPitch(), value -> ModConfig.INSTANCE.setInvertPitch(value))
                                        .build())
                                .option(getBooleanOption("controls", "momentum_based_mouse", true, false)
                                        .binding(false, () -> ModConfig.INSTANCE.getMomentumBasedMouse(), value -> ModConfig.INSTANCE.setMomentumBasedMouse(value))
                                        .build())
                                .option(getOption(Double.class, "controls", "momentum_mouse_deadzone", true, false)
                                        .controller(option -> getDoubleSlider(option, 0.0, 1.0, 0.01))
                                        .binding(0.2, () -> ModConfig.INSTANCE.getMomentumMouseDeadzone(), value -> ModConfig.INSTANCE.setMomentumMouseDeadzone(value))
                                        .build())
                                .option(getOption(ActivationBehaviour.class, "controls", "activation_behaviour", false, false)
                                        .description(behaviour -> OptionDescription.createBuilder()
                                                .text(getText("controls", "activation_behaviour.description")
                                                        .append(getText("controls", "activation_behaviour.description." + behaviour.name().toLowerCase())))
                                                .build())
                                        .controller(option1 -> EnumControllerBuilder.create(option1)
                                                .enumClass(ActivationBehaviour.class))
                                        .binding(ActivationBehaviour.VANILLA, () -> ModConfig.INSTANCE.getActivationBehaviour(), value -> ModConfig.INSTANCE.setActivationBehaviour(value))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(getText("hud"))
                                .option(getBooleanOption("hud", "show_horizon", true, true)
                                        .binding(false, () -> ModConfig.INSTANCE.getShowHorizon(), value -> ModConfig.INSTANCE.setShowHorizon(value))
                                        .build())
                                .option(getBooleanOption("controls", "show_momentum_widget", true, true)
                                        .binding(true, () -> ModConfig.INSTANCE.getShowMomentumWidget(), value -> ModConfig.INSTANCE.setShowMomentumWidget(value))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(getText("banking"))
                                .option(getBooleanOption("banking", "enable_banking", true, false)
                                        .binding(true, () -> ModConfig.INSTANCE.getEnableBanking(), value -> ModConfig.INSTANCE.setEnableBanking(value))
                                        .build())
                                .option(getOption(Double.class, "banking", "banking_strength", false, false)
                                        .controller(option -> getDoubleSlider(option, 0.0, 100.0, 1.0))
                                        .binding(20.0, () -> ModConfig.INSTANCE.getBankingStrength(), value -> ModConfig.INSTANCE.setBankingStrength(value))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(getText("thrust"))
                                .collapsed(true)
                                .option(getBooleanOption("thrust", "enable_thrust", true, false)
                                        .binding(false, () -> ModConfig.INSTANCE.getEnableThrustClient(), value -> ModConfig.INSTANCE.setEnableThrust(value))
                                        .build())
                                .option(getOption(Double.class, "thrust", "max_thrust", true, false)
                                        .controller(option -> getDoubleSlider(option, 0.1, 10.0, 0.1))
                                        .binding(2.0, () -> ModConfig.INSTANCE.getMaxThrust(), value -> ModConfig.INSTANCE.setMaxThrust(value))
                                        .build())
                                .option(getOption(Double.class, "thrust", "thrust_acceleration", true, false)
                                        .controller(option -> getDoubleSlider(option, 0.1, 1.0, 0.1))
                                        .binding(0.1, () -> ModConfig.INSTANCE.getThrustAcceleration(), value -> ModConfig.INSTANCE.setThrustAcceleration(value))
                                        .build())
                                .option(getBooleanOption("thrust", "thrust_particles", false, false)
                                        .binding(true, () -> ModConfig.INSTANCE.getThrustParticles(), value -> ModConfig.INSTANCE.setThrustParticles(value))
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(getText("sensitivity"))
                        .group(OptionGroup.createBuilder()
                                .name(getText("smoothing"))
                                .option(getBooleanOption("smoothing", "smoothing_enabled", false, false)
                                        .binding(true, () -> ModConfig.INSTANCE.getSmoothingEnabled(), value -> ModConfig.INSTANCE.setSmoothingEnabled(value))
                                        .build())
                                .option(getOption(Double.class, "smoothing", "smoothing_pitch", false, false)
                                        .controller(option -> getDoubleSlider(option, 0.1, 5.0, 0.1))
                                        .binding(1.0, () -> ModConfig.INSTANCE.getSmoothingPitch(), value -> ModConfig.INSTANCE.setSmoothingPitch(value))
                                        .build())
                                .option(getOption(Double.class, "smoothing", "smoothing_yaw", false, false)
                                        .controller(option -> getDoubleSlider(option, 0.1, 5.0, 0.1))
                                        .binding(0.4, () -> ModConfig.INSTANCE.getSmoothingYaw(), value -> ModConfig.INSTANCE.setSmoothingYaw(value))
                                        .build())
                                .option(getOption(Double.class, "smoothing", "smoothing_roll", false, false)
                                        .controller(option -> getDoubleSlider(option, 0.1, 5.0, 0.1))
                                        .binding(1.0, () -> ModConfig.INSTANCE.getSmoothingRoll(), value -> ModConfig.INSTANCE.setSmoothingRoll(value))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(getText("desktop"))
                                .option(getOption(Double.class, "desktop", "pitch", false, false)
                                        .controller(option -> getDoubleSlider(option, 0.1, 10.0, 0.1))
                                        .binding(1.0, () -> ModConfig.INSTANCE.getDesktopPitch(), value -> ModConfig.INSTANCE.setDesktopPitch(value))
                                        .build())
                                .option(getOption(Double.class, "desktop", "yaw", false, false)
                                        .controller(option -> getDoubleSlider(option, 0.1, 10.0, 0.1))
                                        .binding(0.4, () -> ModConfig.INSTANCE.getDesktopYaw(), value -> ModConfig.INSTANCE.setDesktopYaw(value))
                                        .build())
                                .option(getOption(Double.class, "desktop", "roll", false, false)
                                        .controller(option -> getDoubleSlider(option, 0.1, 10.0, 0.1))
                                        .binding(1.0, () -> ModConfig.INSTANCE.getDesktopRoll(), value -> ModConfig.INSTANCE.setDesktopRoll(value))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(getText("controller"))
                                .collapsed(!(FabricLoader.getInstance().isModLoaded("controlify") || FabricLoader.getInstance().isModLoaded("midnightcontrols")))
                                .description(OptionDescription.createBuilder()
                                        .text(getText("controller.description"))
                                        .build())
                                .option(getOption(Double.class, "controller", "pitch", false, false)
                                        .controller(option -> getDoubleSlider(option, 0.1, 10.0, 0.1))
                                        .binding(1.0, () -> ModConfig.INSTANCE.getControllerPitch(), value -> ModConfig.INSTANCE.setControllerPitch(value))
                                        .build())
                                .option(getOption(Double.class, "controller", "yaw", false, false)
                                        .controller(option -> getDoubleSlider(option, 0.1, 10.0, 0.1))
                                        .binding(0.4, () -> ModConfig.INSTANCE.getControllerYaw(), value -> ModConfig.INSTANCE.setControllerYaw(value))
                                        .build())
                                .option(getOption(Double.class, "controller", "roll", false, false)
                                        .controller(option -> getDoubleSlider(option, 0.1, 10.0, 0.1))
                                        .binding(1.0, () -> ModConfig.INSTANCE.getControllerRoll(), value -> ModConfig.INSTANCE.setControllerRoll(value))
                                        .build())
                                .build())
                        .build())
                .save(ModConfig.INSTANCE::save)
                .build()
                .generateScreen(parent);
    }

    private static <T> Option.Builder<T> getOption(Class<T> clazz, String category, String key, boolean description, boolean image) {
        Option.Builder<T> builder = Option.<T>createBuilder()
                .name(getText(category, key));
        var descBuilder = OptionDescription.createBuilder();
        if (description) {
            descBuilder.text(getText(category, key + ".description"));
        }
        if (image) {
            descBuilder.image(DoABarrelRoll.id("textures/gui/config/images/" + category + "/" + key + ".png"), 480, 275);
        }
        builder.description(descBuilder.build());
        return builder;
    }

    private static Option.Builder<Boolean> getBooleanOption(String category, String key, boolean description, boolean image) {
        return getOption(Boolean.class, category, key, description, image)
                .controller(TickBoxControllerBuilder::create);
    }

    private static DoubleSliderControllerBuilder getDoubleSlider(Option<Double> option, double min, double max, double step) {
        return DoubleSliderControllerBuilder.create(option)
                .range(min, max)
                .step(step);
    }

    private static MutableText getText(String category, String key) {
        return Text.translatable("config.do_a_barrel_roll." + category + "." + key);
    }

    private static MutableText getText(String key) {
        return Text.translatable("config.do_a_barrel_roll." + key);
    }
}