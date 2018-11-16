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
    
    public Relation select_core(){
    	Relation joined_table;
    	boolean one_pass = true;
    	for(int i = 0; i< parse.select.table_names.size(); i++){
    		if(schema_manager.getRelation(parse.select.table_names.get(i)).getNumOfBlocks() != 1){
    			one_pass = false;
    		}
    	}
    	if(one_pass&&parse.select.table_names.size()>1){
    		System.out.println("Using one-pass algortithm");
    		joined_table= onePass(parse.select.table_names);
    		if(parse.select.w_clause == null && parse.select.argument.get(0).equalsIgnoreCase("*")){
    			if(parser.select.distinct == true){
    				joined_table=first_pass(joined_table, joined_table.getSchema().getFieldNames());
    				joined_table=distinct_second_pass(joined_table, joined_table.getSchema().getFieldNames());
    			}
    			if(parser.select.order == true){
    				ArrayList<String> order_attr=new ArrayList<String>();
    				String order_by = parse.select.o_clause.trim();
    				order_attr.add(order_by);
    				joined_table=first_pass(joined_table, order_attr);
    				if(joined_table.getNumOfBlocks()>10){
    					joined_table=order_second_pass(joined_table,order_attr);
    				}
    			}
    			
    			return joined_table;
    		}
    	}else{
    		if(parser.select.table_names.size()==1){
    			Schema new_schema =  schema_manager.getSchema(parse.select.table_names.get(0));
    			joined_table = schema_manager.createRelation("new_relation", new_schema);
    			int block= schema_manager.getRelation(parse.select.table_names.get(0)).getNumOfBlocks();
    			int scan_times;
    			if(block%9 == 0){
    				scan_times = blocks/9;
    			}else{
    				scan_times = blocks/9+1;
    			}
    			for(int i = 0; i < scan_times; i++){
    				if((i+1)*9 > block){
    					schema_manager.getRelation(parse.select.table_names.get(0)).getBlocks(i*9,0,blocks-(9*i));
    					joined_table.setBlocks(i*9,0,blocks-(9*i));
    				}else{
    					schema_manager.getRelation(parse.select.table_name.get(0)).getBlocks(i*9,0,9);
    					joined_table.setBlocks(i*9,0,9);
    				}
    			}
    			if(parse.select.w_clause == null && parse.select.argument.get(0).equalsIgnoreCase("*")){
    				if(parser.select.distinct == true){
	    				joined_table=first_pass(joined_table, joined_table.getSchema().getFieldNames());
	    				joined_table=distinct_second_pass(joined_table, joined_table.getSchema().getFieldNames());
	    			}
	    			if(parser.select.order == true){
	    				ArrayList<String> order_attr=new ArrayList<String>();
	    				String order_by = parse.select.o_clause.trim();
	    				order_attr.add(order_by);
	    				joined_table=first_pass(joined_table, order_attr);
	    				if(joined_table.getNumOfBlocks()>10){
	    					joined_table=order_second_pass(joined_table,order_attr);
	    				}
    				}
    			}
    			return joined_table;
    		}else{
    			String pre_table = parse.select.table_names.get(0);
    			String now_table;
    			
    			boolean last_one=false;
    			for(int i= 1; i<parse.select.table_names.size(); i++){
    				if(i== parse.select.table_names.size()-1)){
    					last_one = true;
    				}
    				now_table=parse.select.table_names.get(i);
    				String temp = pre_table;
    				boolean natual_join = false;
    				if(i==1){
    					natual_join = true;
    				}
    				pre_table=new_join(pre_table, now_table,last_one,natual_join);
    				if(temp.contains(",")){
    					schema.deleteRelation(temp);
    				}
    			}
    			joined_table=schema_manager.getRelation(pre_table);
    		}
	    	if(parser.select.table_names.size()==1){
	    			Schema new_schema =  schema_manager.getSchema(parse.select.table_names.get(0));
	    			joined_table = schema_manager.createRelation("new_relation", new_schema);
	    			int block= schema_manager.getRelation(parse.select.table_names.get(0)).getNumOfBlocks();
	    			int scan_times;
	    			if(block%9 == 0){
	    				scan_times = blocks/9;
	    			}else{
	    				scan_times = blocks/9+1;
	    			}
	    			for(int i = 0; i < scan_times; i++){
	    				if((i+1)*9 > block){
	    					schema_manager.getRelation(parse.select.table_names.get(0)).getBlocks(i*9,0,blocks-(9*i));
	    					joined_table.setBlocks(i*9,0,blocks-(9*i));
	    				}else{
	    					schema_manager.getRelation(parse.select.table_name.get(0)).getBlocks(i*9,0,9);
	    					joined_table.setBlocks(i*9,0,9);
	    				}
	    			}
	    			if(parse.select.w_clause == null && parse.select.argument.get(0).equalsIgnoreCase("*")){
	    				if(parser.select.distinct == true){
		    				joined_table=first_pass(joined_table, joined_table.getSchema().getFieldNames());
		    				joined_table=distinct_second_pass(joined_table, joined_table.getSchema().getFieldNames());
		    			}
		    			if(parser.select.order == true){
		    				ArrayList<String> order_attr=new ArrayList<String>();
		    				String order_by = parse.select.o_clause.trim();
		    				order_attr.add(order_by);
		    				joined_table=first_pass(joined_table, order_attr);
		    				if(joined_table.getNumOfBlocks()>10){
		    					joined_table=order_second_pass(joined_table,order_attr);
		    				}
	    				}
	    			}
	    			return joined_table;
	    	}
    	}
    	Schema return_schema;
    	Relation return_relation;
    	if(parse.select.table_names.size()>1){
    		ArrayList<String> return_field_names = new ArrayList<String>();
    		ArrayList<FieldType> return_field_types = new ArrayList<FieldType>();
    		if(parse.select.argument.get(0).equalsIgnoreCase("*"){
    			return_field_names = joined_table.getSchema().getFieldNames();
    			return_field_types = joined_table.getSchema().getFieldTypes();
    			
    		}else{
    			return_field_names= parse.select.argument;
    			for(int i=0; i <return_field_names.size(); i++){
    				if(return_field_names.get(i).split("\\.").length==1){
    					return_field_types.add(joined_table.getSchema().getfieldType(return_field_names.get(i)));
    				}else{
    					String real_name = return_field_names.get(i).split("\\.")[1];
    					return_field_types.add(joined_table.getSchema().getfieldType(real_name));
    				}
    			}
    		}
    		return_schema = new Schema(return_field_names,return_field_types);
    		return_relation=schema_manager.createRelation("return_relation", return_schema);
    	}
    	int table_blocks=joined_table.getNumOfBlocks();
    		if(table_blocks == 0){
    			System.out.print("This is an empty table.")
    		}
    		
    		int scan_times;
    		if((table_blocks%(Config.NUM_OF_BLOCKS_IN_MEMORY-1))!=0){
    			scan_times = table_blocks/(Config.NUM_OF_BLOCKS_IN_MEMORY-1)+1;
    		}
    		else{
    			scan_times = table_blocks/(Config.NUM_OF_BLOCKS_IN_MEMORY-1);
    		}
    		for(int i = 0; i<scan_times;i++){
    			int num_blocks;
    			if(table_blocks-i*(Config.NUM_OF_BLOCKS_IN_MEMORY-1)< (Config.NUM_OF_BLOCKS_IN_MEMORY-1)){
    				num_blocks=table_blocks-(i*(Config.NUM_OF_BLOCKS_IN_MEMORY-1));
    				
    			}else{
    				num_blocks=Config.NUM_OF_BLOCKS_IN_MEMORY-1;
    			}
    			joined_table.getBlocks(i*(Config.NUM_OF_BLOCKS_IN_MEMORY-1),0,num_blocks);
    			SubTreeNode select_tree = parse.select.w_clause;
    			for(int j = 0; j<num_blocks;j++){
    				Block test_block=mem.getBlock(j);
    				ArrayList<Tuple> test_tuples=test_block.getTuples();
    				if(test_tuples.size()==0){
    					continue;
    				}
    				for(int k = 0; k<test_tuples.size();k++){
    					if(select_tree == null || where_judge(select_tree, test_tuples.get(k))){
    						if(parse.select.argument.get(0).equalsIgnoreCase("*"){
    							appendTupleToRelation(return_relation,mem,9,test_tuples.get(k));
    						}else{
    							Tuple return_tuple=return_relation.createTuple();
    							for(int n = 0; n<parse.select.argument.size(); n++){
    								if(parse.select.table_names.size()==1){
    									String table_attr[] = parse.select.argument.get(n).split("\\.");
    								
										if(table_attr.length == 1){
											if(return_schema.getFieldType(parse.select.argument.get(n))==FieldType.STR20){
												return_tuple.setField(parse.select.argument.get(n),test_block.getTuple(k).getField(parse.select.argument.get(n)).str);
												
											}
											else{
											return_tuple.setField(parse.select.argument.get(n),test_block.getTuple(k).getField(parse.select.argument.get(n)).integer);
												
											}
							        	}else{
							        		if(return_schema.getFieldType(parse.select.argument.get(n))==FieldType.STR20){
												return_tuple.setField(parse.select.argument.get(n),test_block.getTuple(k).getField(table_attr[1]).str);
							        			
							        		}
											else{
												return_tuple.setField(parse.select.argument.get(n),test_block.getTuple(k).getField(table_attr[1]).integer);
												
											}
							        	
							        	}
									  									
    								}else{
    									if(return_schema.getFieldType(parse.select.argument.get(n))==FieldType.STR20){
											return_tuple.setField(parse.select.argument.get(n),test_block.getTuple(k).getField(parse.select.argument.get(n)).str);
    										
    									}
										else{
											return_tuple.setField(parse.select.argument.get(n),test_block.getTuple(k).getField(parse.select.argument.get(n)).integer);
											
										}
								
    								}
    								
    							}
    						}
    						appendTupleToRelation(return_relation,mem,9,return_tuple);
    					}
    				}
    			}
    		}
    		if(parse.select.distinct==true){
				return_relation=first_pass(return_relation, return_relation.getSchema().getFieldNames());
				return_relation=distinct_second_pass(return_relation, return_relation.getSchema().getFieldNames());
			}
    		if(parse.select.order==true){
				ArrayList<String> order_attr=new ArrayList<String>();
				String order_by =parse.select.o_clause.trim();
				order_attr.add(order_by);
				return_relation=first_pass(return_relation, order_attr);
				if(return_relation.getNumOfBlocks()>10){
					return_relation=order_second_pass(return_relation, order_attr);
				}
			}
    		if(parse.select.table_names.size()>1) {
    			schema_manager.deleteRelation(joined_table.getRelationName());
    		}
    		return return_relation;
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
        
        
     

        
