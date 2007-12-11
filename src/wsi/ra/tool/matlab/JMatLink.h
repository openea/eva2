/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class wsi_ra_tool_matlab_JMatLink */

#ifndef _Included_wsi_ra_tool_matlab_JMatLink
#define _Included_wsi_ra_tool_matlab_JMatLink
#ifdef __cplusplus
extern "C" {
#endif
/* Inaccessible static: threadInitNumber */
/* Inaccessible static: stopThreadPermission */
#undef wsi_ra_tool_matlab_JMatLink_MIN_PRIORITY
#define wsi_ra_tool_matlab_JMatLink_MIN_PRIORITY 1L
#undef wsi_ra_tool_matlab_JMatLink_NORM_PRIORITY
#define wsi_ra_tool_matlab_JMatLink_NORM_PRIORITY 5L
#undef wsi_ra_tool_matlab_JMatLink_MAX_PRIORITY
#define wsi_ra_tool_matlab_JMatLink_MAX_PRIORITY 10L
#undef wsi_ra_tool_matlab_JMatLink_idleI
#define wsi_ra_tool_matlab_JMatLink_idleI 0L
#undef wsi_ra_tool_matlab_JMatLink_engOpenI
#define wsi_ra_tool_matlab_JMatLink_engOpenI 1L
#undef wsi_ra_tool_matlab_JMatLink_engCloseI
#define wsi_ra_tool_matlab_JMatLink_engCloseI 2L
#undef wsi_ra_tool_matlab_JMatLink_engEvalStringI
#define wsi_ra_tool_matlab_JMatLink_engEvalStringI 3L
#undef wsi_ra_tool_matlab_JMatLink_engGetScalarI
#define wsi_ra_tool_matlab_JMatLink_engGetScalarI 4L
#undef wsi_ra_tool_matlab_JMatLink_engGetVectorI
#define wsi_ra_tool_matlab_JMatLink_engGetVectorI 5L
#undef wsi_ra_tool_matlab_JMatLink_engGetArrayI
#define wsi_ra_tool_matlab_JMatLink_engGetArrayI 6L
#undef wsi_ra_tool_matlab_JMatLink_engPutArray2dI
#define wsi_ra_tool_matlab_JMatLink_engPutArray2dI 9L
#undef wsi_ra_tool_matlab_JMatLink_engOutputBufferI
#define wsi_ra_tool_matlab_JMatLink_engOutputBufferI 10L
#undef wsi_ra_tool_matlab_JMatLink_engGetCharArrayI
#define wsi_ra_tool_matlab_JMatLink_engGetCharArrayI 11L
#undef wsi_ra_tool_matlab_JMatLink_destroyJMatLinkI
#define wsi_ra_tool_matlab_JMatLink_destroyJMatLinkI 12L
#undef wsi_ra_tool_matlab_JMatLink_engOpenSingleUseI
#define wsi_ra_tool_matlab_JMatLink_engOpenSingleUseI 13L
/*
 * Class:     wsi_ra_tool_matlab_JMatLink
 * Method:    displayHelloWorld
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_wsi_ra_tool_matlab_JMatLink_displayHelloWorld
  (JNIEnv *, jobject);

/*
 * Class:     wsi_ra_tool_matlab_JMatLink
 * Method:    engTestNATIVE
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_wsi_ra_tool_matlab_JMatLink_engTestNATIVE
  (JNIEnv *, jobject);

/*
 * Class:     wsi_ra_tool_matlab_JMatLink
 * Method:    engOpenNATIVE
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_wsi_ra_tool_matlab_JMatLink_engOpenNATIVE
  (JNIEnv *, jobject, jstring);

/*
 * Class:     wsi_ra_tool_matlab_JMatLink
 * Method:    engOpenSingleUseNATIVE
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_wsi_ra_tool_matlab_JMatLink_engOpenSingleUseNATIVE
  (JNIEnv *, jobject, jstring);

/*
 * Class:     wsi_ra_tool_matlab_JMatLink
 * Method:    engCloseNATIVE
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_wsi_ra_tool_matlab_JMatLink_engCloseNATIVE
  (JNIEnv *, jobject, jint);

/*
 * Class:     wsi_ra_tool_matlab_JMatLink
 * Method:    engEvalStringNATIVE
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_wsi_ra_tool_matlab_JMatLink_engEvalStringNATIVE
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     wsi_ra_tool_matlab_JMatLink
 * Method:    engGetScalarNATIVE
 * Signature: (ILjava/lang/String;)D
 */
JNIEXPORT jdouble JNICALL Java_wsi_ra_tool_matlab_JMatLink_engGetScalarNATIVE
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     wsi_ra_tool_matlab_JMatLink
 * Method:    engGetVectorNATIVE
 * Signature: (ILjava/lang/String;)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_wsi_ra_tool_matlab_JMatLink_engGetVectorNATIVE
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     wsi_ra_tool_matlab_JMatLink
 * Method:    engGetArrayNATIVE
 * Signature: (ILjava/lang/String;)[[D
 */
JNIEXPORT jobjectArray JNICALL Java_wsi_ra_tool_matlab_JMatLink_engGetArrayNATIVE
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     wsi_ra_tool_matlab_JMatLink
 * Method:    engGetCharArrayNATIVE
 * Signature: (ILjava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_wsi_ra_tool_matlab_JMatLink_engGetCharArrayNATIVE
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     wsi_ra_tool_matlab_JMatLink
 * Method:    engPutArrayNATIVE
 * Signature: (ILjava/lang/String;[[D)V
 */
JNIEXPORT void JNICALL Java_wsi_ra_tool_matlab_JMatLink_engPutArrayNATIVE
  (JNIEnv *, jobject, jint, jstring, jobjectArray);

/*
 * Class:     wsi_ra_tool_matlab_JMatLink
 * Method:    engOutputBufferNATIVE
 * Signature: (II)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_wsi_ra_tool_matlab_JMatLink_engOutputBufferNATIVE
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     wsi_ra_tool_matlab_JMatLink
 * Method:    setDebugNATIVE
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_wsi_ra_tool_matlab_JMatLink_setDebugNATIVE
  (JNIEnv *, jobject, jboolean);

#ifdef __cplusplus
}
#endif
#endif
