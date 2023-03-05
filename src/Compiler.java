import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import java.time.LocalDateTime;

public class Compiler {

    public static int fileLine = 0;
    public static void main(String[] args) {

        System.out.println(); // for readability in output console

        boolean verbose = false; 
        String output;
        ArrayList<ArrayList<String>> programs = new ArrayList<ArrayList<String>>(); 
        programs.add(new ArrayList<String>()); 
        int programNumber = 0;
        String input = "";
        int lineNumber = 0;

        // Create a new scanner object to read from the file
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(args[0]));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //read from the file
        while (scanner.hasNextLine()) {
            lineNumber++;
            input = scanner.nextLine();
            if (input.contains("/*")) {
                if (input.contains("*/")) {
                    //replace everything between /* and */ with nothing
                    input = input.replaceAll("/\\*.*?\\*/", "");
                }
                else {
                    //replace everything after /* with nothing and give warning
                    input = input.replaceAll("/\\*.*", "");
                    System.out.println("Input Line: " + lineNumber + " :: Lex Warning: Comment not closed");
                }
            }
            // go to next list if a $ is found
            programs.get(programNumber).add(input);
            if (input.contains("$")) {
                if(scanner.hasNextLine()){
                programs.add(new ArrayList<String>());
                programNumber++;
                }
            }
        }
        // give warning if no $ at end of file
        if (!input.contains("$")) {
            System.out.println("Input Line: " + lineNumber + " :: Lex Warning: Missing \"$\" at the end of final program");
        }

        //if verbose print out the programs in input file
        if(verbose){
            for (int i = 0; i < programs.size(); i++) {
                System.out.println("Program " + (i+1) + ": ");
                printArrayList(programs.get(i));
            }
        }

        //compile each program one at a time
        System.out.println(); //output formatting
        for (int i = 0; i < programs.size(); i++) {
            fileLine++;
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