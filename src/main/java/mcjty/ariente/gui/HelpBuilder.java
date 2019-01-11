package mcjty.ariente.gui;

import java.util.ArrayList;
import java.util.List;

public class HelpBuilder {

    private final List<HelpLine> lines = new ArrayList<>();

    private HelpBuilder() {
    }

    public static HelpBuilder create() {
        return new HelpBuilder();
    }

    public HelpBuilder line(String text) {
        lines.add(new HelpLine(text, 0xff000000));
        return this;
    }

    public HelpBuilder line(String text, int color) {
        lines.add(new HelpLine(text, color));
        return this;
    }

    public HelpBuilder nl() {
        lines.add(new HelpLine("", 0xff000000));
        return this;
    }

    public List<HelpLine> getLines() {
        return lines;
    }

    public static class HelpLine {
        private final String text;
        private final int color;

        public HelpLine(String text, int color) {
            this.text = text;
            this.color = color;
        }

        public String getText() {
            return text;
        }

        public int getColor() {
            return color;
        }
    }
}
