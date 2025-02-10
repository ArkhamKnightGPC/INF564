package mini_java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class InheritanceDAG {
    /* In this class, we will build a graph with class inheritance relations
     * 1) we must check that this graph is a DAG
     * 2) we perform TOPOSORT to get a processing order of our classes
     */

    public class node{
        Class_ class_; //each node in DAG corresponds to a class
        ArrayList<node> adjNodes;
        int indeg, indeg_copy;

        node(Class_ class_){
            this.class_ = class_;
            adjNodes = new ArrayList<node>();
            indeg = 0; indeg_copy = 0;
        }
    }

    ArrayList<node> nodes;
    HashMap<Class_, Integer> nodes_idx;

    InheritanceDAG(){
        nodes = new ArrayList<node>();
        nodes_idx = new HashMap<Class_, Integer>();
    }

    void addNode(Class_ c){
        node n = new node(c);
        this.nodes_idx.put(c, this.nodes.size());
        this.nodes.add(n);
    }

    node getNode(Class_ c){
        int n_idx = this.nodes_idx.get(c);
        return this.nodes.get(n_idx);
    }

    void addEdge(Class_ c1, Class_ c2){
        node n1 = getNode(c1);
        node n2 = getNode(c2);

        n1.adjNodes.add(n2); //add directed edge n1 -> n2
        n2.indeg += 1; n2.indeg_copy += 1;
    }

    ArrayList<Class_> topoSort(){

        LinkedList<node> zeroIndeg = new LinkedList<node>();
        ArrayList<Class_> classOrdering = new ArrayList<Class_>();

        for(int i=0; i < this.nodes.size(); i++){
            node n = this.nodes.get(i);
            if(n.indeg == 0){
                zeroIndeg.add(n);
            }
        }

        while (zeroIndeg.isEmpty() == false) {

            node n = zeroIndeg.remove();
            classOrdering.add(n.class_);

            for(int i=0; i<n.adjNodes.size(); i++){
                node neighbour = n.adjNodes.get(i);
                neighbour.indeg_copy -= 1;
                if(neighbour.indeg_copy == 0){
                    zeroIndeg.addLast(neighbour);
                }
            }  
        }

        for(int i=0; i < this.nodes.size(); i++){
            node n = this.nodes.get(i);
            n.indeg_copy = n.indeg; //restore indeg copy...
        }

        return classOrdering;
    }
    
}
