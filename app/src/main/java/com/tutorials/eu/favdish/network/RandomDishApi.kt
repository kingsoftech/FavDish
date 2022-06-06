package com.tutorials.eu.favdish.network

import com.tutorials.eu.favdish.model.entities.RandomDish
import com.tutorials.eu.favdish.model.entities.Recipes
import com.tutorials.eu.favdish.utils.Constants
import io.reactivex.rxjava3.core.CompletableOnSubscribe
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query


interface RandomDishApi {

    @GET(Constants.API_END_POINT)
    fun getRandomDish(
            // Query parameter appended to the URL. This is the best practice instead of appending it as we have done in the browser.
            @Query(Constants.API_KEY) apiKey: String,
            @Query(Constants.LIMIT_LICENSE) limitLicense: Boolean,
            @Query(Constants.TAGS) tags: String,
            @Query(Constants.NUMBER) number: Int
    ): Single<Recipes>
}