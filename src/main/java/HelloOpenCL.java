import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.*;

import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.opencl.CL10.*;

public class HelloOpenCL {

    public static void main(String[] args) throws Exception {
        final IntBuffer aBuffer = BufferUtils.createIntBuffer(10).put(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        aBuffer.rewind();
        final IntBuffer bBuffer = BufferUtils.createIntBuffer(10).put(new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1, 0});
        bBuffer.rewind();
        final IntBuffer answerBuffer = BufferUtils.createIntBuffer(aBuffer.capacity());
        // Initialize OpenCL and create a context and command queue
        CL.create();

        CLPlatform platform = CLPlatform.getPlatforms().get(0);
        List<CLDevice> devices = platform.getDevices(CL_DEVICE_TYPE_GPU);
        CLContext context = CLContext.create(platform, devices, null, null, null);
        CLCommandQueue queue = clCreateCommandQueue(context, devices.get(0), CL_QUEUE_PROFILING_ENABLE, null);

        // Allocate memory for our two input buffers and our result buffer
        CLMem aMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, aBuffer, null);
        clEnqueueWriteBuffer(queue, aMem, 1, 0, aBuffer, null, null);
        CLMem bMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, bBuffer, null);
        clEnqueueWriteBuffer(queue, bMem, 1, 0, bBuffer, null, null);
        CLMem answerMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, answerBuffer, null);
        clFinish(queue);

        // Load the source from a resource file
        String source = UtilCL.getResourceAsString("sort.txt");

        // Create our program and kernel
        CLProgram program = clCreateProgramWithSource(context, source, null);
        Util.checkCLError(clBuildProgram(program, devices.get(0), "", null));
        // sum has to match a kernel method name in the OpenCL source
        CLKernel kernel = clCreateKernel(program, "sum", null);

        // Execution our kernel
        PointerBuffer kernel1DGlobalWorkSize = BufferUtils.createPointerBuffer(1);
        kernel1DGlobalWorkSize.put(0, aBuffer.capacity());
        kernel.setArg(0, aMem);
        kernel.setArg(1, bMem);
        kernel.setArg(2, answerMem);
        clEnqueueNDRangeKernel(queue, kernel, 1, null, kernel1DGlobalWorkSize, null, null, null);

        // Read the results memory back into our result buffer
        clEnqueueReadBuffer(queue, answerMem, 1, 0, answerBuffer, null, null);
        clFinish(queue);
        // Print the result memory
        print(aBuffer);
        System.out.println("+");
        print(bBuffer);
        System.out.println("=");
        print(answerBuffer);

        // Clean up OpenCL resources
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseMemObject(aMem);
        clReleaseMemObject(bMem);
        clReleaseMemObject(answerMem);
        clReleaseCommandQueue(queue);
        clReleaseContext(context);
        CL.destroy();
    }

    public static void print(IntBuffer buffer) {
        for (int i = 0; i < buffer.capacity(); i++) {
            System.out.print(buffer.get(i) + " ");
        }
        System.out.println("");
    }
}
