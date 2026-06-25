package com.streamvault.app.player

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.streamvault.app.utils.Constants

class CastOptionsProvider : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions =
        CastOptions.Builder()
            .setReceiverApplicationId(Constants.CAST_APP_ID)
            .build()

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider> =
        emptyList()
}
