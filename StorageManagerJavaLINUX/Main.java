// CSCE 608 Database

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;


public class Main{
    
    public static void main(String[] args){
        
        Core core = new Core();
        
        while (true){
            Scanner scanner = new Scanner(System.in);
            System.out.println("*********TinySQL RDMS INTERPRETER BY HENRY QUAN, VISHAKH SHUKLA, XIEN THOMAS*********");
            System.out.println("Read from file? (Yes/No) or enter 'exit' to exit TinySQL:");
            String answer = scanner.nextLine();
            if (answer.equals("Yes") || answer.equals("yes")){
                try{
                    File input = new File("./test/TinySQL-TextLINUX.txt");
            
                    Scanner scan = new Scanner(new FileInputStream(input));
                    long t0 = System.currentTimeMillis();

                    while (scan.hasNextLine()) {
                        core.core(scan.nextLine());
                        // System.out.println(scan.nextLine());
                    }
                    
                    long t1 = System.currentTimeMillis();
                    long seconds = (t1 - t0)/1000;
                    System.out.println("Time taken for execution: " + seconds + " seconds");
                    
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                
            } else if (answer.equals("No") || answer.equals("no")) {
                 System.out.println("Enter TinySQL Statement or enter 'exit' to exit TinySQL:");
                 String statement = scanner.nextLine();
                 long t0 = System.currentTimeMillis();
                 core.core(statement);
                 long t1 = System.currentTimeMillis();
                 long seconds = (t1 - t0)/1000;
                 System.out.println("Time taken for execution: " + seconds + " seconds");
                
            } else if (answer.equals("exit") || answer.equals("Exit")){
                break; 
                
            }
            
        }
        
    }
}