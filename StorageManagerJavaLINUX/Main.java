// CSCE 608 Database

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;


public class Main{
    
    public static void main(String[] args){
        
        long t0 = System.currentTimeMillis();
        
        try{
            // Core core = new Core();
            
            File input = new File("./test/TinySQL-TextLINUX.txt");
            
            Scanner scan = new Scanner(new FileInputStream(input));

            while (scan.hasNextLine()) {
                // core.execute(scanner.nextLine());
                System.out.println(scan.nextLine());
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}