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
    TreeNode delete;
    TreeNode select;
    
    //initialize all variables
    public Parser(){
        sentence = null
        key_word = new ArrayList<>();
        argument = new ArrayList<>();
        words = new ArrayList<>();
        t_names = new ArrayList<>();
        values = new ArrayList<>();
        delete = null;
        select = null;
        
    }
    //get the a string from execution to parse through the values
    public boolean syntax(String sampleString){
        try{
            this.reset();
            
        }
    }
    public void reset(){
        sentence = null
        key_word = new ArrayList<>();
        argument = new ArrayList<>();
        words = new ArrayList<>();
        t_names = new ArrayList<>();
        values = new ArrayList<>();
        delete = null;
        select = null;
        
    }
    
};