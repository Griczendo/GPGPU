import java.util.List;
import java.util.concurrent.RecursiveAction;

public class SortingAction extends RecursiveAction {

    private static final int THREADS_AVAILABLE = Runtime.getRuntime().availableProcessors();

    private List<Integer> data;
    private int start;
    private int end;
    private int dividingTreshold;

    public SortingAction(List<Integer> data, int start, int end) {
        this.data = data;
        this.start = start;
        this.end = end;
        this.dividingTreshold = data.size()/THREADS_AVAILABLE > 8 ? data.size()/THREADS_AVAILABLE : 8;
    }

    @Override
    protected void compute() {
        int length = end - start;
        if(length <= dividingTreshold) {
            //one thread is comparing pairs of his own part of data
            SortingAlgorithmPORR.sortPartOfData(data, start, end);
        } else {
            int mid = start + length / 2;

            SortingAction firstSubtask = new SortingAction(data, start, mid);
            SortingAction secondSubtask = new SortingAction(data, mid, end);
            // queue the first task
            firstSubtask.fork();
            // compute the second task
            secondSubtask.compute();
            // wait for the first task result
            firstSubtask.join();
        }
    }



}
