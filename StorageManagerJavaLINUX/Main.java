// CSCE 608 Database
/*
* RQ, XT, VS
*/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class Main{
    
    public static void main(String[] args){
        
        Core core = new Core();
        
        // User interface
        while (true){
            Scanner scanner = new Scanner(System.in);
            System.out.println("*********TinySQL RDMS INTERPRETER BY HENRY QUAN, VISHAKH SHUKLA, XIEN THOMAS*********");
            System.out.println("Read from file? (Yes/No) or enter 'exit' to exit TinySQL:");
            String answer = scanner.nextLine();
            if (answer.equals("Yes") || answer.equals("yes")){
                try{

                    // Look for the test file located in the test folder
           
                    System.out.println("Enter filename (enter TinySQL-TextLINUX for default file) (without .txt):");
                    String answer2 = scanner.nextLine();
                    if (answer2.equals("exit") || answer2.equals("Exit")){
                        break;
                    }
                    
                    
                    File input = new File("./test/" + answer2 + ".txt");
            
                    Scanner scan = new Scanner(new FileInputStream(input));
                    long t0 = System.currentTimeMillis();
                    
                    try{
                        PrintWriter writer = new PrintWriter("sql_output.txt");
                        writer.print("");
                        writer.close();
                    }
            		catch(FileNotFoundException ex){
            			System.out.println("Output file not found!");
            		}
                    while (scan.hasNextLine()) {
                        //Read in each statement and send it to be parsed in the Core.
                        String s = scan.nextLine();
                        System.out.println(s);
                        core.core(s);
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
                 if (statement.equals("exit") || statement.equals("Exit")){
                    break; 
                 }
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
