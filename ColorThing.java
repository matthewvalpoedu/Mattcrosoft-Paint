package com.example.paint;

import javafx.scene.paint.Color;

/** Class that attaches a color label to a color based on its hashcode */
public class ColorThing {
    /** Determines the hexcode of the color in the color picker */
    public static String colorToHex(Color color) { //converts a color value to a hexcode
        String hex1;
        String hex2;
        hex1 = Integer.toHexString(color.hashCode()).toUpperCase();
        hex2 = switch (hex1.length()) {
            case 2 -> "000000";
            case 3 -> String.format("00000%s", hex1.charAt(0));
            case 4 -> String.format("0000%s", hex1.substring(0, 2));
            case 5 -> String.format("000%s", hex1.substring(0, 3));
            case 6 -> String.format("00%s", hex1.substring(0, 4));
            case 7 -> String.format("0%s", hex1.substring(0, 5));
            default -> hex1.substring(0, 6);
        };
        return hex2;
    }
}
