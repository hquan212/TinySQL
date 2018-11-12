import storageManager.Block;
import storageManager.Disk;
import storageManager.FieldType;
import storageManager.MainMemory;
import storageManager.Relation;
import storageManager.Schema;
import storageManager.SchemaManager;
import storageManager.Tuple;
import storageManager.Config;
import java.util.*;

public class Core {
 
	private Parser parse;
	public MainMemory mem;
	public Disk disk;
	public SchemaManager schema_manager;
	public Executor(){
		parse = new Parser();
		mem=new MainMemory();
		disk=new Disk();
		schema_manager=new SchemaManager(mem,disk);
	    disk.resetDiskIOs();
	    disk.resetDiskTimer();
	    
	}
	public void core(String stm){
	    
		if(parse.syntax(stm)){
		   if(parse.key_word.get(0).equalsIgnoreCase("create")){
			   create_execute();
		   }
		   else if(parse.key_word.get(0).equalsIgnoreCase("insert")){
			   insert_execute();
			   if(schema_manager.relationExists("return_relation")) {
			       schema_manager.deleteRelation("return_relation");
			       
			   }//drop a table
			   
			   if(schema_manager.relationExists("new_relation")){
				   schema_manager.deleteRelation("new_relation");
			   }
			}
			
		   else if(parse.key_word.get(0).equalsIgnoreCase("drop")){
			   drop_execute();
		   }
		   
		   else if(parse.key_word.get(0).equalsIgnoreCase("delete")){
			   delete_execute();
		   }
		   
		   else if(parse.key_word.get(0).equalsIgnoreCase("select")){
			   Relation r=select_execute();
			   System.out.println(r);
			   if(r.getRelationName().contains(",")) {
				   schema_manager.deleteRelation(r.getRelationName());
			   }
			   if(schema_manager.relationExists("new_relation")){
				   schema_manager.deleteRelation("new_relation");
			   }
			   
				if(schema_manager.relationExists("return_relation")){   
			     schema_manager.deleteRelation("return_relation");
				}
			   if(parse.select.distinct==true){
				   if(schema_manager.relationExists("order_relation")){
				   schema_manager.deleteRelation("distinct_relation");
				   }
			   }
			   if(parse.select.order==true){
				   if(schema_manager.relationExists("order_relation")){
				   schema_manager.deleteRelation("order_relation");
				   }
			   }
			   if(schema_manager.relationExists("opr")){   
				     schema_manager.deleteRelation("opr");
					}
					
		   }
		   else{
			   System.out.println("NOT A VALID TINY-SQL STATEMENT!");
		   }
			
		}
	}
	
	
	public void delete_core(){

		String toupleName = parse.delete.t_names.get(0);
		Relation tableToDelete = schema_manager.getRelation(toupleName);
		int toupleBlocks = deleted_table.getNumOfBlocks();
		if (toupleBlocks == 0){
		    System.out.println("The table is already empty!");
		}
		
		int scans =0;
		if ((toupleBlocks % Config.NUM_OF_BLOCKS_IN_MEMORY)!=0){
		    scans = toupleBlocks/Config.NUM_OF_BLOCKS_IN_MEMORY + 1;
		}
		else {
		    scans = toupleBlocks/Config.NUM_OF_BLOCKS_IN_MEMORY;
		}
		
		for (int i=0; i<scans; i++){
		    int numBlocks =0;
		    if (toupleBlocks - i *Config.NUM_OF_BLOCKS_IN_MEMORY <= Config.NUM_OF_BLOCKS_IN_MEMORY){
		        numBlocks = toupleBlocks = i * Config.NUM_OF_BLOCKS_IN_MEMORY;
		    } else {
		        numBlocks = Config.NUM_OF_BLOCKS_IN_MEMORY;
		    }
		    tableToDelete.getBlocks(i * Config.NUM_OF_BLOCKS_IN_MEMORY, 0, numBlocks);
		    blah treeToDelete = parse.delete.w_clause;        //"update later"
		    for (int j =0; j<numBlocks; j++){
		        Block b = mem.getBlock(j);
		        if (treeToDelete == null){
		            b.clear();
		            continue;
		        }
		        
		        ArrayList<Tuple> tupleList = b.getTuples();
		        if (tupleList.size() == 0){
		            continue;
		        }
		        
		        for (int k = 0; k <tupleList.size(); k++){
		            if(treeToDelete = null || where_judge(treeToDelete, tupleList.get(k))){
		                b.invalidateTuple(k);
		            }
		        }
		    }
		    
		    tableToDelete.setBlocks(i * Config.NUM_OF_BLOCKS_IN_MEMORY, 0, numBlocks);
		    
		}
		
	}

    public void create_core() {
        ArrayList<FieldType> fieldType = new ArrayList<FieldType>();
        ArrayList<String> fieldNames = new ArrayList<String>();
        
        for (int i=0; i<parse.arg.size(); i++) {
            String name = parse.arg.get(i).name;
            fieldNames.add(name);
            if (name.equalsIgnoreCase("INT")) { fieldType.add(FieldType.INT); }
            else if (name.equalsIgnoreCase("STR20")) { fieldType.add(FieldType.STR20); }
        }

        Schema schema = new Schema(fieldNames, fieldType);
        schema_manager.createRelation(parse.t_names.get(0), schema);
        System.out.println("Created table: " + parse.t_names.get(0) + 
        " with schema: \n" + schema);
    }
    
    public void drop_core(){
        String tableName = parse.t_names.get(0);
        schema_manager.deleteRelation(tableName);
        System.out.println("Deted table: " + tableName);
    }
    
    public void insert_core(){
        String toTableName = parse.t_names.get(0);
        
        if(!schema_manager.relationExists(toTableName)) {
            System.out.println("Table does not exist!");
        }
        Realation relationToInsert = schema_manager.getRelation(toTableName);
        Tuple tuple = relationToInsert.createTuple();
        Schema relationSchema = relationToInsert.getSchema();
        
        if (parse.select == null ){
            for (int i=0; i< tuple.getNumOfFields(); i++) {
                if ( relationSchema.getFieldType(parse.arg.get(i).name == FieldType.STR20) {
                    String val = parse.values.get(i).replaceAll("\"", "");
                    tuple.setField(parse.arg.get(i).name, value);
                }
                else {
                    tuple.setField(parse.arg.get(i).name, Integer.parseInt(parse.values.get(i)));
                }
            }
            appendTupleToRelation(relationToInsert, mem, 2, tuple);
        }
        
        else if (parse.select!=null) {
            Relation selectedRelation = select_core();
            Schema tempSchema = new Schema(selectedRelation.getSchema());
            Relation tempRelation = schema_manager.createRelation("TemporarySchema", tempSchema);
            
            for (int i=0; i<selected_relation.getNumOfBlocks(); i++) {
                selectedRelation.getBlock(i, 9);
                tempRelation.setBlock(i, 9);
            }
            
            int formerBlocks = relationToInsert.getNumOfBlocks();
            for (int i=0; i<tempRelation.getNumOfBlocks(); i++) {
                tempRelation.getBlock(i,9);
                relationToInsert.setBlock(i+formerBlocks, 9);
            }
        }
    }
    
    private Relation onePass(ArrayList<String) t_names){
    	
    	Schema onePass = schemaCombine(t_names);
    	Relation operation = schema_manager.createRelation("opr", onePass);
    	for (int i=0; i<t_names.size(); i++){
    		schema_manager.getRelation(t_names.get(i)).getBlock(0,i);
    	}
    	
    	ArrayList<Tuple> tList = new ArrayList<Tuple>();
    	int n = 1;
    	
    	for(int i=0; i<t_names.size(); i++){
    		n = n * (schema_manager.getRelation(t_names.get(i)).getNumOfTuples());
    	}
    	
    	for (int i=0; i < n; i++){
    		Tuple temp = operation.createTuple()
    		tList.add(temp);
    	}
    	
    	if(parse.select.distinct == false&&parse.select.order == false&&parse.select.w_clause==null&&parse.select.arg.get(0.equalsIgnoreCase("*")){
    		System.out.println(onePass);
    	}
    	
    	tList = onePassMemory (t.t_names.size(), 0, t_names, n);
    	for (int i = 0; i < tList.size(), 0, t_names, n) {
    		if(parse.select.distinct == false&&parse.select.order == false&&parse.select.w_clause==null&&parse.select.arg.get(0.equalsIgnoreCase("*")) {
    			System.out.println(tList.get(i));
    		}
    		
    		else {
    			appendTuple(operation, mem, 9, tList.get(i));
    		}
    		
    	}
    	
    	return operation;
    }
}
        
        
     

        
