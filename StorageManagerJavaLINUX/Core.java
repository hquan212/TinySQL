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
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Core {
 
	private Parser parse;
	public MainMemory mem;
	public Disk disk;
	public SchemaManager schema_manager;
	public Core(){
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
			   create_core();
		   }
		   else if(parse.key_word.get(0).equalsIgnoreCase("insert")){
			   insert_core();
			   if(schema_manager.relationExists("return_relation")) {
			       schema_manager.deleteRelation("return_relation");
			       
			   }//drop a table
			   
			   if(schema_manager.relationExists("new_relation")){
				   schema_manager.deleteRelation("new_relation");
			   }
			}
			
		   else if(parse.key_word.get(0).equalsIgnoreCase("drop")){
			   drop_core();
		   }
		   
		   else if(parse.key_word.get(0).equalsIgnoreCase("delete")){
			   delete_core();
		   }
		   
		   else if(parse.key_word.get(0).equalsIgnoreCase("select")){
			   Relation r=select_core();
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

		String toupleName = parse.delete.table_names.get(0);
		Relation tableToDelete = schema_manager.getRelation(toupleName);
		int toupleBlocks = tableToDelete.getNumOfBlocks();
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
		    SubTreeNode treeToDelete = parse.delete.w_clause;        //"update later"
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
		            if(treeToDelete == null || where_judge(treeToDelete, tupleList.get(k))){
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
        
        for (int i=0; i<parse.argumentList.size(); i++) {
            String name = parse.argumentList.get(i).name;
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
        Relation relationToInsert = schema_manager.getRelation(toTableName);
        Tuple tuple = relationToInsert.createTuple();
        Schema relationSchema = relationToInsert.getSchema();
        
        if (parse.select == null ){
            for (int i=0; i< tuple.getNumOfFields(); i++) {
                if ( relationSchema.getFieldType(parse.argumentList.get(i).name) == FieldType.STR20) {
                    String val = parse.values.get(i).replaceAll("\"", "");
                    tuple.setField(parse.argumentList.get(i).name, val);
                }
                else {
                    tuple.setField(parse.argumentList.get(i).name, Integer.parseInt(parse.values.get(i)));
                }
            }
            appendTupleToRelation(relationToInsert, mem, 2, tuple);
        }
        
        else if (parse.select!=null) {
            Relation selectedRelation = select_core();
            Schema tempSchema = new Schema(selectedRelation.getSchema());
            Relation tempRelation = schema_manager.createRelation("TemporarySchema", tempSchema);
            
            for (int i=0; i<selectedRelation.getNumOfBlocks(); i++) {
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
    			if(parse.select.distinct == true){
    				joined_table=first_pass(joined_table, joined_table.getSchema().getFieldNames());
    				joined_table=distinct_second_pass(joined_table, joined_table.getSchema().getFieldNames());
    			}
    			if(parse.select.order == true){
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
    		if(parse.select.table_names.size()==1){
    			Schema new_schema =  schema_manager.getSchema(parse.select.table_names.get(0));
    			joined_table = schema_manager.createRelation("new_relation", new_schema);
    			int blocks = schema_manager.getRelation(parse.select.table_names.get(0)).getNumOfBlocks();
    			int scan_times;
    			if(blocks%9 == 0){
    				scan_times = blocks/9;
    			}else{
    				scan_times = blocks/9+1;
    			}
    			for(int i = 0; i < scan_times; i++){
    				if((i+1)*9 > blocks){
    					schema_manager.getRelation(parse.select.table_names.get(0)).getBlocks(i*9,0,blocks-(9*i));
    					joined_table.setBlocks(i*9,0,blocks-(9*i));
    				}else{
    					schema_manager.getRelation(parse.select.table_names.get(0)).getBlocks(i*9,0,9);
    					joined_table.setBlocks(i*9,0,9);
    				}
    			}
    			if(parse.select.w_clause == null && parse.select.argument.get(0).equalsIgnoreCase("*")){
    				if(parse.select.distinct == true){
	    				joined_table=first_pass(joined_table, joined_table.getSchema().getFieldNames());
	    				joined_table=distinct_second_pass(joined_table, joined_table.getSchema().getFieldNames());
	    			}
	    			if(parse.select.order == true){
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
    				if(i== parse.select.table_names.size()-1){
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
    					schema_manager.deleteRelation(temp);
    				}
    			}
    			joined_table=schema_manager.getRelation(pre_table);
    		}
	    	if(parse.select.table_names.size()==1){
	    			Schema new_schema =  schema_manager.getSchema(parse.select.table_names.get(0));
	    			joined_table = schema_manager.createRelation("new_relation", new_schema);
	    			int blocks= schema_manager.getRelation(parse.select.table_names.get(0)).getNumOfBlocks();
	    			int scan_times;
	    			if(blocks%9 == 0){
	    				scan_times = blocks/9;
	    			}else{
	    				scan_times = blocks/9+1;
	    			}
	    			for(int i = 0; i < scan_times; i++){
	    				if((i+1)*9 > blocks){
	    					schema_manager.getRelation(parse.select.table_names.get(0)).getBlocks(i*9,0,blocks-(9*i));
	    					joined_table.setBlocks(i*9,0,blocks-(9*i));
	    				}else{
	    					schema_manager.getRelation(parse.select.table_names.get(0)).getBlocks(i*9,0,9);
	    					joined_table.setBlocks(i*9,0,9);
	    				}
	    			}
	    			if(parse.select.w_clause == null && parse.select.argument.get(0).equalsIgnoreCase("*")){
	    				if(parse.select.distinct == true){
		    				joined_table=first_pass(joined_table, joined_table.getSchema().getFieldNames());
		    				joined_table=distinct_second_pass(joined_table, joined_table.getSchema().getFieldNames());
		    			}
		    			if(parse.select.order == true){
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
    		if(parse.select.argument.get(0).equalsIgnoreCase("*")){
    			return_field_names = joined_table.getSchema().getFieldNames();
    			return_field_types = joined_table.getSchema().getFieldTypes();
    			
    		}else{
    			return_field_names= parse.select.argument;
    			for(int i=0; i <return_field_names.size(); i++){
    				if(return_field_names.get(i).split("\\.").length==1){
    					return_field_types.add(joined_table.getSchema().getFieldType(return_field_names.get(i)));
    				}else{
    					String real_name = return_field_names.get(i).split("\\.")[1];
    					return_field_types.add(joined_table.getSchema().getFieldType(real_name));
    				}
    			}
    		}
    		return_schema = new Schema(return_field_names,return_field_types);
    		return_relation=schema_manager.createRelation("return_relation", return_schema);
    	}
    	int table_blocks=joined_table.getNumOfBlocks();
    		if(table_blocks == 0){
    			System.out.print("This is an empty table.");
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
    						if(parse.select.argument.get(0).equalsIgnoreCase("*")){
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
    							appendTupleToRelation(return_relation,mem,9,return_tuple);
    						}
    						
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
    
    private Relation onePass(ArrayList<String> t_names){
    	
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
    		Tuple temp = operation.createTuple();
    		tList.add(temp);
    	}
    	
    	if(parse.select.distinct==false&&parse.select.order==false&&parse.select.w_clause==null&&parse.select.argument.get(0).equalsIgnoreCase("*")){
			System.out.println(onePass);
    	}
    	
    	tList = onePassMemory(tList, t_names.size(), 0, t_names, n);
    	for (int i = 0; i < tList.size(); i++) {
    		if(parse.select.distinct == false&&parse.select.order == false&&parse.select.w_clause==null&&parse.select.argument.get(0).equalsIgnoreCase("*")) {
    			System.out.println(tList.get(i));
    		}
    		
    		else {
    			appendTupleToRelation(operation, mem, 9, tList.get(i));
    		}
    		
    	}
    	
    	return operation;
    }
    
    private ArrayList<Tuple> onePassMemory(ArrayList<Tuple> tList, int num, int time, ArrayList<String> tups, int totalTups){
    	
    	if(time==(num-1)){
    		
			int rxTime=schema_manager.getRelation(tups.get(time)).getNumOfTuples();
			int rx=totalTups/rxTime;
			int fNums=tList.get(0).getNumOfFields();
			
			
			for(int i=0;i<rxTime;i++){
				
				for(int j=i*rx;j<(rx*(i+1));j++){
					
					int fieldNums=schema_manager.getSchema(tups.get(time)).getNumOfFields();
					for(int k=0;k<fieldNums;k++){
						
					 if(tList.get(j).getField(fNums-1-k).type==FieldType.STR20){
						tList.get(j).setField(fNums-1-k,mem.getBlock(time).getTuple(i).getField(fieldNums-k-1).str);
					 	
					 }
					 else{
					    tList.get(j).setField(fNums-1-k,mem.getBlock(time).getTuple(i).getField(fieldNums-k-1).integer);
					 	
						}
					 }
					 
					}
				}
				
			return tList;
			}			
			
		
         tList=onePassMemory(tList,num,time+1,tups,totalTups);
         int rxTime=schema_manager.getRelation(tups.get(time)).getNumOfTuples();
		 int rx=totalTups/rxTime;
		 int fNums=tList.get(0).getNumOfFields();
         int previousFnums=0;
         
         
         for(int i=0;i<time;i++){
        	 previousFnums=schema_manager.getSchema(tups.get(i)).getNumOfFields()+previousFnums;
        	 
         }
         
         for(int i=0;i<rxTime;i++){
         	
				for(int j=i*rx;j<(rx*(i+1));j++){
					
					int fieldNums=schema_manager.getSchema(tups.get(time)).getNumOfFields();
					for(int k=0;k<fieldNums;k++){
					 if(tList.get(j).getField(previousFnums+k).type==FieldType.STR20){
						tList.get(j).setField(previousFnums+k,mem.getBlock(time).getTuple(i).getField(k).str);
					 	
					 }
					 else{
					    tList.get(j).setField(previousFnums+k,mem.getBlock(time).getTuple(i).getField(k).integer);
					 	
						}
					 }
					}
				}
         
         
		return tList;
    	
    }
           
    private Schema schemaCombine(ArrayList<String> tableNames){
    	
    	Schema[] schm_a = new Schema[tableNames.size()];
    	ArrayList<String> joinFildName = new ArrayList<String>();
    	ArrayList<FieldType> joinedFileTypes = new ArrayList<FieldType>();
    	
    	for (int i=0; i< schm_a.length; i++) {
    		schm_a[i] = schema_manager.getSchema(tableNames.get(i));
			ArrayList<String> field_names =schm_a[i].getFieldNames();
		    for(int j=0; j<field_names.size() ;j++){
				String new_name = tableNames.get(i) + "." + field_names.get(j);
				field_names.set(j, new_name);
			}
		    joinFildName.addAll(field_names);
		    joinedFileTypes.addAll(schm_a[i].getFieldTypes());
		}
		
	    Schema joined_schema = new Schema(joinFildName,joinedFileTypes);
	    return joined_schema;
    } //merge_schema
    
    private String natural_join(String t_1, String t_2, String attr) {
    	
    	Relation r1 = schema_manager.getRelation(t_1);
    	Relation r2 = schema_manager.getRelation(t_2);
    	ArrayList<FieldType> fieldType1 = r1.getSchema().getFieldTypes();
    	ArrayList<FieldType> fieldType2 = r2.getSchema().getFieldTypes();
    	fieldType1.addAll(fieldType2);
    	ArrayList<String> newFields = new ArrayList<String>();
    	
      	// Add the names of the fields to a total list
    	//Check for r1
    	if (r1.getSchema().getFieldNames().get(0).contains(".")) {
    		newFields = r1.getSchema().getFieldNames();
    	}
    	else {
    		for (int i=0; i < r1.getSchema().getNumOfFields(); i++) {
    			String name = t_1 + "." + r1.getSchema().getFieldNames().get(i);
    			newFields.add(name);
    		}
    	}
    	
    	// Check for r2
    	if (r2.getSchema().getFieldNames().get(0).contains(".")) {
    		newFields.addAll(r2.getSchema().getFieldNames());
    	}
    	else {
    		for (int i=0; i < r2.getSchema().getNumOfFields(); i++) {
    			String name = t_2 + "." + r2.getSchema().getFieldNames().get(i);
    			newFields.add(name);
    		}
    	}
    	
    	String n_j_name = t_1 + "," + t_2;
    	Schema n_j_schema = new Schema(newFields, fieldType1);
    	Relation njr = schema_manager.createRelation(n_j_name, n_j_schema);
    	ArrayList<String> n_j_field = new ArrayList<String>();
    	n_j_field.add(attr);
    	
    	//first pass
    	r1 = first_pass(r1, n_j_field);
    	r2 = first_pass(r2, n_j_field);
    	
    	//second pass
    	Heap heap1 = new Heap(80,n_j_field);
    	Heap heap2 = new Heap(80,n_j_field);
    	int r1_blocks = r1.getNumOfBlocks();
    	int r2_blocks = r2.getNumOfBlocks();
    	int sb_num1 = 0;
    	if(r1_blocks%Config.NUM_OF_BLOCKS_IN_MEMORY==0){
    		sb_num1=r1_blocks/Config.NUM_OF_BLOCKS_IN_MEMORY;
    	}
    	else{
    		sb_num1=r1_blocks/Config.NUM_OF_BLOCKS_IN_MEMORY+1;
    	}
    	int sb_num2 = 0;
    	if(r2_blocks%Config.NUM_OF_BLOCKS_IN_MEMORY==0){
    		sb_num2=r2_blocks/Config.NUM_OF_BLOCKS_IN_MEMORY;
    	}
    	else{
    		sb_num2=r2_blocks/Config.NUM_OF_BLOCKS_IN_MEMORY+1;
    	}
    	
    	for(int i=0; i<sb_num1; i++){//put the first block of each sublist of relation1 into memory, starting from 0
	    	r1.getBlock(i*Config.NUM_OF_BLOCKS_IN_MEMORY, i);
	    	Tuple_with_position tuple_with_p = new Tuple_with_position(mem.getBlock(i).getTuple(0),i,0,0);
			heap1.insert(tuple_with_p);
    	}
    	for(int i=0; i<sb_num2; i++){//put the first block of each sublist of relation2 into memory, starting from sublistNum1
	    	r2.getBlock(i*Config.NUM_OF_BLOCKS_IN_MEMORY, i+sb_num1);
	    	Tuple_with_position tuple_with_p = new Tuple_with_position(mem.getBlock(i+sb_num1).getTuple(0),i+sb_num1,0,0);
			heap2.insert(tuple_with_p);
    	}
    	while(heap1.size>0 && heap2.size>0){
	    	Tuple_with_position t_p_1 = heap1.pop_min();
			Tuple_with_position t_p_2 = heap2.pop_min();
			heap1.insert(t_p_1);
			heap2.insert(t_p_2);
			
			if(t_p_1.tuple.getField(attr).integer>t_p_2.tuple.getField(attr).integer){
				Tuple_with_position t_p_temp=heap2.pop_min();
				if(t_p_temp.tuple_pointer<mem.getBlock(t_p_temp.sublist_pointer).getNumTuples()-1){
					Tuple tuple=mem.getBlock(t_p_temp.sublist_pointer).getTuple(t_p_temp.tuple_pointer+1);
					heap2.insert(new Tuple_with_position(tuple,t_p_temp.sublist_pointer,t_p_temp.block_pointer,t_p_temp.tuple_pointer+1));
				}
				else if(t_p_temp.block_pointer<9 && (t_p_temp.sublist_pointer-sb_num1)*10+t_p_temp.block_pointer<r2_blocks-1){
				    t_p_temp.block_pointer++;
				    r2.getBlock((t_p_temp.sublist_pointer-sb_num1)*10+t_p_temp.block_pointer,t_p_temp.sublist_pointer);
				    heap2.insert(new Tuple_with_position(mem.getBlock(t_p_temp.sublist_pointer).getTuple(0),t_p_temp.sublist_pointer,t_p_temp.block_pointer,0));
				}
			}
			else if(t_p_1.tuple.getField(attr).integer<t_p_2.tuple.getField(attr).integer){
				Tuple_with_position t_p_temp=heap1.pop_min();
				if(t_p_temp.tuple_pointer<mem.getBlock(t_p_temp.sublist_pointer).getNumTuples()-1){
					Tuple tuple=mem.getBlock(t_p_temp.sublist_pointer).getTuple(t_p_temp.tuple_pointer+1);
					heap1.insert(new Tuple_with_position(tuple,t_p_temp.sublist_pointer,t_p_temp.block_pointer,t_p_temp.tuple_pointer+1));
				}
				else if(t_p_temp.block_pointer<9 && t_p_temp.sublist_pointer*10+t_p_temp.block_pointer<r1_blocks-1){
				    t_p_temp.block_pointer++;
				    r1.getBlock(t_p_temp.sublist_pointer*10+t_p_temp.block_pointer,t_p_temp.sublist_pointer);
				    heap1.insert(new Tuple_with_position(mem.getBlock(t_p_temp.sublist_pointer).getTuple(0),t_p_temp.sublist_pointer,t_p_temp.block_pointer,0));
				}
			}
			else{
				Tuple_with_position t_p_1_temp = heap1.pop_min();
				Tuple_with_position t_p_2_temp = heap2.pop_min();
				if(t_p_2_temp.tuple_pointer<mem.getBlock(t_p_2_temp.sublist_pointer).getNumTuples()-1){
					Tuple tuple=mem.getBlock(t_p_2_temp.sublist_pointer).getTuple(t_p_2_temp.tuple_pointer+1);
					heap2.insert(new Tuple_with_position(tuple,t_p_2_temp.sublist_pointer,t_p_2_temp.block_pointer,t_p_2_temp.tuple_pointer+1));
				}
				else if(t_p_2_temp.block_pointer<9 && (t_p_2_temp.sublist_pointer-sb_num1)*10+t_p_2_temp.block_pointer<r2_blocks-1){
				    t_p_2_temp.block_pointer++;
				    r2.getBlock((t_p_2_temp.sublist_pointer-sb_num1)*10+t_p_2_temp.block_pointer,t_p_2_temp.sublist_pointer);
				    heap2.insert(new Tuple_with_position(mem.getBlock(t_p_2_temp.sublist_pointer).getTuple(0),t_p_2_temp.sublist_pointer,t_p_2_temp.block_pointer,0));
				}
				if(t_p_1_temp.tuple_pointer<mem.getBlock(t_p_1_temp.sublist_pointer).getNumTuples()-1){
					Tuple tuple=mem.getBlock(t_p_1_temp.sublist_pointer).getTuple(t_p_1_temp.tuple_pointer+1);
					heap1.insert(new Tuple_with_position(tuple,t_p_1_temp.sublist_pointer,t_p_1_temp.block_pointer,t_p_1_temp.tuple_pointer+1));
				}
				else if(t_p_1_temp.block_pointer<9 && t_p_1_temp.sublist_pointer*10+t_p_1_temp.block_pointer<r1_blocks-1){
				    t_p_1_temp.block_pointer++;
				    r1.getBlock(t_p_1_temp.sublist_pointer*10+t_p_1_temp.block_pointer,t_p_1_temp.sublist_pointer);
				    heap1.insert(new Tuple_with_position(mem.getBlock(t_p_1_temp.sublist_pointer).getTuple(0),t_p_1_temp.sublist_pointer,t_p_1_temp.block_pointer,0));
				}
    	        Tuple tuple1=t_p_1_temp.tuple;
    	        Tuple tuple2=t_p_2_temp.tuple;
    	        Tuple njt=njr.createTuple();
    	        int t1_fields=tuple1.getNumOfFields();
    	        
    	        for(int k=0;k<t1_fields;k++){
	    	        if(tuple1.getField(k).type == FieldType.INT){
	    				njt.setField(k,tuple1.getField(k).integer);
	    			}
	    			else{
	    				njt.setField(k,tuple1.getField(k).str);
	    			}	
    	        }
    	        
    	        for(int n=0;n<tuple2.getNumOfFields();n++){
    	        	if(tuple2.getField(n).type == FieldType.INT){
        				njt.setField(n+t1_fields,tuple2.getField(n).integer);
        			}
        			else{
        				njt.setField(n+t1_fields,tuple2.getField(n).str);
        			}	
    	        }
    	        appendTupleToRelation(njr,mem,9,njt);
    	        Tuple_with_position pop1=heap1.pop_min();
    	        Tuple_with_position pop2=heap2.pop_min();
    	        Tuple compare_1=pop1.tuple;
    	        Tuple compare_2=pop2.tuple;
    	        heap1.insert(pop1);
    	        heap2.insert(pop2);
    	        
    	        while(heap1.size>0 && heap1.compare_tuple(tuple1, compare_1)==0){
					Tuple_with_position new_tp1 = heap1.pop_min();
					//process newTuplePlus1,tuplePlus2;
					Tuple new_tuple1=new_tp1.tuple;
					t1_fields=new_tuple1.getNumOfFields();
	    	        Tuple new_njt=njr.createTuple();
	    	        for(int k=0;k<t1_fields;k++){
	    	        if(new_tuple1.getField(k).type == FieldType.INT){
	    				new_njt.setField(k,new_tuple1.getField(k).integer);
	    			}
	    			else{
	    				new_njt.setField(k,new_tuple1.getField(k).str);
	    			}	
	    	        }
	    	        for(int n=0;n<tuple2.getNumOfFields();n++){
	    	        	if(tuple2.getField(n).type == FieldType.INT){
	        				new_njt.setField(n+t1_fields,tuple2.getField(n).integer);
	        			}
	        			else{
	        				new_njt.setField(n+t1_fields,tuple2.getField(n).str);
	        			}	
	    	        }
	    	        appendTupleToRelation(njr,mem,9,new_njt);
					
					
	    	        Tuple_with_position t_p_temp=new_tp1;
					if(t_p_temp.tuple_pointer<mem.getBlock(t_p_temp.sublist_pointer).getNumTuples()-1){
						Tuple tuple=mem.getBlock(t_p_temp.sublist_pointer).getTuple(t_p_temp.tuple_pointer+1);
						heap1.insert(new Tuple_with_position(tuple,t_p_temp.sublist_pointer,t_p_temp.block_pointer,t_p_temp.tuple_pointer+1));
					}
					else if(t_p_temp.block_pointer<9 && t_p_temp.sublist_pointer*10+t_p_temp.block_pointer<r1_blocks-1){
					    t_p_temp.block_pointer++;
					    r1.getBlock(t_p_temp.sublist_pointer*10+t_p_temp.block_pointer,t_p_temp.sublist_pointer);
					    heap1.insert(new Tuple_with_position(mem.getBlock(t_p_temp.sublist_pointer).getTuple(0),t_p_temp.sublist_pointer,t_p_temp.block_pointer,0));
					}
					if(heap1.size>0){
		    	        Tuple_with_position temp1=heap1.pop_min();
		    	        heap1.insert(temp1);
						compare_1=temp1.tuple;
					}
					
				}
    	        
    	        while(heap2.size>0 && heap2.compare_tuple(tuple2, compare_2)==0){
					Tuple_with_position new_tp2 = heap2.pop_min();
					//process newTuplePlus1,tuplePlus2;
					Tuple new_tuple2=new_tp2.tuple;
	    	        Tuple new_njt=njr.createTuple();
	    	        for(int k=0;k<t1_fields;k++){
	    	        if(tuple1.getField(k).type == FieldType.INT){
	    				new_njt.setField(k,tuple1.getField(k).integer);
	    			}
	    			else{
	    				new_njt.setField(k,tuple1.getField(k).str);
	    			}	
	    	        }
	    	        for(int n=0;n<new_tuple2.getNumOfFields();n++){
	    	        	if(new_tuple2.getField(n).type == FieldType.INT){
	        				new_njt.setField(n+t1_fields,new_tuple2.getField(n).integer);
	        			}
	        			else{
	        				new_njt.setField(n+t1_fields,new_tuple2.getField(n).str);
	        			}	
	    	        }
	    	        appendTupleToRelation(njr,mem,9,new_njt);
					
					
	    	        Tuple_with_position t_p_temp=new_tp2;
	    	        if(t_p_temp.tuple_pointer<mem.getBlock(t_p_temp.sublist_pointer).getNumTuples()-1){
						Tuple tuple=mem.getBlock(t_p_temp.sublist_pointer).getTuple(t_p_temp.tuple_pointer+1);
						heap2.insert(new Tuple_with_position(tuple,t_p_temp.sublist_pointer,t_p_temp.block_pointer,t_p_temp.tuple_pointer+1));
					}
					else if(t_p_temp.block_pointer<9 && (t_p_temp.sublist_pointer-sb_num1)*10+t_p_temp.block_pointer<r2_blocks-1){
					    t_p_temp.block_pointer++;
					    r2.getBlock((t_p_temp.sublist_pointer-sb_num1)*10+t_p_temp.block_pointer,t_p_temp.sublist_pointer);
					    heap2.insert(new Tuple_with_position(mem.getBlock(t_p_temp.sublist_pointer).getTuple(0),t_p_temp.sublist_pointer,t_p_temp.block_pointer,0));
					}
	    	        if(heap2.size>0){
	    	        Tuple_with_position temp2=heap2.pop_min();
	    	        heap2.insert(temp2);
					compare_2=temp2.tuple;
	    	        }
	    	        
	    	        
				}
		
			}
    	}
		return n_j_name;
		
	}
	
	private Relation distinct_second_pass(Relation return_relation, ArrayList<String> field_names){
    	Heap heap= new Heap(80,field_names);
    	int blocks = return_relation.getNumOfBlocks();
    	int num_sublists = 0;
    	if(blocks%Config.NUM_OF_BLOCKS_IN_MEMORY==0){
    		num_sublists = blocks/Config.NUM_OF_BLOCKS_IN_MEMORY;
    	}else{
    		num_sublists = blocks/Config.NUM_OF_BLOCKS_IN_MEMORY+1;
    	}
    	for(int i =0; i<num_sublists; i++){
    		return_relation.getBlock(i*Config.NUM_OF_BLOCKS_IN_MEMORY,i);
    	}
    	for(int i=0;i<num_sublists;i++){
			 Block tested_block=mem.getBlock(i);
			 Tuple tested_tuple=tested_block.getTuple(0);
			 Tuple_with_position tested_tuple_p=new Tuple_with_position(tested_tuple,i,0,0);
			 heap.insert(tested_tuple_p); 
		}
		if(schema_manager.relationExists("distinct_relation")){
			 schema_manager.deleteRelation("distinct_relation");
		} 
		
		Relation distinct_relation=schema_manager.createRelation("distinct_relation", return_relation.getSchema());
		Tuple_with_position output=heap.pop_min();
		Tuple output_tuple=output.tuple;
		heap.insert(output);
		
		appendTupleToRelation(distinct_relation, mem, 9, output_tuple);
		
		while(heap.size>0){
			Tuple_with_position tp = heap.pop_min();
			if(heap.compare_tuple(tp.tuple,output.tuple) !=0){
				appendTupleToRelation(distinct_relation,mem, 9, tp.tuple);
				output=tp;
			}
			if(tp.tuple_pointer<mem.getBlock(tp.sublist_pointer).getNumTuples()-1){
				Tuple tuple = mem.getBlock(tp.sublist_pointer).getTuple(tp.tuple_pointer+1);
				heap.insert(new Tuple_with_position(tuple,tp.sublist_pointer,tp.block_pointer,tp.tuple_pointer+1));
			}
			else if(tp.block_pointer<9 && tp.sublist_pointer*10+tp.block_pointer<blocks-1){
				tp.block_pointer++;
		    	return_relation.getBlock(tp.sublist_pointer*10+tp.block_pointer, tp.sublist_pointer);
		    	heap.insert(new Tuple_with_position(mem.getBlock(tp.sublist_pointer).getTuple(0),tp.sublist_pointer,tp.block_pointer,0));
			}
		}
		return distinct_relation;
    }
    
	private boolean where_judge(SubTreeNode ExTree, Tuple test_tuple){
		 if(ExTree==null) return true;
		 if(calculate(ExTree, test_tuple).equalsIgnoreCase("true")) return true;
		 else if(calculate(ExTree, test_tuple).equalsIgnoreCase("null")) System.out.println("Syntax Error!");
		 return false;
	}
	
	private String calculate(SubTreeNode ExTree, Tuple test_tuple){
		if(ExTree.left==null){
			return ExTree.operation;
	    }
		if(ExTree.right==null){
			return ExTree.operation;
	    }
		String left="false";
		String right="false";
		
		if(ExTree.left!=null){
			left=calculate(ExTree.left, test_tuple);
		}
		if(ExTree.right!=null){
		    right=calculate(ExTree.right,test_tuple);
		}
		if(ExTree.operation.equalsIgnoreCase("&")){
			if(left.equalsIgnoreCase("true")&&right.equalsIgnoreCase("true")){
				return "true";
			}
			else return "false";
		}
		else if(ExTree.operation.equalsIgnoreCase("|")){
			if(left.equalsIgnoreCase("true")||right.equalsIgnoreCase("true")){
				return "true";
			}
			else return "false";
		}
		else if(ExTree.operation.equalsIgnoreCase("=")){
			if(Pattern.matches("[0-9]",String.valueOf(left.charAt(0)))){
			     if(Pattern.matches("[0-9]", String.valueOf(right.charAt(0)))){
			  if(left.equalsIgnoreCase(right)) return "true";
			  else return "false";}
			     else if(Pattern.matches("[^0-9]", String.valueOf(right.charAt(0)))){
			    	 int lvalue=Integer.parseInt(left);
			    	 int rvalue=test_tuple.getField(right).integer;
			    	 if(lvalue==rvalue) return "true";
			    	 else return "false";
			     }
			}
			else if(Pattern.matches("[^0-9]",String.valueOf(left.charAt(0)))){
			     if(Pattern.matches("[0-9]", String.valueOf(right.charAt(0)))){
			    	 int rvalue=Integer.parseInt(right);
			    	 int lvalue=test_tuple.getField(left).integer;
			    	 if(lvalue==rvalue) return "true";
			    	 else return "false";
			     }
			     else if(Pattern.matches("[^0-9]", String.valueOf(right.charAt(0)))){
                     if(test_tuple.getSchema().fieldNameExists(right)) {
                    	 if(test_tuple.getSchema().fieldNameExists(left)){
                    		 if(test_tuple.getField(left).str!=null){
                    		if(test_tuple.getField(right).str.equalsIgnoreCase(test_tuple.getField(left).str)) return "true";
                    		else return "false";
                    		 }
                    		 else{
                    			 if(test_tuple.getField(right).integer==test_tuple.getField(left).integer) return "true";
                         		else return "false";
                    		 }
                    	 }
                    	 else{
                    		left=left.replaceAll("\"", "");
                    		if(test_tuple.getField(right).str.equalsIgnoreCase(left)) return "true";
                    		else return "false";
                    	 }
                     }
                     else if(!test_tuple.getSchema().fieldNameExists(right)){
                    	 if(test_tuple.getSchema().fieldNameExists(left)){
                    		 right=right.replaceAll("\"", "");
                    		 if(test_tuple.getField(left).str.equalsIgnoreCase(right)) return "true";
                    		 else return "false";
                    	 }
                    	 else{
                    		 if(left.equalsIgnoreCase(right)) return "true";
                    		 else return "false";
                    	 }
                     }
			     }
			}
		}
		else if(ExTree.operation.equalsIgnoreCase("<")){
			if(Pattern.matches("[^0-9]", String.valueOf(left.charAt(0)))){
				if(Pattern.matches("[^0-9]", String.valueOf(right.charAt(0)))){
			    if(test_tuple.getField(left).integer<test_tuple.getField(right).integer) return "true";
			    else return "false";
				}
				else if(Pattern.matches("[0-9]", String.valueOf(right.charAt(0)))){
					if(test_tuple.getField(left).integer<Integer.parseInt(right)) return "true";
				    else return "false";
				}
			}
			else if(Pattern.matches("[0-9]", String.valueOf(left.charAt(0)))){
				if(Pattern.matches("[^0-9]", String.valueOf(right.charAt(0)))){
				    if(Integer.parseInt(left)<test_tuple.getField(right).integer) return "true";
				    else return "false";
					}
					else if(Pattern.matches("[0-9]", String.valueOf(right.charAt(0)))){
						if(Integer.parseInt(left)<Integer.parseInt(right)) return "true";
					    else return "false";
					}
			}
		}
		else if(ExTree.operation.equalsIgnoreCase(">")){
			if(Pattern.matches("[^0-9]", String.valueOf(left.charAt(0)))){
				if(Pattern.matches("[^0-9]", String.valueOf(right.charAt(0)))){
			    if(test_tuple.getField(left).integer>test_tuple.getField(right).integer) return "true";
			    else return "false";
				}
				else if(Pattern.matches("[0-9]", String.valueOf(right.charAt(0)))){
					if(test_tuple.getField(left).integer>Integer.parseInt(right)) return "true";
				    else return "false";
				}
			}
			else if(Pattern.matches("[0-9]", String.valueOf(left.charAt(0)))){
				if(Pattern.matches("[^0-9]", String.valueOf(right.charAt(0)))){
				    if(Integer.parseInt(left)>test_tuple.getField(right).integer) return "true";
				    else return "false";
					}
					else if(Pattern.matches("[0-9]", String.valueOf(right.charAt(0)))){
						if(Integer.parseInt(left)>Integer.parseInt(right)) return "true";
					    else return "false";
					}
			}
		}
		else if(ExTree.operation.equalsIgnoreCase("+")  || ExTree.operation.equalsIgnoreCase("-")  || ExTree.operation.equalsIgnoreCase("*")  || ExTree.operation.equalsIgnoreCase("/")){
			if(Pattern.matches("[0-9]",String.valueOf(left.charAt(0)))){
				     if(Pattern.matches("[0-9]", String.valueOf(right.charAt(0)))){
				    	 int temp=0;
				    	 if(ExTree.operation.equalsIgnoreCase("+")){
				    	  temp=Integer.parseInt(left)+Integer.parseInt(right);}
				    	 else if(ExTree.operation.equalsIgnoreCase("-")){
					      temp=Integer.parseInt(left)-Integer.parseInt(right);}
				    	 else if(ExTree.operation.equalsIgnoreCase("*")){
					    	  temp=Integer.parseInt(left)*Integer.parseInt(right);}
				    	 else if(ExTree.operation.equalsIgnoreCase("/")){
					    	  temp=Integer.parseInt(left)/Integer.parseInt(right);}
				    	  return String.valueOf(temp);	             //如果是左边的是数字，那右边也得是数字或者字符
				     }
				     else if(Pattern.matches("[^0-9]", String.valueOf(right.charAt(0)))){
				    	  int right_value=test_tuple.getField(right).integer;
				    	  int temp=0;
					      if(ExTree.operation.equalsIgnoreCase("+")){
				    	  temp=Integer.parseInt(left)+right_value;}
					      else if(ExTree.operation.equalsIgnoreCase("-")){
					    	  temp=Integer.parseInt(left)-right_value;}
					      else if(ExTree.operation.equalsIgnoreCase("*")){
					    	  temp=Integer.parseInt(left)*right_value;}
					      else if(ExTree.operation.equalsIgnoreCase("/")){
					    	  temp=Integer.parseInt(left)/right_value;}
				    	  return String.valueOf(temp);
				     }
			}
			else if(Pattern.matches("[0-9]",String.valueOf(right.charAt(0)))){
			          if(Pattern.matches("[^0-9]", String.valueOf(left.charAt(0)))){
			    	  int left_value=test_tuple.getField(left).integer;
			    	  int temp=0;
				      if(ExTree.operation.equalsIgnoreCase("+")){
			    	  temp=Integer.parseInt(right)+left_value;}
				      else if(ExTree.operation.equalsIgnoreCase("-")){
				    	   temp=Integer.parseInt(right)-left_value;}
				      else if(ExTree.operation.equalsIgnoreCase("*")){
				    	   temp=Integer.parseInt(right)*left_value;}
				      else if(ExTree.operation.equalsIgnoreCase("/")){
				    	   temp=Integer.parseInt(right)/left_value;}
			    	  return String.valueOf(temp);
			     }
		    }
			else if(Pattern.matches("[^0-9]",String.valueOf(left.charAt(0)))&&Pattern.matches("[^0-9]",String.valueOf(right.charAt(0)))){
				int left_value=test_tuple.getField(left).integer;
				int right_value=test_tuple.getField(right).integer;
				int temp=0;
			      if(ExTree.operation.equalsIgnoreCase("+")){
		         temp=right_value+left_value;}
			      else if(ExTree.operation.equalsIgnoreCase("-")){
				         temp=right_value-left_value;}
			      else if(ExTree.operation.equalsIgnoreCase("*")){
				         temp=right_value*left_value;}
			      else if(ExTree.operation.equalsIgnoreCase("/")){
				         temp=right_value/left_value;}
		        return String.valueOf(temp);
			}
			
		}
		 return "null";
	}
	
    private Relation first_pass(Relation return_relation, ArrayList<String> field_names){
//		 if(return_relation.getSchema().getFieldNames().contains(field_names.get(0))){
//			 return return_relation;
//		 }
		 Heap heap=new Heap(80,field_names);
		 int num_blocks=return_relation.getNumOfBlocks();
		 int scan_times=0;
		 if(num_blocks%Config.NUM_OF_BLOCKS_IN_MEMORY==0)  scan_times=num_blocks/Config.NUM_OF_BLOCKS_IN_MEMORY;
		 else scan_times=num_blocks/Config.NUM_OF_BLOCKS_IN_MEMORY+1;
		 
		 for(int i=0;i<scan_times;i++){
			 if(num_blocks-Config.NUM_OF_BLOCKS_IN_MEMORY*i>=Config.NUM_OF_BLOCKS_IN_MEMORY){
				 
				 return_relation.getBlocks(i*Config.NUM_OF_BLOCKS_IN_MEMORY, 0, Config.NUM_OF_BLOCKS_IN_MEMORY);
				 for(int j=0;j<Config.NUM_OF_BLOCKS_IN_MEMORY;j++){
					 Block sort_block=mem.getBlock(j);
					 if(sort_block.isEmpty()) {continue;}
					 int d=sort_block.getNumTuples();
					 for(int k=0;k<sort_block.getNumTuples();k++){
						 Tuple sort_tuple=sort_block.getTuple(k);
						 if(sort_tuple.isNull())  {continue;}
						 Tuple_with_position tp=new Tuple_with_position(sort_tuple,0,0,0);
						 heap.insert(tp);
					 }
				 }
				 for(int j=0;j<Config.NUM_OF_BLOCKS_IN_MEMORY;j++){
					 Block sorted_block=mem.getBlock((j));
					 sorted_block.clear();
					 while(!sorted_block.isFull() && heap.size>0){
						 Tuple_with_position tp= heap.pop_min();
						 sorted_block.appendTuple(tp.tuple);
					    }
					}
				 return_relation.setBlocks(i*Config.NUM_OF_BLOCKS_IN_MEMORY, 0, Config.NUM_OF_BLOCKS_IN_MEMORY);
			 }
			 else{
				 return_relation.getBlocks(i*Config.NUM_OF_BLOCKS_IN_MEMORY, 0, num_blocks-i*Config.NUM_OF_BLOCKS_IN_MEMORY);
				 for(int j=0;j<num_blocks-i*Config.NUM_OF_BLOCKS_IN_MEMORY;j++){
					 Block sort_block=mem.getBlock(j);
					 if(sort_block.isEmpty()) {continue;}
					 for(int k=0;k<sort_block.getNumTuples();k++){
						 Tuple sort_tuple=sort_block.getTuple(k);
						 if(sort_tuple.isNull())  {continue;}
						 Tuple_with_position tp=new Tuple_with_position(sort_tuple,0,0,0);
						 heap.insert(tp);
					 }
				 }
				 for(int j=0;j<num_blocks-i*Config.NUM_OF_BLOCKS_IN_MEMORY;j++){
					 Block sorted_block=mem.getBlock((j));
					 sorted_block.clear();
					 while(!sorted_block.isFull() && heap.size>0){
						 Tuple_with_position tp= heap.pop_min();
						 sorted_block.appendTuple(tp.tuple);
					    }
					}
				 return_relation.setBlocks(i*Config.NUM_OF_BLOCKS_IN_MEMORY, 0, num_blocks-i*Config.NUM_OF_BLOCKS_IN_MEMORY); 
			 }
		 }
		 return return_relation;
	 }
	
	private static void appendTupleToRelation(Relation relation_reference, MainMemory mem, int memory_block_index, Tuple tuple) {
	    Block block_reference;
	    if (relation_reference.getNumOfBlocks()==0) {
//		      System.out.print("The relation is empty" + "\n");
//		      System.out.print("Get the handle to the memory block " + memory_block_index + " and clear it" + "\n");
	      block_reference=mem.getBlock(memory_block_index);
	      block_reference.clear(); //clear the block
	      block_reference.appendTuple(tuple); // append the tuple
//		      System.out.print("Write to the first block of the relation" + "\n");
	      relation_reference.setBlock(relation_reference.getNumOfBlocks(),memory_block_index);
	    } else {
//		      System.out.print("Read the last block of the relation into memory block 5:" + "\n");
	      relation_reference.getBlock(relation_reference.getNumOfBlocks()-1,memory_block_index);
	      block_reference=mem.getBlock(memory_block_index);

	      if (block_reference.isFull()) {
//		        System.out.print("(The block is full: Clear the memory block and append the tuple)" + "\n");
	        block_reference.clear(); //clear the block
	        block_reference.appendTuple(tuple); // append the tuple
//		        System.out.print("Write to a new block at the end of the relation" + "\n");
	        relation_reference.setBlock(relation_reference.getNumOfBlocks(),memory_block_index); //write back to the relation
	      } else {
//		        System.out.print("(The block is not full: Append it directly)" + "\n");
	        block_reference.appendTuple(tuple); // append the tuple
//		        System.out.print("Write to the last block of the relation" + "\n");
	        relation_reference.setBlock(relation_reference.getNumOfBlocks()-1,memory_block_index); //write back to the relation
	      }
	    }
    }
    

	

	
}
        
        
     

        
