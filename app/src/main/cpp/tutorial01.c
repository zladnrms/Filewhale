#include <jni.h>
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>

#define LOG_TAG "veaver"
#define LOGI(...) __android_log_print(4, LOG_TAG, __VA_ARGS__);
#define LOGE(...) __android_log_print(6, LOG_TAG, __VA_ARGS__);


jint Java_filewhalewebhard_defytech_wmqkem_filewhalewebhard_App_1cpptest_ffmpeg(JNIEnv* env, jobject thiz, int argc, jstring filepath) {

	const char* nativeFilepath = (*env)->GetStringUTFChars( env, filepath , NULL ) ;

	avcodec_register_all(); // 코덱 등록

	AVFormatContext *pFormatCtx = NULL;

// Open video file
	if(avformat_open_input(&pFormatCtx, nativeFilepath, NULL, NULL)!=0)
		return -1; // Couldn't open file

	return 0;
}

void Java_filewhalewebhard_defytech_wmqkem_filewhalewebhard_App_1cpptest_callJava(JNIEnv* env, jobject thiz){
	jclass jCallJava = (*env)->FindClass(env, "filewhalewebhard/defytech/wmqkem/filewhalewebhard/App_cpptest");
	//jclass jCallJava = (*env)->GetObjectClass(env, thiz);

	jmethodID testToast = (*env)->GetStaticMethodID(env, jCallJava, "testToast", "()V");
	(*env)->CallStaticVoidMethod(env, jCallJava, testToast);
}

jstring Java_filewhalewebhard_defytech_wmqkem_filewhalewebhard_App_1cpptest_printString(JNIEnv* env, jobject thiz){
	return (*env)->NewStringUTF(env, "FFMPEG 테스트중 By Cfile");
}