//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.gui.screen;

import centric.pl.Main;
import centric.pl.functions.impl.misc.UnHook;
import centric.pl.johon0.utils.drag.DragManager;
import centric.pl.johon0.utils.drag.Dragging;
import centric.pl.johon0.utils.math.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.CommandSuggestionHelper;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

public class ChatScreen extends Screen {
    private String historyBuffer = "";
    private int sentHistoryCursor = -1;
    protected TextFieldWidget inputField;
    private String defaultInputFieldText = "";
    private CommandSuggestionHelper commandSuggestionHelper;
    public static boolean hide;
    Button button;

    public ChatScreen(String defaultText) {
        super(NarratorChatListener.EMPTY);
        this.defaultInputFieldText = defaultText;
    }

    protected void init() {
        MainWindow mainWindow = Minecraft.getInstance().getMainWindow();
        UnHook selfDestruct = Main.getInstance().getFunctionRegistry().getUnHook();
        if (!selfDestruct.enabled) {
            this.addButton(this.button = new Button(mainWindow.getScaledWidth() - 152, mainWindow.getScaledHeight() - 35, 150, 20, new StringTextComponent("������ ����������: " + hide), (button) -> {
                hide = !hide;
            }));
        } else if (selfDestruct.enabled) {
            hide = false;
        }

        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.sentHistoryCursor = this.minecraft.ingameGUI.getChatGUI().getSentMessages().size();
        this.inputField = new TextFieldWidget(this.font, 4, this.height - 12, this.width - 4, 12, new TranslationTextComponent("chat.editBox")) {
            @Override
            protected IFormattableTextComponent getNarrationMessage() {
                return super.getNarrationMessage().appendString(ChatScreen.this.commandSuggestionHelper.getSuggestionMessage());
            }
        };        this.inputField.setMaxStringLength(256);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setText(this.defaultInputFieldText);
        this.inputField.setResponder(this::func_212997_a);
        this.children.add(this.inputField);
        this.commandSuggestionHelper = new CommandSuggestionHelper(this.minecraft, this, this.inputField, this.font, false, false, 1, 10, true, -805306368);
        this.commandSuggestionHelper.init();
        this.setFocusedDefault(this.inputField);
    }

    public void resize(Minecraft minecraft, int width, int height) {
        String s = this.inputField.getText();
        this.init(minecraft, width, height);
        this.setChatLine(s);
        this.commandSuggestionHelper.init();
    }

    public void onClose() {
        this.minecraft.keyboardListener.enableRepeatEvents(false);
        this.minecraft.ingameGUI.getChatGUI().resetScroll();
        Iterator var1 = DragManager.draggables.values().iterator();

        while(var1.hasNext()) {
            Dragging dragging = (Dragging)var1.next();
            if (dragging.getModule().isState()) {
                dragging.onRelease(0);
            }
        }

    }

    public void tick() {
        this.inputField.tick();
    }

    private void func_212997_a(String p_212997_1_) {
        String s = this.inputField.getText();
        this.commandSuggestionHelper.shouldAutoSuggest(!s.equals(this.defaultInputFieldText));
        this.commandSuggestionHelper.init();
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.commandSuggestionHelper.onKeyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == 256) {
            this.minecraft.displayGuiScreen((Screen)null);
            return true;
        } else if (keyCode != 257 && keyCode != 335) {
            if (keyCode == 265) {
                this.getSentHistory(-1);
                return true;
            } else if (keyCode == 264) {
                this.getSentHistory(1);
                return true;
            } else if (keyCode == 266) {
                this.minecraft.ingameGUI.getChatGUI().addScrollPos((double)(this.minecraft.ingameGUI.getChatGUI().getLineCount() - 1));
                return true;
            } else if (keyCode == 267) {
                this.minecraft.ingameGUI.getChatGUI().addScrollPos((double)(-this.minecraft.ingameGUI.getChatGUI().getLineCount() + 1));
                return true;
            } else {
                return false;
            }
        } else {
            String s = this.inputField.getText().trim();
            if (!s.isEmpty()) {
                this.sendMessage(s);
            }

            this.minecraft.displayGuiScreen((Screen)null);
            return true;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 1.0) {
            delta = 1.0;
        }

        if (delta < -1.0) {
            delta = -1.0;
        }

        if (this.commandSuggestionHelper.onScroll(delta)) {
            return true;
        } else {
            if (!hasShiftDown()) {
                delta *= 7.0;
            }

            this.minecraft.ingameGUI.getChatGUI().addScrollPos(delta);
            return true;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.commandSuggestionHelper.onClick((double)((int)mouseX), (double)((int)mouseY), button)) {
            return true;
        } else {
            if (button == 0) {
                NewChatGui newchatgui = this.minecraft.ingameGUI.getChatGUI();
                if (newchatgui.func_238491_a_(mouseX, mouseY)) {
                    return true;
                }

                Style style = newchatgui.func_238494_b_(mouseX, mouseY);
                if (style != null && this.handleComponentClicked(style)) {
                    return true;
                }
            }

            Iterator var8 = DragManager.draggables.values().iterator();

            while(var8.hasNext()) {
                Dragging dragging = (Dragging)var8.next();
                if (dragging.getModule().isState() && dragging.onClick(mouseX, mouseY, button)) {
                    break;
                }
            }

            return this.inputField.mouseClicked(mouseX, mouseY, button) ? true : super.mouseClicked(mouseX, mouseY, button);
        }
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Iterator var6 = DragManager.draggables.values().iterator();

        while(var6.hasNext()) {
            Dragging dragging = (Dragging)var6.next();
            if (dragging.getModule().isState()) {
                dragging.onRelease(button);
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    protected void insertText(String text, boolean overwrite) {
        if (overwrite) {
            this.inputField.setText(text);
        } else {
            this.inputField.writeText(text);
        }

    }

    public void getSentHistory(int msgPos) {
        int i = this.sentHistoryCursor + msgPos;
        int j = this.minecraft.ingameGUI.getChatGUI().getSentMessages().size();
        i = MathHelper.clamp(i, 0, j);
        if (i != this.sentHistoryCursor) {
            if (i == j) {
                this.sentHistoryCursor = j;
                this.inputField.setText(this.historyBuffer);
            } else {
                if (this.sentHistoryCursor == j) {
                    this.historyBuffer = this.inputField.getText();
                }

                this.inputField.setText((String)this.minecraft.ingameGUI.getChatGUI().getSentMessages().get(i));
                this.commandSuggestionHelper.shouldAutoSuggest(false);
                this.sentHistoryCursor = i;
            }
        }

    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        UnHook selfDestruct = Main.getInstance().getFunctionRegistry().getUnHook();
        if (this.button != null && !selfDestruct.enabled) {
            this.button.setMessage(new StringTextComponent("������ ����������: " + hide));
        }
        this.setListener(this.inputField);
        this.inputField.setFocused2(true);
        fill(matrixStack, 2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.gameSettings.getChatBackgroundColor(Integer.MIN_VALUE));
        this.inputField.render(matrixStack, mouseX, mouseY, partialTicks);
        this.commandSuggestionHelper.drawSuggestionList(matrixStack, mouseX, mouseY);
        Style style = this.minecraft.ingameGUI.getChatGUI().func_238494_b_((double)mouseX, (double)mouseY);
        if (style != null && style.getHoverEvent() != null) {
            this.renderComponentHoverEffect(matrixStack, style, mouseX, mouseY);
        }

        AtomicBoolean anyHovered = new AtomicBoolean(false);
        DragManager.draggables.values().forEach((dragging) -> {
            if (dragging.getModule().isState()) {
                if (MathUtil.isInRegion((float)mouseX, (float)mouseY, dragging.getX(), dragging.getY(), dragging.getWidth(), dragging.getHeight())) {
                    anyHovered.set(true);
                }

                dragging.onDraw(mouseX, mouseY, Minecraft.getInstance().getMainWindow());
            }

        });
        DragManager.save();

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public boolean isPauseScreen() {
        return false;
    }

    private void setChatLine(String p_208604_1_) {
        this.inputField.setText(p_208604_1_);
    }
}
