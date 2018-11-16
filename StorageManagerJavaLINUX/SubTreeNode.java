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
}