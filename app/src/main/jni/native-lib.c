#include <jni.h>
#include <stddef.h>

// Convert temperature from Celsius to Fahrenheit. Used in MainActivity.
JNIEXPORT jint JNICALL
Java_com_wb_weatherbender_MainActivity_convertCToF(JNIEnv *env, jobject instance, jint cTemp) {
    return cTemp*9/5 + 32;
}

// Convert temperature from Celsius to Fahrenheit. Used in WeatherService.
JNIEXPORT jint JNICALL
Java_WeatherServiceProvider_WeatherService_convertCtoF(JNIEnv *env, jobject instance, jint cTemp) {
    return cTemp*9/5 + 32;
}

// Convert all temperatures in array from Celsius to Fahrenheit. Used in WeatherService.
JNIEXPORT jintArray JNICALL
Java_WeatherServiceProvider_WeatherService_convertCtoFArray(JNIEnv *env, jobject instance,
                                                       jintArray oldArray) {
    jsize len = (*env)->GetArrayLength(env, oldArray);
    jintArray newArray = (*env)->NewIntArray(env, len);
    if(newArray == NULL) {
        return NULL;
    }

    jint *before = (*env)->GetIntArrayElements(env,oldArray, 0);
    jint *after = (*env)->GetIntArrayElements(env,newArray,0);

    int i;
    for(i=0; i<len; i++) {
        after[i] = before[i]*9/5 + 32;
    }

    (*env)->ReleaseIntArrayElements(env, newArray, after, 0);
    (*env)->ReleaseIntArrayElements(env, oldArray, before, 0);

    return newArray;
}

// Convert temperature from Celsius to Fahrenheit. Used in ListActivity.
JNIEXPORT jint JNICALL
Java_com_wb_weatherbender_ListActivity_convertCToF(JNIEnv *env, jobject instance, jint cTemp) {
    return cTemp*9/5 + 32;
}