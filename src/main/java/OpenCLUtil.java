import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opencl.CL10.*;

public class OpenCLUtil {

    /**
     *
     * @param dataBuffer
     */
    public static IntBuffer sort(IntBuffer dataBuffer) throws LWJGLException, IOException {
        int dataBufferCapacity = dataBuffer.capacity();
        dataBuffer.rewind();

        IntBuffer startOddBuffer = BufferUtils.createIntBuffer(1).put(new int[] {0});
        startOddBuffer.rewind();
        IntBuffer endOddBuffer = BufferUtils.createIntBuffer(1).put(new int[] {dataBufferCapacity});
        endOddBuffer.rewind();
        IntBuffer startEvenBuffer = BufferUtils.createIntBuffer(1).put(new int[] {1});
        startEvenBuffer.rewind();
        IntBuffer endEvenBuffer = BufferUtils.createIntBuffer(1).put(new int[] {dataBufferCapacity - 1});
        endEvenBuffer.rewind();


        CL.create();
        CLPlatform platform = CLPlatform.getPlatforms().get(1);
        List<CLDevice> devices = platform.getDevices(CL_DEVICE_TYPE_GPU);
        CLContext context = CLContext.create(platform, devices, null, null, null);
        CLCommandQueue queue = clCreateCommandQueue(context, devices.get(0), CL_QUEUE_PROFILING_ENABLE, null);

        // Allocate memory for our two input buffers and our result buffer
        CLMem dataMem = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, dataBuffer, null);
        clEnqueueWriteBuffer(queue, dataMem, 1, 0, dataBuffer, null, null);

        CLMem startOddMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, startOddBuffer, null);
        clEnqueueWriteBuffer(queue, startOddMem, 1, 0, startOddBuffer, null, null);

        CLMem endOddMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, endOddBuffer, null);
        clEnqueueWriteBuffer(queue, endOddMem, 1, 0, endOddBuffer, null, null);

        CLMem startEvenMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, startEvenBuffer, null);
        clEnqueueWriteBuffer(queue, startEvenMem, 1, 0, startEvenBuffer, null, null);

        CLMem endEvenMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, endEvenBuffer, null);
        clEnqueueWriteBuffer(queue, endEvenMem, 1, 0, endEvenBuffer, null, null);

        clFinish(queue);

        // Load the source from a resource file
        String source = getResourceAsString("sort.txt");

        // Create our program and kernel
        CLProgram program = clCreateProgramWithSource(context, source, null);
        Util.checkCLError(clBuildProgram(program, devices.get(0), "", null));
        // sum has to match a kernel method name in the OpenCL source
        CLKernel kernel = clCreateKernel(program, "sort", null);

        // Execution our kernel
        PointerBuffer kernel1DGlobalWorkSize = BufferUtils.createPointerBuffer(1);
        kernel1DGlobalWorkSize.put(0, dataBuffer.capacity());
        kernel.setArg(0, dataMem);


        for(int i = 0; i < dataBufferCapacity; i++) {
            if(i % 2 == 0) {
                //ODD PHASE
                executeKernel(kernel, queue, startOddMem, endOddMem, kernel1DGlobalWorkSize);
            } else {
                //EVEN PHASE
                executeKernel(kernel, queue, startEvenMem, endEvenMem, kernel1DGlobalWorkSize);
            }
        }
        // Read the results memory back into our result buffer
        clEnqueueReadBuffer(queue, dataMem, 1, 0, dataBuffer, null, null);
        clFinish(queue);
        try {
            return dataBuffer;
        } finally {
            // Clean up OpenCL resources
            clReleaseKernel(kernel);
            clReleaseProgram(program);
            clReleaseMemObject(dataMem);
            clReleaseMemObject(startEvenMem);
            clReleaseMemObject(endOddMem);
            clReleaseCommandQueue(queue);
            clReleaseContext(context);
            CL.destroy();
        }
    }

    private static void executeKernel(
            CLKernel kernel,
            CLCommandQueue queue,
            CLMem startMem,
            CLMem endMem,
            PointerBuffer kernel1DGlobalWorkSize
    ) {
        kernel.setArg(1, startMem);
        kernel.setArg(2, endMem);
        clEnqueueNDRangeKernel(queue, kernel, 1, null, kernel1DGlobalWorkSize, null, null, null);
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
     * Read a resource into a string.
     * @param filePath The resource to read.
     * @return The resource as a string.
     * @throws IOException
     */
    public static String getResourceAsString(String filePath) throws IOException {
        InputStream is = OpenCLUtil.class.getClassLoader().getResourceAsStream(filePath);
        if (is == null) {
            throw new IOException("Can't find resource: " + filePath);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}
