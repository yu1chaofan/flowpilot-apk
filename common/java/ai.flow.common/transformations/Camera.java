package ai.flow.common.transformations;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import ai.flow.common.utils;

public class Camera {

    public static final boolean FORCE_TELE_CAM_F3 = false;

    public static float
        FocalX = 930f,
        FocalY = 930f,
        CenterX = 640f, //632
        CenterY = 360f; //358

    // Camera 2
   // MATRIX: [538.2648047477589, 0, 635.4029785884212;
//0, 538.3225487046863, 348.6366566852139;
 //0, 0, 1]

    public static int UseCameraID = 0;

    // everything autocalculated below
    public static float actual_cam_focal_length = (FocalX + FocalY) * 0.5f;
    public static float digital_zoom_apply = 1f; //actual_cam_focal_length / (utils.F2 ? Model.MEDMODEL_F2_FL : Model.MEDMODEL_FL);
    public static final int[] frameSize = new int[]{1280, 720};
    public static float OffsetX = CenterX - (frameSize[0]*0.5f);
    public static float OffsetY = CenterY - (frameSize[1]*0.5f);

    public static float[] CameraIntrinsics = {
            FocalX, 0.0f, frameSize[0] * 0.5f + OffsetX * digital_zoom_apply,
            0.0f, FocalY, frameSize[1] * 0.5f + OffsetY * digital_zoom_apply,
            0.0f,   0.0f, 1.0f
    };

    // everything auto-generated from above
    public static final int CAMERA_TYPE_ROAD = 0;
    public static final int CAMERA_TYPE_WIDE = 1;
    public static final int CAMERA_TYPE_DRIVER = 2;
    public static INDArray cam_intrinsics = Nd4j.createFromArray(new float[][]{
            { CameraIntrinsics[0],  0.0f,  CameraIntrinsics[2]},
            {0.0f,  CameraIntrinsics[4],  CameraIntrinsics[5]},
            {0.0f,  0.0f,  1.0f}
    });

    public static final INDArray view_from_device = Nd4j.createFromArray(new float[][]{
            {0.0f,  1.0f,  0.0f},
            {0.0f,  0.0f,  1.0f},
            {1.0f,  0.0f,  0.0f}
    });

    // Camera #0 (telephoto)
    /*public static final float[] WideIntrinsics = {
            910f,   0.0f, 900f,
            0.0f,   910f, 514f,
            0.0f,   0.0f, 1.0f
    };*/
}
