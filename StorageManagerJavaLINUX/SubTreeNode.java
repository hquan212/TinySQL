import java.util.ArrayList;
import java.util.Stack;

/*
    created 11/2/2018
*/

public class SubTreeNode{
    
    //Left tree -- Right Tree -- operation
    SubTreeNode left;
    SubTreeNode right;
    String operation;
    
    
    public SubTreeNode(){
        left = null;
        right = null;
        operation = null;
    }
    public SubTreeNode(SubTreeNode _left, SubTreeNode _right, String _operation){
        this.left = _left;
        this.right = _right;
        this.operation = _operation;
    }
    public SubTreeNode(String _operation){
        this.operation = _operation;
        left = null;
        right = null;
    }
    
        public ArrayList<SubTreeNode> hasSelection(){
        if(right ==null){
            return null;
        }

        if((Character.isDigit(this.left.operation.charAt(0)) || Character.isLetter(this.left.operation.charAt(0))) && (Character.isDigit(this.right.operation.charAt(0)) || Character.isLetter(this.right.operation.charAt(0)))){
            ArrayList<SubTreeNode> res = new ArrayList<>();
            res.add(this);
            return res;
        }

        ArrayList<SubTreeNode> left_ex = this.left.hasSelection();
        ArrayList<SubTreeNode> right_ex = this.right.hasSelection();

        if (left_ex!=null){
            if(right_ex!=null){
                left_ex.addAll(right_ex);
            }
            return left_ex;
        }else{
            if(right_ex!=null){
                return right_ex;
            }
            return null;
        }
    }

}