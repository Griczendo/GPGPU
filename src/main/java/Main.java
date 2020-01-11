import org.lwjgl.LWJGLException;
import org.lwjgl.opencl.CL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException, LWJGLException {
        CL.create();
//        writeToFile("Concurrent:");
//        concurrentResults();
//        writeToFile("Sequential:");
//        sequentialResults();
        writeToFile("GPU:");
        gpuResults();
    }

    private static void sequentialResults() throws IOException {
        for(int i = 4; i <= 18; i++) {
            List<Integer> dataToBeSorted = generateRandomList((int)Math.pow(2, i));
            long startTime = System.nanoTime();
            SortingAlgorithmPORR.sortSequential(dataToBeSorted);
            long elapsedTime = System.nanoTime() - startTime;
            double elapsedTimeInMilis = ((double)elapsedTime)/1000000;
            writeToFile("" + elapsedTimeInMilis);
            System.out.println("Finished sequential, iteration: " + i);
        }
    }

    private static void concurrentResults() throws IOException {
        for(int i = 4; i <= 18; i++) {
            List<Integer> dataToBeSorted = generateRandomList((int)Math.pow(2, i));
            long startTime = System.nanoTime();
            SortingAlgorithmPORR.sortConcurrent(dataToBeSorted);
            long elapsedTime = System.nanoTime() - startTime;
            double elapsedTimeInMilis = ((double)elapsedTime)/1000000;
            writeToFile("" + elapsedTimeInMilis);
            System.out.println("Finished concurrent, iteration: " + i);
        }
    }

    private static void gpuResults() throws IOException {
        for(int i = 4; i <= 18; i++) {
            int[] dataToBeSorted = generateRandomArray((int)Math.pow(2, i));
            long startTime = System.nanoTime();
            SortingAlgorithmPORR.sortGPU(dataToBeSorted);
            long elapsedTime = System.nanoTime() - startTime;
            double elapsedTimeInMilis = ((double)elapsedTime)/1000000;
            writeToFile("" + elapsedTimeInMilis);
            System.out.println("Finished gpu, iteration: " + i);
        }
    }

    public static List<Integer> generateRandomList(int dataListSize) {
        List<Integer> data = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < dataListSize; i++) {
            data.add(random.nextInt(3 * dataListSize));
        }
        return data;
    }

    public static int[] generateRandomArray(int dataListSize) {
        int[] array = new int[dataListSize];
        Random random = new Random();
        for (int i = 0; i < dataListSize; i++) {
            array[i] = random.nextInt(3 * dataListSize);
        }
        return array;
    }

    public static void writeToFile(String message) throws IOException {
        File file = new File("src/main/resources/results.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
        writer.append("" + message + "\n");
        writer.close();
    }


}
