package centric.pl.ui.mainmenu.altmanager;

import centric.pl.johon0.utils.client.IMinecraft;
import com.google.gson.*;
import centric.pl.Main;
import net.minecraft.util.Session;

import java.io.*;
import java.util.UUID;

public class AltConfig implements IMinecraft {

    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final File file = new File(mc.gameDir, "\\jre\\files\\alts.cfg");

    public void init() throws Exception {
        if (!file.exists()) {
            file.createNewFile();
        } else {
            readAlts();
        }
    }

    public static void updateFile() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("last", mc.session.getUsername());

        JsonArray altsArray = new JsonArray();
        for (Alt alt : Main.getInstance().getAltWidget().alts) {
            altsArray.add(alt.name);
        }

        jsonObject.add("alts", altsArray);

        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.println(gson.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAlts() throws FileNotFoundException {
        JsonElement jsonElement = new JsonParser().parse(new BufferedReader(new FileReader(file)));

        if (jsonElement.isJsonNull()) return;

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (jsonObject.has("last")) {
            mc.session = new Session(jsonObject.get("last").getAsString(), UUID.randomUUID().toString(), "", "mojang");
        }

        if (jsonObject.has("alts")) {
            for (JsonElement element : jsonObject.get("alts").getAsJsonArray()) {
                String name = element.getAsString();

                Main.getInstance().getAltWidget().alts.add(new Alt(name));
            }
        }
    }
}
