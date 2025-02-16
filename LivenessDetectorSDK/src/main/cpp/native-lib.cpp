#include <jni.h>
#include <string>

extern "C" [[maybe_unused]] JNIEXPORT jstring JNICALL
Java_com_example_newlivenessdetector_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
