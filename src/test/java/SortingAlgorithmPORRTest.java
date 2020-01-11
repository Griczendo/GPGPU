import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SortingAlgorithmPORRTest {

    @Test
    public void testSortingGPU() throws IOException {
        int[] sortedData = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] dataToBeSorted = new int[] {3, 1, 7, 5, 4, 2, 8, 6, 9, 0};

        dataToBeSorted = SortingAlgorithmPORR.sortGPU(dataToBeSorted);

        Assert.assertEquals(sortedData.length, dataToBeSorted.length);
        Assert.assertTrue(Arrays.equals(sortedData, dataToBeSorted));
    }

    @Test
    public void testSortingConcurrent() {
        List<Integer> sortedData = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        List<Integer> dataToBeSorted = generateFrom0to9();

        SortingAlgorithmPORR.sortConcurrent(dataToBeSorted);

        Assert.assertEquals(sortedData.size(), dataToBeSorted.size());
        Assert.assertEquals(sortedData, dataToBeSorted);
    }

    @Test
    public void testSortingConcurrentLessThanAvailableThreads() {
        List<Integer> sortedData = Arrays.asList(0, 1, 2, 3);
        List<Integer> dataToBeSorted = new ArrayList<>();
        dataToBeSorted.add(1);dataToBeSorted.add(3);
        dataToBeSorted.add(2);dataToBeSorted.add(0);

        SortingAlgorithmPORR.sortConcurrent(dataToBeSorted);

        Assert.assertEquals(sortedData.size(), dataToBeSorted.size());
        Assert.assertEquals(sortedData, dataToBeSorted);
    }

    @Test
    public void testSortingSequential() {
        List<Integer> sortedData = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        List<Integer> dataToBeSorted = generateFrom0to9();

        SortingAlgorithmPORR.sortSequential(dataToBeSorted);

        Assert.assertEquals(sortedData.size(), dataToBeSorted.size());
        Assert.assertEquals(sortedData, dataToBeSorted);
    }

    public static List<Integer> generateFrom0to9() {
        List<Integer> data = new ArrayList<>();
        data.add(1);data.add(3);data.add(7);data.add(5);data.add(4);
        data.add(2);data.add(8);data.add(6);data.add(9);data.add(0);
        return data;
    }
}
