package src;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Compiler {
    public static void main(String[] args) {

        System.out.println(); // for readability in output console

        String[] programs; // used to store the programs from the input file

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

        System.out.println("Raw input: " + input);
        System.out.println();

        //print programs
        for (int i = 0; i < programs.length; i++) {
            System.out.println("Program " + i + ": " + programs[i]);
        }
    }
}