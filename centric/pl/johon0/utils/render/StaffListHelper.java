package centric.pl.johon0.utils.render;
import lombok.*;
import lombok.experimental.FieldDefaults;

import net.minecraft.util.text.ITextComponent;

import java.util.*;
import java.util.regex.Pattern;


@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class StaffListHelper {



    private Set<StaffData> staffPlayers = new LinkedHashSet<>();
    private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
    private final Pattern prefixMatches = Pattern.compile(".*(mod|der|adm|help|wne|хелп|адм|поддержка|кура|own|taf|curat|dev|supp|yt|сотруд).*");

    float width;
    float height;


    @AllArgsConstructor
    @Data
    public static class StaffData {
        ITextComponent prefix;
        String name;
        Status status;

        public enum Status {
            NONE("", -1),
            VANISHED("H", ColorUtils.rgb(254, 68, 68));
            public final String string;
            public final int color;

            Status(String string, int color) {
                this.string = string;
                this.color = color;
            }
        }

        @Override
        public String toString() {
            return prefix.getString();
        }
    }


    private int getPriority(StaffData staffData) {
        return switch (staffData.toString()) {
            case "admin", "админ" -> 0;
            case "ml.admin" -> 1;
            case "gl.moder" -> 2;
            case "st.moder", "s.moder" -> 3;
            case "moder", "модератор", "куратор" -> 4;
            case "j.moder" -> 5;
            case "st.helper" -> 6;
            case "helper+" -> 7;
            case "helper" -> 8;
            case "yt+" -> 9;
            case "yt" -> 10;
            default -> 11;
        };
    }

}
