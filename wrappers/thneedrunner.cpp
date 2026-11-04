#include <jni.h>
#include <dirent.h>

#include "selfdrive/modeld/thneed/thneed.h"
#include "selfdrive/modeld/models/driving.h"
#include "common/timing.h"

#include "android/log.h"

#define  LOG_TAG    "thneedrunner"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

#define MODEL_OUTPUT_SIZE 6504
#define PREV_DESIRED_CURVS_LEN (HISTORY_BUFFER_LEN + 1)
float zero_buf[1024/4] = {0};
float features_buf[HISTORY_BUFFER_LEN * FEATURE_LEN] = {0};
float prev_curvs_buf[PREV_DESIRED_CURVS_LEN] = {0};
PubMaster pm({"modelV2", "cameraOdometry"});
Thneed* thneed;
bool record;

extern "C" {

void JNICALL Java_ai_flow_android_vision_THNEEDModelRunner_loadModel(JNIEnv *env, jobject obj, jstring model_path) {    
    // Hack to point to kgsl
    int fd = open("/dev/kgsl-3d0", O_RDWR, 0);
    ioctl(fd, IOCTL_KGSL_GPUOBJ_ALLOC, NULL);
    // End hack
    thneed = new Thneed(true, NULL);

    const char* path = env->GetStringUTFChars(model_path, NULL);
    thneed->load(path);
}

JNIEXPORT void JNICALL Java_ai_flow_android_vision_THNEEDModelRunner_executeAndPublish(
        JNIEnv *env,
        jobject obj,
        jfloatArray input,
        jint last_frame
    ) {
    uint64_t start_t = nanos_since_boot();

    // buffers
    jfloat *input_buf = env->GetFloatArrayElements(input, 0);

    // useful offsets
    int input_imgs_len = 1572864 / 4;
    int desire_len = 3200 / 4;

    float* input_imgs_buf = &input_buf[0];
    float* big_input_imgs_buf = &input_buf[input_imgs_len];
    float* desire_buf = &input_buf[input_imgs_len * 2];
    float* lat_params = &input_buf[input_imgs_len * 2 + desire_len];
    float outputs[MODEL_OUTPUT_SIZE];

    float* inputs[] = {
      features_buf,
      zero_buf,
      zero_buf,
      prev_curvs_buf,
      lat_params,
      zero_buf,
      desire_buf,
      big_input_imgs_buf,
      input_imgs_buf,
    };

    thneed->copy_inputs(inputs, false);
    thneed->clexec();
    thneed->copy_output(outputs);

    // When done, release the memory
    env->ReleaseFloatArrayElements(input, input_buf, 0);

    // handle features
    std::memmove(&features_buf[0], &features_buf[FEATURE_LEN], sizeof(float) * FEATURE_LEN*(HISTORY_BUFFER_LEN-1));
    std::memcpy(&features_buf[FEATURE_LEN*(HISTORY_BUFFER_LEN-1)], &outputs[OUTPUT_SIZE], sizeof(float) * FEATURE_LEN);

    // handle previous curves
    std::memmove(&prev_curvs_buf[0], &prev_curvs_buf[1], PREV_DESIRED_CURVS_LEN - 1);
    prev_curvs_buf[PREV_DESIRED_CURVS_LEN - 1] = outputs[5990];

    // get the outputs
    model_publish(pm, last_frame, last_frame, last_frame, 0, 
              outputs, 0, (nanos_since_boot()-start_t)/1000, true);
    posenet_publish(pm, last_frame, 0, outputs, 0, true);
}

}