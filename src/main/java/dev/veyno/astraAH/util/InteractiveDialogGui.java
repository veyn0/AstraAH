package dev.veyno.astraAH.util;


import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class InteractiveDialogGui {

    private final Component title;
    private Component externalTitle;
    private boolean canCloseWithEscape = true;
    private DialogBase.DialogAfterAction afterAction;
    private final List<DialogBody> body = new ArrayList<>();
    private final List<DialogInput> inputs = new ArrayList<>();
    private DialogType type;

    private InteractiveDialogGui(@NotNull Component title) {
        this.title = Objects.requireNonNull(title, "title");
    }

    public static @NotNull InteractiveDialogGui create(@NotNull Component title) {
        return new InteractiveDialogGui(title);
    }

    public @NotNull InteractiveDialogGui externalTitle(@Nullable Component externalTitle) {
        this.externalTitle = externalTitle;
        return this;
    }

    public @NotNull InteractiveDialogGui canCloseWithEscape(boolean canCloseWithEscape) {
        this.canCloseWithEscape = canCloseWithEscape;
        return this;
    }

    public @NotNull InteractiveDialogGui afterAction(@Nullable DialogBase.DialogAfterAction afterAction) {
        this.afterAction = afterAction;
        return this;
    }

    public @NotNull InteractiveDialogGui message(@NotNull Component component) {
        this.body.add(DialogBody.plainMessage(component));
        return this;
    }

//    public @NotNull InteractiveDialogGui item(@NotNull ItemStack itemStack) {
//        this.body.add(DialogBody.item(itemStack));
//        return this;
//    }

    public @NotNull InteractiveDialogGui body(@NotNull DialogBody bodyElement) {
        this.body.add(bodyElement);
        return this;
    }

    public @NotNull InteractiveDialogGui bodies(@NotNull List<? extends DialogBody> bodyElements) {
        this.body.addAll(bodyElements);
        return this;
    }

    public @NotNull InteractiveDialogGui input(@NotNull DialogInput input) {
        this.inputs.add(input);
        return this;
    }

    public @NotNull InteractiveDialogGui inputs(@NotNull List<? extends DialogInput> inputs) {
        this.inputs.addAll(inputs);
        return this;
    }

    public @NotNull InteractiveDialogGui type(@NotNull DialogType type) {
        this.type = Objects.requireNonNull(type, "type");
        return this;
    }

    public @NotNull InteractiveDialogGui notice(
            @NotNull Component buttonText,
            @Nullable Component tooltip,
            @Nullable Runnable callback
    ) {
        this.type = DialogType.notice(actionButton(buttonText, tooltip, callback));
        return this;
    }

    public @NotNull InteractiveDialogGui notice(
            @NotNull Component buttonText,
            @Nullable Component tooltip,
            @Nullable Consumer<DialogContext> callback
    ) {
        this.type = DialogType.notice(actionButton(buttonText, tooltip, callback));
        return this;
    }

    public @NotNull InteractiveDialogGui confirmation(
            @NotNull Component yesText,
            @Nullable Component yesTooltip,
            @Nullable Runnable yesCallback,
            @NotNull Component noText,
            @Nullable Component noTooltip,
            @Nullable Runnable noCallback
    ) {
        this.type = DialogType.confirmation(
                actionButton(yesText, yesTooltip, yesCallback),
                actionButton(noText, noTooltip, noCallback)
        );
        return this;
    }

    public @NotNull InteractiveDialogGui confirmation(
            @NotNull Component yesText,
            @Nullable Component yesTooltip,
            @Nullable Consumer<DialogContext> yesCallback,
            @NotNull Component noText,
            @Nullable Component noTooltip,
            @Nullable Consumer<DialogContext> noCallback
    ) {
        this.type = DialogType.confirmation(
                actionButton(yesText, yesTooltip, yesCallback),
                actionButton(noText, noTooltip, noCallback)
        );
        return this;
    }

//    public @NotNull InteractiveDialogGui multiAction(@NotNull List<ActionButton> actions) {
//        this.type = DialogType.multiAction(actions);
//        return this;
//    }
//
//    public @NotNull InteractiveDialogGui multiActionSpecs(@NotNull List<ActionSpec> actions) {
//        List<ActionButton> built = new ArrayList<>(actions.size());
//        for (ActionSpec spec : actions) {
//            built.add(spec.build());
//        }
//        this.type = DialogType.multiAction(built);
//        return this;
//    }

    public @NotNull Dialog build() {
        if (this.type == null) {
            throw new IllegalStateException("No dialog type configured.");
        }

        DialogBase.Builder baseBuilder = DialogBase.builder(this.title)
                .canCloseWithEscape(this.canCloseWithEscape);

        if (this.externalTitle != null) {
            baseBuilder.externalTitle(this.externalTitle);
        }

        if (this.afterAction != null) {
            baseBuilder.afterAction(this.afterAction);
        }

        if (!this.body.isEmpty()) {
            baseBuilder.body(this.body);
        }

        if (!this.inputs.isEmpty()) {
            baseBuilder.inputs(this.inputs);
        }

        DialogBase base = baseBuilder.build();

        return Dialog.create(builder -> builder.empty()
                .base(base)
                .type(this.type)
        );
    }

    public @NotNull DialogLike asDialogLike() {
        return build();
    }

    public void show(@NotNull Audience audience) {
        audience.showDialog(build());
    }

    public void show(@NotNull Player player) {
        player.showDialog(build());
    }

    public static @NotNull ActionSpec action(
            @NotNull Component text
    ) {
        return new ActionSpec(text);
    }

    public static @NotNull ActionButton actionButton(
            @NotNull Component text,
            @Nullable Component tooltip,
            @Nullable Runnable callback
    ) {
        ActionButton.Builder builder = ActionButton.builder(text);

        if (tooltip != null) {
            builder.tooltip(tooltip);
        }

        if (callback != null) {
            builder.action(DialogAction.customClick(
                    (view, audience) -> callback.run(),
                    ClickCallback.Options.builder().uses(1).build()
            ));
        } else {
            builder.action(null);
        }

        return builder.build();
    }

    public static @NotNull ActionButton actionButton(
            @NotNull Component text,
            @Nullable Component tooltip,
            @Nullable Consumer<DialogContext> callback
    ) {
        ActionButton.Builder builder = ActionButton.builder(text);

        if (tooltip != null) {
            builder.tooltip(tooltip);
        }

        if (callback != null) {
            builder.action(DialogAction.customClick(
                    (view, audience) -> callback.accept(new DialogContext(view, audience)),
                    ClickCallback.Options.builder().uses(1).build()
            ));
        } else {
            builder.action(null);
        }

        return builder.build();
    }

    public static @NotNull ActionButton actionButton(
            @NotNull Component text,
            @Nullable Component tooltip,
            @Nullable BiConsumer<DialogResponseView, Audience> callback
    ) {
        ActionButton.Builder builder = ActionButton.builder(text);

        if (tooltip != null) {
            builder.tooltip(tooltip);
        }

        if (callback != null) {
            builder.action(DialogAction.customClick(
                    callback::accept,
                    ClickCallback.Options.builder().uses(1).build()
            ));
        } else {
            builder.action(null);
        }

        return builder.build();
    }

    public static final class ActionSpec {

        private final Component text;
        private Component tooltip;
        private Integer width;
        private Integer uses;
        private ClickCallback.Options options;
        private Runnable runnable;
        private Consumer<DialogContext> consumer;
        private BiConsumer<DialogResponseView, Audience> biConsumer;

        private ActionSpec(@NotNull Component text) {
            this.text = Objects.requireNonNull(text, "text");
        }

        public @NotNull ActionSpec tooltip(@Nullable Component tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public @NotNull ActionSpec width(int width) {
            this.width = width;
            return this;
        }

        public @NotNull ActionSpec uses(int uses) {
            this.uses = uses;
            return this;
        }

        public @NotNull ActionSpec options(@Nullable ClickCallback.Options options) {
            this.options = options;
            return this;
        }

        public @NotNull ActionSpec onClick(@Nullable Runnable runnable) {
            this.runnable = runnable;
            this.consumer = null;
            this.biConsumer = null;
            return this;
        }

        public @NotNull ActionSpec onClick(@Nullable Consumer<DialogContext> consumer) {
            this.consumer = consumer;
            this.runnable = null;
            this.biConsumer = null;
            return this;
        }

        public @NotNull ActionSpec onClick(@Nullable BiConsumer<DialogResponseView, Audience> biConsumer) {
            this.biConsumer = biConsumer;
            this.runnable = null;
            this.consumer = null;
            return this;
        }

        public @NotNull ActionButton build() {
            ActionButton.Builder builder = ActionButton.builder(this.text);

            if (this.tooltip != null) {
                builder.tooltip(this.tooltip);
            }

            if (this.width != null) {
                builder.width(this.width);
            }

            DialogAction action = null;

            if (this.runnable != null) {
                action = DialogAction.customClick(
                        (view, audience) -> this.runnable.run(),
                        effectiveOptions()
                );
            } else if (this.consumer != null) {
                action = DialogAction.customClick(
                        (view, audience) -> this.consumer.accept(new DialogContext(view, audience)),
                        effectiveOptions()
                );
            } else if (this.biConsumer != null) {
                action = DialogAction.customClick(this.biConsumer::accept, effectiveOptions());
            }

            builder.action(action);
            return builder.build();
        }

        private @NotNull ClickCallback.Options effectiveOptions() {
            if (this.options != null) {
                return this.options;
            }

            ClickCallback.Options.Builder builder = ClickCallback.Options.builder();
            if (this.uses != null) {
                builder.uses(this.uses);
            }
            return builder.build();
        }
    }

    public static final class DialogContext {

        private final DialogResponseView view;
        private final Audience audience;

        public DialogContext(@Nullable DialogResponseView view, @NotNull Audience audience) {
            this.view = view;
            this.audience = Objects.requireNonNull(audience, "audience");
        }

        public @Nullable DialogResponseView view() {
            return this.view;
        }

        public @NotNull Audience audience() {
            return this.audience;
        }

        public boolean hasView() {
            return this.view != null;
        }

        public @Nullable Player playerOrNull() {
            return this.audience instanceof Player player ? player : null;
        }

        public @NotNull Player player() {
            if (!(this.audience instanceof Player player)) {
                throw new IllegalStateException("Audience is not a Player.");
            }
            return player;
        }

        public boolean getBoolean(@NotNull String key) {
            ensureView();
            Boolean value = this.view.getBoolean(key);
            return value != null && value;
        }

        public int getInt(@NotNull String key) {
            ensureView();
            Number value = this.view.getFloat(key);
            if (value == null) {
                throw new IllegalStateException("Missing numeric dialog input: " + key);
            }
            return value.intValue();
        }

        public float getFloat(@NotNull String key) {
            ensureView();
            Number value = this.view.getFloat(key);
            if (value == null) {
                throw new IllegalStateException("Missing numeric dialog input: " + key);
            }
            return value.floatValue();
        }

        public @Nullable String getText(@NotNull String key) {
            ensureView();
            return this.view.getText(key);
        }

//        public @Nullable String getOption(@NotNull String key) {
//            ensureView();
//            return this.view.getString(key);
//        }

        private void ensureView() {
            if (this.view == null) {
                throw new IllegalStateException("DialogResponseView is null.");
            }
        }
    }
}