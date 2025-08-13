#include <jni.h>
#include <string>
#include <curl/curl.h>
#include <stdlib.h>
#include <string.h>

struct Memory {
    char *data;
    size_t size;
};

static size_t write_callback(void *contents, size_t size, size_t nmemb, void *userp) {
    size_t realsize = size * nmemb;
    struct Memory *mem = (struct Memory *)userp;

    char *ptr = (char *)realloc(mem->data, mem->size + realsize + 1);
    if (!ptr) return 0; // Out of memory

    mem->data = ptr;
    memcpy(&(mem->data[mem->size]), contents, realsize);
    mem->size += realsize;
    mem->data[mem->size] = '\0';
    return realsize;
}

static char* get_URL_Cont(const char *url) {
    CURL *curl = curl_easy_init();
    if (!curl) return NULL;

    struct Memory chunk = {0};

    curl_easy_setopt(curl, CURLOPT_URL, url);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &chunk);

    // Disable SSL verification for testing only!
    curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L);
    curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 0L);

    CURLcode res = curl_easy_perform(curl);
    if (res != CURLE_OK) {
        free(chunk.data);
        chunk.data = NULL;
    }

    curl_easy_cleanup(curl);
    return chunk.data; // caller must free()
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_curltest4_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject, jstring URL) {
    const char *url = env->GetStringUTFChars(URL, nullptr);
    char *data = get_URL_Cont(url);
    env->ReleaseStringUTFChars(URL, url);
    if (data) {
        jstring result = env->NewStringUTF(data);
        free(data);
        return result;
    }
    return env->NewStringUTF("Failed to fetch URL");
}
