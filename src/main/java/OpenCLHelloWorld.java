import org.lwjgl.LWJGLException;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL10GL;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLPlatform;

import java.util.List;
import java.util.Locale;

import static org.lwjgl.opencl.CL10.*;

public class OpenCLHelloWorld {


    public static void main(String[] args) throws LWJGLException {

        CL.create();
        displayInfo();
    }


    public static void displayInfo() {

        for (int platformIndex = 0; platformIndex < CLPlatform.getPlatforms().size(); platformIndex++) {
            CLPlatform platform = CLPlatform.getPlatforms().get(platformIndex);
            System.out.println("Platform #" + platformIndex + ":" + platform.getInfoString(CL_PLATFORM_NAME));
            List<CLDevice> devices = platform.getDevices(CL_DEVICE_TYPE_ALL);
            for (int deviceIndex = 0; deviceIndex < devices.size(); deviceIndex++) {
                CLDevice device = devices.get(deviceIndex);
                System.out.printf(Locale.ENGLISH, "Device #%d(%s):%s\n",
                        deviceIndex,
                        UtilCL.getDeviceType(device.getInfoInt(CL_DEVICE_TYPE)),
                        device.getInfoString(CL_DEVICE_NAME));
                System.out.printf(Locale.ENGLISH, "\tCompute Units: %d @ %d mghtz\n",
                        device.getInfoInt(CL_DEVICE_MAX_COMPUTE_UNITS), device.getInfoInt(CL_DEVICE_MAX_CLOCK_FREQUENCY));
                System.out.printf(Locale.ENGLISH, "\tWork-group max size: %d Work-item max size %d\n",
                        device.getInfoInt(CL_DEVICE_MAX_WORK_GROUP_SIZE), device.getInfoInt(CL_DEVICE_MAX_WORK_ITEM_SIZES));
                System.out.printf(Locale.ENGLISH, "\tLocal memory: %s\n",
                        UtilCL.formatMemory(device.getInfoLong(CL_DEVICE_LOCAL_MEM_SIZE)));
                System.out.printf(Locale.ENGLISH, "\tGlobal memory: %s\n",
                        UtilCL.formatMemory(device.getInfoLong(CL_DEVICE_GLOBAL_MEM_SIZE)));
                System.out.println();
            }
        }
    }
}
