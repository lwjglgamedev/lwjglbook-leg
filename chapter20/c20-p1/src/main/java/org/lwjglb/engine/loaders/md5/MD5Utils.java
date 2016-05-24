package org.lwjglb.engine.loaders.md5;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MD5Utils {

    public static final String FLOAT_REGEXP = "[+-]?\\d*\\.?\\d*";

    public static final String VECTOR3_REGEXP = "\\(\\s*(" + FLOAT_REGEXP + ")\\s*(" + FLOAT_REGEXP + ")\\s*(" + FLOAT_REGEXP + ")\\s*\\)";

    private MD5Utils() {
    }

    public static Quaternionf calculateQuaternion(Vector3f vec) {
        return calculateQuaternion(vec.x, vec.y, vec. z);
    }

    public static Quaternionf calculateQuaternion(float x, float y, float z) {
        Quaternionf orientation = new Quaternionf(x, y, z, 0);
        float temp = 1.0f - (orientation.x * orientation.x) - (orientation.y * orientation.y) - (orientation.z * orientation.z);
        if (temp < 0.0f) {
            orientation.w = 0.0f;
        } else {
            orientation.w = -(float) (Math.sqrt(temp));
        }
        return orientation;
    }
}
