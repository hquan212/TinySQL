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
                    System.out.println("creating table please wait...");
                }
                else{
                    System.out.print("This implementation only accept drop or create table.");
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
            //Drop Table
            else if(res[0].equalsIgnoreCase("drop")){
                //add it into keyword
                key_word.add("drop");
                if(res[1].equalsIgnoreCase("table")) {
                    System.out.println("deleting (dropping) table please wait...");
                }
                else{
                    System.out.print("This implementation only accept drop or create table.");
                    return false;
                }
                
                table_names.add(res[2]);
                
                if(res.length>3){
                    System.out.print("Too many variables for Drop Table");
                    return false;
                }
                
            }
            //inserting tables
            else if(res[0].equalsIgnoreCase("insert")){
                //add it to keyword
                key_word("insert")
                if(!res[1].equalsIgnoreCase("into")){
                    System.out.print("This implementation only accept inteo from inserts.");
                    return false;
                }
                else{
                    System.out.println("inserting tuples into table please wait...");
                }
                
                t_names.add(res[2]);
                
                int index = -1;
                int string_index = -1;
                for(int i=3; i< res.length; i++){
                    if(res[i].equalsIgnoreCase("values")){
                       index = i;
                   }

                   if(res[i].equalsIgnoreCase("select")){
                       string_index = i;
                   }
                    
                }
                //No values for inserting into tables
                if(index<0 && s_index<0){
                   System.out.print("No values are inserted into the program. Please try again.");
                   return false;
                }
                
                if(index>0){
                    StringBuilder string_builder = new StringBuilder();
                    for(int i = 3; i< index; i++){
                        string_builder.append(res[i] + " ");
                    }
                    String temp = string_builder.toString();
                    if(temp.charAt(0) == '(' && temp.indexOf(")") > 0){
                        String sub = temp.substring(1, temp.indexO(")"));
                        String args[] = sub.split(",");
                        for(int j = 0; j < args.length; j++){
                            
                            args[j] = args[j].trim();
                            String[] field = args[j].split(" ");
                            if (field.length != 1) {
                                System.out.print("Wrong Argument Format inside INSERT into");
                                return false;
                            } else {
                                Argument argumentSub = new Argument(null, field[0]);
                                argumentList.add(argumentSub);
                            }
                            
                        }
                    }else{
                        System.out.print("Paranethesis were not found!");
                        return false;
                    }
            
                    string_builder = new StringBuilder();
                    for(int i = string_index; i< res.length; i++){
                        string_builder.append(res[i] + " ");
                        
                    }
                    
                    System.out.println("Processing selection parses")
                    return selectedParse(string_builder.toString().split(" "));
                    
                }
      
            }else if(res[0].equalsIgnoreCase("delete"){
                key_word.add("delete")
                delete = new TreeNode()
                if(!res[1].equalsIgnoreCase("from"){
                    System.out.print("No from key word after delete");
                    return false;
                }
                int index =-1;
                for(int i=0; i<res.length; i++){
                    if(res[i].equalsIgnoreCase("where")){
                        index = i;
                        break;
                    }
                }
                
                if(index<0){
                    index = res.length;
                }else{
                    delete.where = true;
                }
                
                StringBuilder string_builder_1 = new StringBuilder();
                
                for(int i=2;i<index;i++){
                    string_builder_1.append(res[i]+" ");
                }
                
                String[] tables = string_builder_1.toString().split(",");

                for(int i=0;i<tables.length;i++){
                    delete.table_names.add(tables[i].trim());
                }
                
                if(delete.where==true){
                    StringBuilder string_builder = new StringBuilder();
                    for(int i=index+1;i<res.length;i++){
                        string_builder.append(res[i]+" ");
                    }
                    delete.w_clause =Builder.generate(string_builder.toString());
                }
            }else if(res[0].equalsIgnoreCase("select"){
                return selectedParse(res)
            }else{
                
                return false;
            }
        }catch (Exception e)
            System.out.print("Syntax is in the wrong format");
            return false;
        }
        return true;
    }
    private boolean selectedParse(String[] res){
        select = new TreeNode();
        key_word.add("select");
        
        int f_index = -1;
        int d_index = -1;
        int w_index = -1;
        int o_index = -1;
        
        for(int i = 1; i < res.length; i++){
            if(res[i].equalsIgnoreCase("distinct")){
                d_index = i;
            }

            if(res[i].equalsIgnoreCase("from")){
                f_index = i;
            }

            if(res[i].equalsIgnoreCase("where")){
                w_index = i;
            }

            if(res[i].equalsIgnoreCase("order")){
                o_index = i;
            }
            
        }
        if( d_index == 1){
            select.distinct = true;
        }else if (d_index >=0){
            return false;
        }
        
        if(w_index>0) {
            if(o_index>0) {
                if (w_index > o_index) {
                    System.out.print("Order can not be found in front of WHERE!");
                    return false;
                }
            }
        }
        
        if(f_index < 0){
            System.out.print("FROM is not after SELECT");
            return false;
        }
        
        StringBuilder string_builder = new StringBuilder();
        
        // adding * char when not distinct
        if(d_index >0){
            for(int i=2;i<f_index;i++){
                string_builder.append(res[i]+" ");
            }
            String []arg_statment = string_builder.toString().split(",");

            if(arg_statment[0].trim().equalsIgnoreCase("*")){
                if(arg_statement.length==1) {
                    select.argument.add("*");
                }
                else{
                    System.out.print("check if two characters are connected to *")
                    return false;
                }

            }else{
                for(int i=0;i<arg_statement.length;i++){
                    arg_statement[i]=arg_statement[i].trim();
                    select.argument.add(arg_statement[i]);
                }
            }
        }else{
            
            for(int i=1;i<f_index;i++){
                string_builder.append(res[i]+" ");
            }
            String []arg_statment = string_builder.toString().split(",");

            if(arg_statment[0].trim().equalsIgnoreCase("*")){
                if(arg_statement.length==1) {
                    select.argument.add("*");
                }
                else{
                    System.out.print("check if two characters are connected to *")
                    return false;
                }

            }else{
                for(int i=0;i<arg_statement.length;i++){
                    arg_statement[i]=arg_statement[i].trim();
                    select.argument.add(arg_statement[i]);
                }
            }
        }
        
        string_builder = new StringBuilder();
        
        if(w_index >0){
            select.where = true;
            for(int i = f_index+1; i < w_index; i++){
                string_builder.append(res[i]+" ");
            }
            String tables[] = string_builder.toString().split(",");
            
            for(int i = 0; i<table.length; i++){
                tables[i] = tables[i].trim();
                select.table_names.add(tables[i]);
            }
            
            
            string_builder = new StringBuilder();
            if(o_index>0){
                select.order = true;
                for(int i = w_index; i < o_index; i++){
                    string_builder.append(res[i]+" ")
                    
                }
                select.w_clause = Builder.generate(string_builder.toString());
                
                if(!res[o_index+1].equalsIgnoreCase("by")){
                    System.out.print("No 'by' after ORDER");
                    return false;
                }
                
                string_builder = new StringBuilder();
                for(int i = o_index + 2; i<res.length; i++){
                    string_builder.append(res[i]+" ");  
                }
                select.o_clause = string_builder.toString();
            }else{
                for(int i=w_index + 1; i <res.length;i++){
                    string_builder.append(res[i]+" ");
                }
                select.w_clause = Builder.generate(string_builder.toString());
            }
        }else{
            if(o_index > 0){
                if(!res[o_index+1].equalsIgnoreCase("by")){
                    System.out.print("No 'by' after ORDER");
                    return false;
                }
                select.order = true;
                for(int i = f_index; i < o_index; i++){
                    string_builder.append(res[i]+" ");
                }
                String tables[] = string_builder.toString().split(",");
                for(int i = 0; i< tables.length; i++){
                    tables[i] = tables[i].trim();
                    select.table_names.add(tables[i]);
                }
                
                string_builder = new StringBuilder();
                for(int i = o_index+2; i< res.length ; i++){
                    string_builder.append(res[i]+" ");
                }
                select.oclause = string_builder.toString();
                
            }else{
                for(int i=w_index + 1; i <res.length;i++){
                    string_builder.append(res[i]+" ");
                }
                String tables[] = string_builder.toString().split(",");
                for(int i = 0; i< tables.length; i++){
                    tables[i] = tables[i].trim();
                    select.table_names.add(tables[i]);
                }
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