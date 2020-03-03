package main.amazed.solver;

import amazed.maze.Maze;

import java.util.*;
import java.util.concurrent.*;

/**
 * <code>ForkJoinSolver</code> implements a solver for
 * <code>Maze</code> objects using a fork/join multi-thread
 * depth-first search.
 * <p>
 * Instances of <code>ForkJoinSolver</code> should be run by a
 * <code>ForkJoinPool</code> object.
 */


public class ForkJoinSolver
        extends amazed.solver.SequentialSolver
{
    Maze maze;
    Set<ForkJoinSolver> treds = new ConcurrentSkipListSet<>();
    Integer current;

    @Override
    protected void initStructures() {
        visited = new ConcurrentSkipListSet<>();
        predecessor = new ConcurrentSkipListMap<>();
        //frontier = new Stack<Integer>();
    }

    /**
     * Creates a solver that searches in <code>maze</code> from the
     * start node to a goal.
     *
     * @param maze   the maze to be searched
     */
    public ForkJoinSolver(Maze maze)
    {
        super(maze);
        this.maze = maze;
        current = start;
    }

    /**
     * Creates a solver that searches in <code>maze</code> from the
     * start node to a goal, forking after a given number of visited
     * nodes.
     *
     * @param maze        the maze to be searched
     */
    /*public ForkJoinSolver(Maze maze, int forkAfter)
    {
        this(maze);
        this.forkAfter = forkAfter;
    }*/


    public ForkJoinSolver(Maze maze, int current) {
        this(maze);
        this.current = current;
    }

    /**
     * Searches for and returns the path, as a list of node
     * identifiers, that goes from the start node to a goal node in
     * the maze. If such a path cannot be found (because there are no
     * goals, or all goals are unreacheable), the method returns
     * <code>null</code>.
     *
     * @return   the list of node identifiers from the start node to a
     *           goal node in the maze; <code>null</code> if such a path cannot
     *           be found.
     */
    @Override
    public List<Integer> compute()
    {
        return parallelSearch();
    }



    private List<Integer> parallelSearch(){
        return parallelSearch(current);
    }

    private List<Integer> parallelSearch(int current){
        Set<Integer> neighbours = maze.neighbors(current);

        if(maze.hasGoal(current)){
            return pathFromTo(current, start);
        }
        if(neighbours.size() > 1){
            //spawn thread
            for (Integer next : neighbours){
                if(!visited.contains(next)) {break;}
                visited.add(next);
                treds.add((ForkJoinSolver) new ForkJoinSolver(maze, next).fork());
            }
        } else if(neighbours.size() == 1){
            return parallelSearch(neighbours.iterator().next());
        }
        else{
            return null;
        }

        for(ForkJoinSolver tred : treds) {
            List<Integer> res = tred.join();
            if(res != null)
                return res;
        }
        return null;
    }
}
