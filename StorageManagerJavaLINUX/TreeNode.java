import java.util.ArrayList;

/*
  Created  in 11/2/2018.
 */
public class TreeNode {
    public boolean distinct;
    public boolean where;
    public ArrayList<String> table_names;
    public ExTreeNode w_clause;
    public boolean order;
    public String o_clause;
    public ArrayList<String> argument;

    public TreeNode() {
        this.distinct = false;
        this.where = false;
        this.w_clause = null;
        this.order = false;
        this.o_clause = null;
        this.argument = new ArrayList<>();
        this.table_names = new ArrayList<>();
    }
}