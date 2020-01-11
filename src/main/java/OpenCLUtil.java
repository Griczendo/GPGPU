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
     * @param start - inclusive
     * @param end - exclusive
     */
    public static IntBuffer sort(IntBuffer dataBuffer, final int[] start, final int[] end) throws LWJGLException, IOException {
        dataBuffer.rewind();
        IntBuffer startBuffer = BufferUtils.createIntBuffer(start.length).put(start);
        startBuffer.rewind();
        IntBuffer endBuffer = BufferUtils.createIntBuffer(end.length).put(end);
        endBuffer.rewind();

        CLPlatform platform = CLPlatform.getPlatforms().get(1);
        List<CLDevice> devices = platform.getDevices(CL_DEVICE_TYPE_GPU);
        CLContext context = CLContext.create(platform, devices, null, null, null);
        CLCommandQueue queue = clCreateCommandQueue(context, devices.get(0), CL_QUEUE_PROFILING_ENABLE, null);

        // Allocate memory for our two input buffers and our result buffer
        CLMem dataMem = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, dataBuffer, null);
        clEnqueueWriteBuffer(queue, dataMem, 1, 0, dataBuffer, null, null);

        CLMem startMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, startBuffer, null);
        clEnqueueWriteBuffer(queue, startMem, 1, 0, startBuffer, null, null);

        CLMem endMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, endBuffer, null);
        clEnqueueWriteBuffer(queue, endMem, 1, 0, endBuffer, null, null);

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
        kernel.setArg(1, startMem);
        kernel.setArg(2, endMem);
        clEnqueueNDRangeKernel(queue, kernel, 1, null, kernel1DGlobalWorkSize, null, null, null);

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
            clReleaseMemObject(startMem);
            clReleaseMemObject(endMem);
            clReleaseCommandQueue(queue);
            clReleaseContext(context);
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
