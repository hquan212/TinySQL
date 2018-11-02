import storageManager.Field;
import storageManager.FieldType;

import java.util.ArrayList;

/*

    created 10/2/18

*/

public class Parser{
    String sentence;
    //key word is used for keeping the terms in the query
    ArrayList<String> key_word;
    ArrayList<Argument> argumentList;
    ArrayList<String> words;
    ArrayList<String> t_names;
    ArrayList<String> values;
    TreeNode delete;
    TreeNode select;
    
    //initialize all variables
    public Parser(){
        sentence = null
        key_word = new ArrayList<>();
        argumentList = new ArrayList<>();
        words = new ArrayList<>();
        t_names = new ArrayList<>();
        values = new ArrayList<>();
        delete = null;
        select = null;
        
    }
    //get the a string from execution to parse through the values
    public boolean syntax(String sampleString){
        try{
            //clean up
            this.reset();
            sampleString = sampleString.trim();
            sampleString = sampleString.replace("//s{1,}", " ");
            
            String[] res = sampleString.split(" ");
            
            //Creating the table
            if(res[0].equalsIgnoreCase("create"){
                key_word.ad("create");
                if(res[1].equalsIgnoreCase("table")) {
                    System.out.print("creating table please wait...");
                }
                else{
                    System.out.print("This implementation only accept create table.");
                    return false;
                }
                
                StringBuilder stringBuilder = new StringBuilder();
                for(int i = 3; i<res.length;i++){
                    stringBuilder.append(res[i]+" ");
                }
                String temp = stringBuilder.toString();
                
                // looking at the attributes in the table and their types
                if(temp.charAt(0)=='(' && temp.indexOf(")")>0){
                    String sub=temp.substring(1,temp.indexOf(")"));
                    String[] attributes =sub.split(",");
                    for(int j = 0; j<attributes.length; j++){
                        attributes[j] = attributes.trim();
                        String[] field=attributes[j].split(" ");
                        //length should be two
                        if(field.length!=2){
                                System.out.print("Wrong attribute Format (Attribute_Name Data_Type)");
                                return false;
                        }
                        
                        //check if they are correct types
                        if(field[1].equalsIgnoreCase("str20")){
                            Argument argument=new Argument(field[1],field[0]);
                            argumentList.add(argument);
                        }else if(field[1].equalsIgnoreCase("int")){
                            Argument argument=new Argument(field[1],field[0]);
                            argumentList.add(argument);
                        }else{
                            System.out.print("Type specified could only be INT or STR20 in this implementation");
                            return false;
                        }
                    }
                }
                System.out.print("Table created!");
            }
            else if(){
                
            }
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