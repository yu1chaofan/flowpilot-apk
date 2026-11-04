package ai.flow.app;

import ai.flow.modeld.CommonModelF3;
import ai.flow.modeld.LeadDataV3;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

public class Draw {
    /**
     * Scale to convert raw net outputs to meters.
     */
    public static final float LEAD_X_SCALE = 10f;
    public static final float LEAD_Y_SCALE = 10f;

    public static Matrix4 cam_intrinsics = new Matrix4(new float[] {
            930.f,  0.0f, 0.0f, 0.f,
            0.0f, 930.f, 0.0f, 0.f,
            640.0f, 360.0f, 1.0f, 0.f,
            0.f, 0.f, 0.f, 0.f
    });

    public static Vector3[][] getLaneCameraFrame(ArrayList<float[]> lane, Matrix4 K, Matrix4 Rt, float width) {
        // Camera assumes x left, y up, z forward
        Vector3[] projected_left_edge = new Vector3[CommonModelF3.TRAJECTORY_SIZE];
        Vector3[] projected_right_edge = new Vector3[CommonModelF3.TRAJECTORY_SIZE];

        for (int i = 0; i < CommonModelF3.TRAJECTORY_SIZE; i++) {
            float x = lane.get(1)[i];
            float y = lane.get(2)[i];
            float z = lane.get(0)[i];

            projected_left_edge[i] = projectToCamera(new Vector3(x - width / 2.0f, y, z), K, Rt);
            projected_right_edge[i] = projectToCamera(new Vector3(x + width / 2.0f, y, z), K, Rt);
        }

        return new Vector3[][]{projected_left_edge, projected_right_edge};
    }

    public static Vector3 projectToCamera(Vector3 worldCoords, Matrix4 K, Matrix4 Rt) {
        Matrix4 projection_matrix = new Matrix4(K).mul(Rt);
        Vector3 projected = new Vector3(worldCoords).mul(projection_matrix);
        projected.scl(1f / projected.z); // normalize projected coordinates by W

        return new Vector3(projected.x, projected.y, 0); // drop W
    }

    public static Vector3[] getTriangleCameraFrame(LeadDataV3 lead, Matrix4 K, Matrix4 Rt, float scale) {
        Vector3[] triangle = new Vector3[3];
        triangle[0] = projectToCamera(new Vector3(lead.y[0] * LEAD_Y_SCALE, 1.32f, lead.x[0] * LEAD_X_SCALE), K, Rt);
        triangle[1] = projectToCamera(new Vector3(lead.y[0] * LEAD_Y_SCALE - scale, 1.32f + scale, lead.x[0] * LEAD_X_SCALE), K, Rt);
        triangle[2] = projectToCamera(new Vector3(lead.y[0] * LEAD_Y_SCALE + scale, 1.32f + scale, lead.x[0] * LEAD_X_SCALE), K, Rt);

        return triangle;
    }
}