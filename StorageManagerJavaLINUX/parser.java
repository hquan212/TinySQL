import storageManager.Field;
import storageManager.FieldType;

import java.util.ArrayList;

/*

    created 10/2/18

*/

public class Parser{
    String sentence;
    ArrayList<String> key_word;
    ArrayList<Argument> argument;
    ArrayList<String> words;
    ArrayList<String> t_names;
    ArrayList<String> values;
    
    //initialize all variables
    public Parser(){
        sentence = null
        key_word = new ArrayList<>();
        argument = new ArrayList<>();
        words = new ArrayList<>();
        t_names = new ArrayList<>();
        values = new ArrayList<>();
        
    }
    public void reset(){
        
    }
    
};