import java.util.ArrayList;

/*
  RQ, XT, VS
 */
public class TreeNode {
    public boolean distinct;
    public boolean where;
    public ArrayList<String> table_names;
    public SubTreeNode w_clause;
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