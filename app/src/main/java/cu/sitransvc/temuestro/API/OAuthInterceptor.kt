package cu.sitransvc.temuestro.API

import android.util.Log
import okhttp3.Interceptor


class OAuthInterceptor(private val tokenType: String, private val accessToken: String) :    Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        request = request.newBuilder().header("Authorization", "$tokenType $accessToken").build()
       // Log.e("request",request.toString());
      //  Log.e("tokenType", tokenType)
      //  Log.e("accessToken", accessToken)
        return chain.proceed(request)
    }
}