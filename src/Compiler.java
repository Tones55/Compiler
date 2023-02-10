package src;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Compiler {
    public static void main(String[] args) {

        System.out.println(); // for readability in output console

        boolean verbose = false; 
        String output;
        ArrayList<ArrayList<String>> programs = new ArrayList<ArrayList<String>>(); // used to store the programs from the input file
        programs.add(new ArrayList<String>()); // add an empty arraylist to the arraylist of programs (this is so that the first program is stored in the first index of the arraylist)
        //PatternMatcher patternMatcher = new PatternMatcher();
        int programNumber = 0;
        String input = "";

        // Create a new scanner object to read from the file
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("src\\IO\\input.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //read from the file
        while (scanner.hasNextLine()) {
            input = scanner.nextLine();
            input = input.replaceAll("/\\*.*?\\*/", "");
            input = input.replaceAll("\\s", "☺"); //replace all spaces with ☺
            programs.get(programNumber).add(input);
            if (input.contains("$")) {
                if(scanner.hasNextLine()){
                programs.add(new ArrayList<String>());
                programNumber++;
                }
            }
        }

        //if verbose print out the programs in input file
        if(verbose){
            for (int i = 0; i < programs.size(); i++) {
                System.out.println("Program " + (i+1) + ": ");
                printArrayList(programs.get(i));
            }
        }

        //compile each program one at a time
        for (int i = 0; i < programs.size(); i++) {
            output = CodeGeneration.doCodeGeneration(SemanticAnalysis.doSemanticAnalysis(Parser.doParse(Lexer.doLex(programs.get(i)))));
            if (verbose) {
            System.out.println("Program " + i + ": " + output);
            }
        }

    }

    private static void printArrayList(ArrayList<String> list){
        //used to pint an arraylist line by line
        for(int i=0; i<list.size(); i++){
            System.out.println(list.get(i));
        }
    }

}