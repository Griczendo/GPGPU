import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opencl.CL;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * Algorithm for comparing Integers.
 */
public class SortingAlgorithmPORR {

    /**
     * Sorting performed according to the algorithm from the task given
     * with GPU usage.
     * @param data - data list to be sorted
     */
    public static int[] sortGPU(int[] data) {
        IntBuffer dataBuffer = BufferUtils.createIntBuffer(data.length).put(data);
        try {
            CL.create();
            int dataSize = data.length;
            for(int i = 0; i < dataSize; i++) {
                if(i % 2 == 0) {
                    //ODD PHASE
                    OpenCLUtil.sort(dataBuffer, new int[] {0}, new int[] {data.length});
                } else {
                    //EVEN PHASE
                    OpenCLUtil.sort(dataBuffer, new int[] {1}, new int[] {data.length -1});
                }
            }
        } catch (LWJGLException e) {
            e.printStackTrace();
        } finally {
            CL.destroy();
            return bufferToArray(dataBuffer);
        }
    }

    private static int[] bufferToArray(IntBuffer b) {
        if(b.hasArray()) {
            if(b.arrayOffset() == 0)
                return b.array();

            return Arrays.copyOfRange(b.array(), b.arrayOffset(), b.array().length);
        }

        b.rewind();
        int[] foo = new int[b.remaining()];
        b.get(foo);

        return foo;
    }

    /**
     * Sorting performed according to the algorithm from the task given
     * in concurrent manner.
     * @param data - data list to be sorted
     */
    public static void sortConcurrent(List<Integer> data) {
        ForkJoinPool pool = new ForkJoinPool();
        performSortingAlogrithmConcurrent(pool, data);
    }

    /**
     * Sorting performed according to the algorithm from the task given
     * in sequential manner.
     * @param data - data list to be sorted
     */
    public static void sortSequential(List<Integer> data) {
        for(int i = 0; i < data.size(); i++) {
            if(i % 2 == 0) {
                //odd phase
                sortPartOfData(data, 0, data.size());
            } else {
                //even phase
                sortPartOfData(data, 1, data.size()-1);
            }
        }
    }

    private static void performSortingAlogrithmConcurrent(ForkJoinPool pool, List<Integer> data) {
        for(int i = 0; i < data.size(); i++) {
            if(i % 2 == 0) {
                oddPhase(data, pool);
            } else {
                evenPhase(data, pool);
            }
        }
    }

    private static void evenPhase(List<Integer> data, ForkJoinPool pool) {
        List<Integer> dataSublist = data.subList(1, data.size());

        SortingAction task = new SortingAction(dataSublist, 0, data.size());
        pool.invoke(task);
    }

    private static void oddPhase(List<Integer> data, ForkJoinPool pool) {
        SortingAction task = new SortingAction(data, 0, data.size());
        pool.invoke(task);
    }

    public static void sortPartOfData(List<Integer> data, int start, int end) {
        if(end % 2 == 0) {
            end = end - 1;
        }
        if(start % 2 != 0 && start != 1) {
            start = start - 1;
        }

        for(int i = start; limit(i, end, data); i+=2) {
            if(data.get(i) > data.get(i+1)) {
                int temp = data.get(i+1);
                data.set(i+1, data.get(i));
                data.set(i, temp);
            }
        }
    }

    private static boolean limit(int i, int end, List<Integer> data) {
        if(end == data.size()) {
            return i+1 < end;
        } else {
            return i+1 <= end;
        }
    }
}
