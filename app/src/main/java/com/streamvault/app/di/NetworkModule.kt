package com.streamvault.app.di

import com.streamvault.app.data.api.XtreamApiService
import com.streamvault.app.data.preferences.UserPreferences
import com.streamvault.app.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        userPreferences: UserPreferences
    ): OkHttpClient {
        val dynamicBaseUrlInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val serverUrl = runBlocking { userPreferences.serverUrl.first() }
            if (serverUrl.isNotEmpty()) {
                val newUrl = originalRequest.url.newBuilder()
                    .host(extractHost(serverUrl))
                    .port(extractPort(serverUrl))
                    .scheme(extractScheme(serverUrl))
                    .build()
                chain.proceed(originalRequest.newBuilder().url(newUrl).build())
            } else {
                chain.proceed(originalRequest)
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(dynamicBaseUrlInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://placeholder.api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideXtreamApiService(retrofit: Retrofit): XtreamApiService =
        retrofit.create(XtreamApiService::class.java)

    private fun extractHost(url: String): String = try {
        java.net.URL(url).host
    } catch (e: Exception) { url }

    private fun extractPort(url: String): Int = try {
        val port = java.net.URL(url).port
        if (port == -1) if (url.startsWith("https")) 443 else 80 else port
    } catch (e: Exception) { 80 }

    private fun extractScheme(url: String): String = try {
        java.net.URL(url).protocol
    } catch (e: Exception) { "http" }
}
