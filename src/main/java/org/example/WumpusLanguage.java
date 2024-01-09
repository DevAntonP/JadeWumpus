package org.example;

import aima.core.environment.wumpusworld.WumpusAction;
import aima.core.environment.wumpusworld.WumpusPercept;

public class WumpusLanguage {

    public static String Language = "English";
    public static String Ontology = "WumpusWorld";

    public static String CreatePerceptMessage(WumpusPercept percept) {
        // Create a human-like sentence based on the percept
        StringBuilder message = new StringBuilder();
        if (percept.isStench()) message.append("a stench, ");
        if (percept.isBreeze()) message.append("a breeze, ");
        if (percept.isGlitter()) message.append("a glitter, ");
        if (percept.isBump()) message.append("a bump, ");
        if (percept.isScream()) message.append("a scream, ");

        // Remove trailing comma and space
        if (message.length() > 0) {
            message.setLength(message.length() - 2);
            message.insert(0, "I perceive ");
            return message.toString();
        } else {
            return "It's safe and clear";
        }
    }

    public static WumpusPercept ParsePerceptMessage(String message) {
        // Convert the received message to WumpusPercept
        WumpusPercept percept = new WumpusPercept();

        if (message.contains("stench")) percept.setStench();
        if (message.contains("breeze")) percept.setBreeze();
        if (message.contains("glitter")) percept.setGlitter();
        if (message.contains("bump")) percept.setBump();
        if (message.contains("scream")) percept.setScream();

        return percept;
    }

    public static String CreateActionMessage(WumpusAction action) {
        // Create a human-like sentence based on the action
        return switch (action) {
            case FORWARD -> "Let's move forward and explore!";
            case TURN_LEFT -> "I'm thinking of turning left for a change.";
            case TURN_RIGHT -> "How about turning right this time?";
            case GRAB -> "You should grab the gold now.";
            case SHOOT -> "Time to take a shot and see what happens!";
            case CLIMB -> "You are ready to climb!";
        };
    }

    public static WumpusAction ParseActionMessage(String message) {
        // Convert the received message to WumpusAction
        String actionStr = message.toLowerCase();

        // Use contains to check for keywords in the action string
        if (actionStr.contains("forward")) {
            return WumpusAction.FORWARD;
        } else if (actionStr.contains("left")) {
            return WumpusAction.TURN_LEFT;
        } else if (actionStr.contains("right")) {
            return WumpusAction.TURN_RIGHT;
        } else if (actionStr.contains("grab")) {
            return WumpusAction.GRAB;
        } else if (actionStr.contains("shot")) {
            return WumpusAction.SHOOT;
        } else if (actionStr.contains("climb")) {
            return WumpusAction.CLIMB;
        } else {
            System.out.println("Unknown action - " + actionStr);
            throw new IllegalArgumentException(message);
        }
    }
}
