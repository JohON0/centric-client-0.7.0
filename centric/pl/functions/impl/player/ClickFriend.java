package centric.pl.functions.impl.player;

import centric.pl.command.friends.FriendStorage;
import centric.pl.events.impl.EventKey;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.BindSetting;
import centric.pl.johon0.utils.client.IMinecraft;
import centric.pl.johon0.utils.player.PlayerUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.player.PlayerEntity;

@FunctionRegister(name = "ClickFriend", type = Category.Player, beta = false)
public class ClickFriend extends Function {
    final BindSetting throwKey = new BindSetting("Кнопка", -98);
    public ClickFriend() {
        addSettings(throwKey);
    }
    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == throwKey.get() && IMinecraft.mc.pointedEntity instanceof PlayerEntity) {

            if (IMinecraft.mc.player == null || IMinecraft.mc.pointedEntity == null) {
                return;
            }

            String playerName = IMinecraft.mc.pointedEntity.getName().getString();

            if (!PlayerUtils.isNameValid(playerName)) {
                print("Невозможно добавить бота в друзья, увы, как бы вам не хотелось это сделать");
                return;
            }

            if (FriendStorage.isFriend(playerName)) {
                FriendStorage.remove(playerName);
                printStatus(playerName, true);
            } else {
                FriendStorage.add(playerName);
                printStatus(playerName, false);
            }
        }
    }

    void printStatus(String name, boolean remove) {
        if (remove) print(name + " удалён из друзей");
        else print(name + " добавлен в друзья");
    }
}
