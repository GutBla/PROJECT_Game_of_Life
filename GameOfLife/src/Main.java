

import java.util.Random;

public class Main {

    private static final String RESET = "\033[0m";
    private static final String RED = "\033[0;31m";
    private static final String GREEN = "\033[0;32m";
    private static final String YELLOW = "\033[0;33m";
    private static final String BLUE = "\033[0;34m";
    private static final String MAGENTA = "\033[0;35m";
    private static final String CYAN = "\033[0;36m";
    private static final String ORANGE = "\033[38;5;208m";

    private static final int NOT_PRESENT = -1;
    private static final int INVALID = -2;

    private static int widthGrid = NOT_PRESENT;
    private static int heightGrid = NOT_PRESENT;
    private static int generationsTotal = NOT_PRESENT;
    private static int speedForGenerations = NOT_PRESENT;
    private static String populationInitial = "";
    private static int neighborhoodType = 3;

    private static final String MESSAGE_INVALID = "Invalid";
    private static final String MESSAGE_NOT_PRESENT = "Not present";

    public static void main(String[] args) {
        if (args.length == 0) {
            displayCurrentConfiguration();
            terminateProgram();
        } else {
            processArgumentsAndStartGame(args);
        }
    }

    private static void processArgumentsAndStartGame(String[] args) {
        parseCommandLineArguments(args);
        boolean hasInvalidArguments = validateCommandLineArguments();
        boolean populationValid = validatePopulation();

        displayCurrentConfiguration();

        if (hasInvalidArguments || !populationValid) {
            terminateProgram();
        } else {
            printTitle();
            runGameOfLife();
        }
    }

    private static void parseCommandLineArguments(String[] commandLineArguments) {
        for (String argument : commandLineArguments) {
            String[] argumentParts = argument.split("=");
            if (argumentParts.length == 2) {
                assignConfigurationValue(argumentParts[0], argumentParts[1]);
            } else {
                assignInvalidConfigurationValue(argumentParts[0]);
            }
        }
    }

    private static void assignConfigurationValue(String key, String value) {
        switch (key) {
            case "w": widthGrid = parseGridWidth(value); break;
            case "h": heightGrid = parseGridHeight(value); break;
            case "g": generationsTotal = parseTotalGenerations(value); break;
            case "s": speedForGenerations = parseGenerationDelay(value); break;
            case "p": populationInitial = parseInitialPopulation(value); break;
            case "n": neighborhoodType = parseNeighborhoodMode(value); break;
        }
    }

    private static void assignInvalidConfigurationValue(String key) {
        switch (key) {
            case "w": widthGrid = INVALID; break;
            case "h": heightGrid = INVALID; break;
            case "g": generationsTotal = INVALID; break;
            case "s": speedForGenerations = INVALID; break;
            case "p": populationInitial = MESSAGE_INVALID; break;
            case "n": neighborhoodType = INVALID; break;
        }
    }

    private static boolean validateCommandLineArguments() {
        return widthGrid == INVALID || heightGrid == INVALID || generationsTotal == INVALID ||
                speedForGenerations == INVALID || populationInitial.equals(MESSAGE_INVALID) || neighborhoodType == INVALID;
    }

    private static boolean validatePopulation() {
        return validatePopulationHeight() && validatePopulationWidth();
    }

    private static boolean validatePopulationHeight() {
        String[] populationRows = populationInitial.split("#");
        if (populationRows.length > heightGrid) {
            populationInitial = MESSAGE_INVALID;
            return false;
        }
        return true;
    }

    private static boolean validatePopulationWidth() {
        String[] populationRows = populationInitial.split("#");
        for (String row : populationRows) {
            if (row.length() > widthGrid) {
                populationInitial = MESSAGE_INVALID;
                return false;
            }
        }
        return true;
    }


    private static int parseGridWidth(String widthValue) {
        return parseIntegerInRange(widthValue, new int[]{10, 20, 40, 80});
    }

    private static int parseGridHeight(String heightValue) {
        return parseIntegerInRange(heightValue, new int[]{10, 20, 40});
    }

    private static int parseTotalGenerations(String generationsValue) {
        return parsePositiveInteger(generationsValue);
    }

    private static int parseGenerationDelay(String delayValue) {
        return parseIntegerInRange(delayValue, 250, 1000);
    }

    private static String parseInitialPopulation(String populationValue) {
        return populationValue.equals("rnd") || populationValue.matches("[01#]+") ? populationValue : MESSAGE_INVALID;
    }

    private static int parseNeighborhoodMode(String modeValue) {
        return parseIntegerInRange(modeValue, 1, 5);
    }

    private static int parseIntegerInRange(String value, int[] validValues) {
        try {
            int parsedValue = Integer.parseInt(value);
            for (int validValue : validValues) {
                if (parsedValue == validValue) return parsedValue;
            }
        } catch (NumberFormatException ignored) {}
        return Main.INVALID;
    }

    private static int parseIntegerInRange(String value, int min, int max) {
        try {
            int parsedValue = Integer.parseInt(value);
            if (parsedValue >= min && parsedValue <= max) return parsedValue;
        } catch (NumberFormatException ignored) {}
        return Main.INVALID;
    }

    private static int parsePositiveInteger(String value) {
        try {
            int parsedValue = Integer.parseInt(value);
            if (parsedValue >= 0) return parsedValue;
        } catch (NumberFormatException ignored) {}
        return Main.INVALID;
    }

    private static void displayCurrentConfiguration() {
        System.out.println(RED + "Width : [" + formatValue(widthGrid) + "]" + RESET);
        System.out.println(GREEN + "Height : [" + formatValue(heightGrid) + "]" + RESET);
        System.out.println(YELLOW + "Generations : [" + formatValue(generationsTotal) + "]" + RESET);
        System.out.println(BLUE + "Speed : [" + formatValue(speedForGenerations) + "]" + RESET);
        System.out.println(MAGENTA + "Population : " + (populationInitial.isEmpty() ? MESSAGE_NOT_PRESENT : formatPopulation(populationInitial)) + RESET);
        System.out.println(CYAN + "Neighborhood: [" + getNeighborhoodModeDescription() + "]" + RESET);
    }

    private static String formatValue(int value) {
        return value == NOT_PRESENT ? MESSAGE_NOT_PRESENT : (value == INVALID ? MESSAGE_INVALID : String.valueOf(value));
    }

    private static String formatPopulation(String population) {
        return population.equals(MESSAGE_INVALID) ? MESSAGE_INVALID : population;
    }

    private static String getNeighborhoodModeDescription() {
        return switch (neighborhoodType) {
            case 1 -> "Von Neumann";
            case 2 -> "Hexagonal Diagonal";
            case 3 -> "Moore";
            case 4 -> "Reverse Von Neumann";
            case 5 -> "Parallels";
            default -> MESSAGE_INVALID;
        };
    }

    private static int[][] getNeighborhoodOffsetsForMode() {
        return switch (neighborhoodType) {
            case 1 -> new int[][]{{-1, 0}, {0, -1}, {0, 1}, {1, 0}};
            case 2 -> new int[][]{{-1, -1}, {-1, 0}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};
            case 3 -> new int[][]{{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
            case 4 -> new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
            case 5 -> new int[][]{{-1, -1}, {-1, 0}, {-1, 1}, {1, -1}, {1, 0}, {1, 1}};
            default -> new int[0][0];
        };
    }

    private static int[][] initializePopulationMatrix() {
        if (populationInitial.equals("rnd")) {
            return generateRandomPopulationMatrix();
        } else if (!populationInitial.equals(MESSAGE_INVALID)) {
            return parsePopulationMatrixFromString();
        }
        return null;
    }

    private static int[][] generateRandomPopulationMatrix() {
        Random randomGenerator = new Random();
        int[][] populationMatrix = new int[heightGrid][widthGrid];
        for (int rowIndex = 0; rowIndex < heightGrid; rowIndex++) {
            for (int columnIndex = 0; columnIndex < widthGrid; columnIndex++) {
                populationMatrix[rowIndex][columnIndex] = randomGenerator.nextInt(2);
            }
        }
        return populationMatrix;
    }

    private static int[][] parsePopulationMatrixFromString() {
        String[] populationRows = populationInitial.split("#");
        heightGrid = Math.max(heightGrid, populationRows.length);
        widthGrid = Math.max(widthGrid, populationRows[0].length());

        int[][] populationMatrix = new int[heightGrid][widthGrid];

        for (int rowIndex = 0; rowIndex < populationRows.length; rowIndex++) {
            String row = populationRows[rowIndex];
            for (int columnIndex = 0; columnIndex < row.length(); columnIndex++) {
                populationMatrix[rowIndex][columnIndex] = Character.getNumericValue(row.charAt(columnIndex));
            }
        }

        return populationMatrix;
    }

    private static void runGameOfLife() {
        try {
            int[][] initialPopulationMatrix = initializePopulationMatrix();
            if (initialPopulationMatrix == null) {
                terminateProgram();
            } else {
                executeGameOfLifeSimulation(initialPopulationMatrix);
            }
        } catch (Exception e) {
            System.err.println("Error during simulation: " + e.getMessage());
            terminateProgram();
        }
    }

    private static void executeGameOfLifeSimulation(int[][] populationMatrix) {
        int[][] currentGenerationMatrix = populationMatrix;
        int generationIndex = 0;

        while (generationIndex < generationsTotal || generationsTotal == 0) {
            System.out.println("Generation : " + generationIndex );
            displayFormattedMatrix(currentGenerationMatrix);
            currentGenerationMatrix = computeNextGenerationMatrix(currentGenerationMatrix);
            pauseForDelay();
            generationIndex++;
        }
    }

    private static int[][] computeNextGenerationMatrix(int[][] currentGenerationMatrix) {
        int[][] nextGenerationMatrix = new int[heightGrid][widthGrid];

        for (int rowIndex = 0; rowIndex < heightGrid; rowIndex++) {
            for (int columnIndex = 0; columnIndex < widthGrid; columnIndex++) {
                int liveNeighborCount = countLiveNeighborsInMatrix(currentGenerationMatrix, rowIndex, columnIndex);
                nextGenerationMatrix[rowIndex][columnIndex] = (currentGenerationMatrix[rowIndex][columnIndex] == 1)
                        ? determineNextStateForAliveCell(liveNeighborCount)
                        : determineNextStateForDeadCell(liveNeighborCount);
            }
        }

        return nextGenerationMatrix;
    }

    private static int countLiveNeighborsInMatrix(int[][] matrix, int rowIndex, int columnIndex) {
        int liveNeighborsCount = 0;
        int[][] neighborhoodOffsets = getNeighborhoodOffsetsForMode();

        for (int[] offset : neighborhoodOffsets) {
            int neighborRowIndex = rowIndex + offset[0];
            int neighborColumnIndex = columnIndex + offset[1];
            if (neighborRowIndex >= 0 && neighborRowIndex < heightGrid && neighborColumnIndex >= 0 && neighborColumnIndex < widthGrid) {
                liveNeighborsCount += matrix[neighborRowIndex][neighborColumnIndex];
            }
        }

        return liveNeighborsCount;
    }

    private static int determineNextStateForAliveCell(int liveNeighborCount) {
        return (liveNeighborCount < 2 || liveNeighborCount > 3) ? 0 : 1;
    }

    private static int determineNextStateForDeadCell(int liveNeighborCount) {
        return (liveNeighborCount == 3) ? 1 : 0;
    }

    private static void printTitle() {
        System.out.println(ORANGE + "☐ ⏹ ☐ ===================================== ⏹ ☐ ⏹" + RESET);
        System.out.println(ORANGE + "☐ ☐ ⏹ *       GAME OF LIFE DE CONWAY      * ☐ ⏹ ⏹" + RESET);
        System.out.println(ORANGE + "⏹ ⏹ ⏹ ===================================== ☐ ⏹ ☐" + RESET);
        System.out.println();
    }

    private static void displayFormattedMatrix(int[][] matrixDisplay) {
        for (int[] row : matrixDisplay) {
            for (int cell : row) {
                if (cell == 1) {
                    System.out.print(GREEN + "⏹ " + RESET);
                } else {
                    System.out.print(RED + "☐ " + RESET);
                }
            }
            System.out.println();
        }
    }

    private static void pauseForDelay() {
        try {
            Thread.sleep(speedForGenerations);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void terminateProgram() {
        System.exit(0);
    }
}
