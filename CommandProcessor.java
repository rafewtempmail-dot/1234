package com.offline.assistant.command;

import java.util.Locale;

public class CommandProcessor {

    public enum CommandType {
        OPEN_CAMERA,
        FLASHLIGHT_ON,
        FLASHLIGHT_OFF,
        OPEN_WHATSAPP,
        CALL_CONTACT,
        OPEN_GALLERY,
        BLUETOOTH_ON,
        BLUETOOTH_OFF,
        WIFI_ON,
        WIFI_OFF,
        UNKNOWN
    }

    public static class CommandResult {
        public CommandType type;
        public String payload; // Holds details like contact name or metadata

        public CommandResult(CommandType type, String payload) {
            this.type = type;
            this.payload = payload;
        }
    }

    public CommandResult process(String rawInput) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return new CommandResult(CommandType.UNKNOWN, null);
        }

        String input = rawInput.toLowerCase(Locale.ROOT).trim();

        if (input.contains("open camera")) {
            return new CommandResult(CommandType.OPEN_CAMERA, null);
        } else if (input.contains("turn on flashlight") || input.contains("flashlight on")) {
            return new CommandResult(CommandType.FLASHLIGHT_ON, null);
        } else if (input.contains("turn off flashlight") || input.contains("flashlight off")) {
            return new CommandResult(CommandType.FLASHLIGHT_OFF, null);
        } else if (input.contains("open whatsapp")) {
            return new CommandResult(CommandType.OPEN_WHATSAPP, null);
        } else if (input.contains("open gallery") || input.contains("open photos")) {
            return new CommandResult(CommandType.OPEN_GALLERY, null);
        } else if (input.contains("turn on bluetooth") || input.contains("bluetooth on")) {
            return new CommandResult(CommandType.BLUETOOTH_ON, null);
        } else if (input.contains("turn off bluetooth") || input.contains("bluetooth off")) {
            return new CommandResult(CommandType.BLUETOOTH_OFF, null);
        } else if (input.contains("turn on wifi") || input.contains("wifi on")) {
            return new CommandResult(CommandType.WIFI_ON, null);
        } else if (input.contains("turn off wifi") || input.contains("wifi off")) {
            return new CommandResult(CommandType.WIFI_OFF, null);
        } else if (input.startsWith("call")) {
            // Extracts the string following the command token "call "
            String contactName = extractPayload(rawInput, "call");
            return new CommandResult(CommandType.CALL_CONTACT, contactName);
        }

        return new CommandResult(CommandType.UNKNOWN, null);
    }

    private String extractPayload(String standardInput, String commandKeyword) {
        if (standardInput.toLowerCase(Locale.ROOT).startsWith(commandKeyword)) {
            return standardInput.substring(commandKeyword.length()).trim();
        }
        return standardInput.trim();
    }
}
