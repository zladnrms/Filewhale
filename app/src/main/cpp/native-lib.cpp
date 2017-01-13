#include <jni.h>
#include <string>

extern "C"
jstring Java_filewhalewebhard_defytech_wmqkem_filewhalewebhard_App_1cpptest_stringFromJNI( JNIEnv *env, jobject /* this */) {
    std::string hello = "헬로 월드 ";
    return env->NewStringUTF(hello.c_str());
}

