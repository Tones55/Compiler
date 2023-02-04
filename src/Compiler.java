package src;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Compiler {

    private static boolean verbose = false;
    public static void main(String[] args) {

        System.out.println(); // for readability in output console

        String[] programs; // used to store the programs from the input file
        PatternMatcher patternMatcher = new PatternMatcher();

        // Create a new scanner object to read from the file
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("src\\IO\\input.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //read from the file
        String input = "";
        while (scanner.hasNextLine()) {
            input += scanner.nextLine();
        }

        //split input by $ and store in programs array but keep the $ in the array
        programs = input.split("(?<=\\$)");

        //remove comments from programs
        for (int i = 0; i < programs.length; i++) {
            programs[i] = programs[i].replaceAll("(?s)/\\*.*?\\*/", "");
        }

        if(verbose){
            System.out.println("Raw input: " + input);
            System.out.println();
        
            for (int i = 0; i < programs.length; i++) {
                System.out.println("Program " + i + ": " + programs[i]);
            }
        }

        
        for (int i = 0; i < programs.length; i++) {
            new CodeGeneration(new SemanticAnalysis(new Parser(new Lexer(programs[i] , patternMatcher))));
        }

    }
}