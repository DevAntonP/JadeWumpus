package org.example;

import aima.core.environment.wumpusworld.WumpusPercept;


public class WumpusPerceptExtended extends WumpusPercept {

    public static WumpusPerceptExtended fromString(String perceptString) {
        WumpusPerceptExtended customPercept = new WumpusPerceptExtended();

        // Parse the input string and set the corresponding perceptual information
        String[] percepts = perceptString.replace("{", "").replace("}", "").split(", ");
        for (String percept : percepts) {
            switch (percept.trim()) {
                case "Stench":
                    customPercept.setStench();
                    break;
                case "Breeze":
                    customPercept.setBreeze();
                    break;
                case "Glitter":
                    customPercept.setGlitter();
                    break;
                case "Bump":
                    customPercept.setBump();
                    break;
                case "Scream":
                    customPercept.setScream();
                    break;
            }
        }

        return customPercept;
    }
}