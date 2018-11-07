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
        
        
        
        
        
        
        
        
        
        