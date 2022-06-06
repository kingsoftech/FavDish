package com.tutorials.eu.favdish.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.tutorials.eu.favdish.model.entities.Recipes
import com.tutorials.eu.favdish.network.RandomDishApiService

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers

class RandomDishViewModel: ViewModel() {
    private val randomRecipesApiService =RandomDishApiService()

    private val compositeDishDisposable = CompositeDisposable()

    val loadRandomDish = MutableLiveData<Boolean>()
    val randomDishResponse = MutableLiveData<Recipes>()
    val randomDishLoadingError  = MutableLiveData<Boolean>()
    fun getRandomRecipeFromApi(){
        loadRandomDish.value = true
        compositeDishDisposable.add(
            randomRecipesApiService.getRandomDish().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object :DisposableSingleObserver<Recipes>(){
                    override fun onSuccess(value: Recipes?) {
                        loadRandomDish.value = false
                        randomDishResponse.value = value
                        randomDishLoadingError.value = false
                    }

                    override fun onError(e: Throwable?) {

                    }


                })
        )
    }
}