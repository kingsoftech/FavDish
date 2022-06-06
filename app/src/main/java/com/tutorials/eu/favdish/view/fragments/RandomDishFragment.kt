package com.tutorials.eu.favdish.view.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.tutorials.eu.favdish.R
import com.tutorials.eu.favdish.application.FavDishApplication
import com.tutorials.eu.favdish.databinding.DialogCustomProgressBinding
import com.tutorials.eu.favdish.databinding.FragmentRandomDishBinding
import com.tutorials.eu.favdish.model.entities.FavDish
import com.tutorials.eu.favdish.model.entities.Recipe
import com.tutorials.eu.favdish.model.entities.Recipes
import com.tutorials.eu.favdish.utils.Constants
import com.tutorials.eu.favdish.viewmodel.FavDishViewModel
import com.tutorials.eu.favdish.viewmodel.FavDishViewModelFactory
import com.tutorials.eu.favdish.viewmodel.RandomDishViewModel

@Suppress("DEPRECATION")
class RandomDishFragment : Fragment(){
    private var mBinding:FragmentRandomDishBinding? = null
    lateinit var mRandomDishViewModel: RandomDishViewModel
    private var  mProgressDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentRandomDishBinding.inflate(inflater, container, false)

        return  mBinding!!.root
    }
    private fun showCustomProgressDialog(){
        val binding = DialogCustomProgressBinding.inflate(layoutInflater )
        mProgressDialog = Dialog(requireActivity())
        mProgressDialog?.let{
             it.setContentView(binding.root)
            it.show()
        }
    }
    private  fun hideCustomProgressDialog(){
        mProgressDialog?.let {
            it.dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRandomDishViewModel = ViewModelProvider(this).get(RandomDishViewModel::class.java)
        mRandomDishViewModel.getRandomRecipeFromApi()
        randomDishViewModelObserver()
        mBinding!!.srlDishDetailMain.setOnRefreshListener {
            mRandomDishViewModel.getRandomRecipeFromApi()
        }
    }

    private fun randomDishViewModelObserver(){
        mRandomDishViewModel.randomDishResponse.observe(viewLifecycleOwner,
            {
                randomDishResponse->
                randomDishResponse?.let {
                    Log.i("Random dish Recipes", "${randomDishResponse.recipes[0]}")
                    if(mBinding!!.srlDishDetailMain.isRefreshing){
                        mBinding!!.srlDishDetailMain.isRefreshing = false
                    }
                    setRandomDishResponseInUI(randomDishResponse.recipes[0])
                }
            })
        mRandomDishViewModel.randomDishLoadingError.observe(viewLifecycleOwner,
            {
                dataError->
                dataError?.let {
                    Log.i("Random dish API error", "$dataError")
                    if(mBinding!!.srlDishDetailMain.isRefreshing){
                        mBinding!!.srlDishDetailMain.isRefreshing = false
                    }
                }
            })
        mRandomDishViewModel.loadRandomDish.observe(viewLifecycleOwner,
            {
                loadRandomDish->
                loadRandomDish?.let {
                    Log.i("Random dish loading", "$loadRandomDish")
                    if(loadRandomDish && mBinding!!.srlDishDetailMain.isRefreshing){
                        showCustomProgressDialog()
                    }else{
                        hideCustomProgressDialog()
                    }
                }
            })
    }
    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }

    private fun setRandomDishResponseInUI(recipe: Recipe){
        Glide.with(requireActivity())
                .load(recipe.image)
                .centerCrop()
                .into(mBinding!!.ivDishImage)
        mBinding!!.tvTitle.text = recipe.title
        var dishType:String = "other"
        if(recipe.dishTypes.isNotEmpty()){
            dishType = recipe.dishTypes[0]
            mBinding!!.tvType.text = dishType
        }
        mBinding!!.tvCategory.text = "other"
        var ingredients = ""
        for(value in recipe.extendedIngredients){
            if (ingredients.isEmpty()) {

                ingredients = ingredients + ", \n" + value.original
            } else {
                ingredients = value.original
            }
        }
        mBinding!!.tvIngredients.text = ingredients
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.N){
            mBinding!!.tvCookingDirection.text = Html.fromHtml(
                    recipe.instructions,
                    Html.FROM_HTML_MODE_COMPACT
            )
        }
        else{
            mBinding!!.tvCookingDirection.text = Html.fromHtml(recipe.instructions)
        }
        mBinding!!.ivFavoriteDish.setImageDrawable(
                ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_favorite_unselected
                )
        )
        var addedToFavorite = false
        mBinding!!.tvCookingTime.text = resources.getString(R.string.lbl_estimate_cooking_time,
                recipe.readyInMinutes.toString())
        mBinding!!.ivFavoriteDish.setOnClickListener {
            if(addedToFavorite){
                Toast.makeText(requireActivity(),
                        resources.getString(R.string.msg_already_added_to_favorites)
                        ,Toast.LENGTH_SHORT).show()
            }
            else{
                val randomDishDetails = FavDish(
                        recipe.image,
                        Constants.DISH_IMAGE_SOURCE_ONLINE,
                        recipe.title,
                        dishType,
                        "other",
                        ingredients,
                        recipe.readyInMinutes.toString(),
                        recipe.instructions,
                        true
                )

                val favDishViewModel:FavDishViewModel by viewModels {
                    FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
                }
                favDishViewModel.insert(randomDishDetails)

                addedToFavorite = true
                mBinding!!.ivFavoriteDish.setImageDrawable(
                        ContextCompat.getDrawable(
                                requireActivity(),
                                R.drawable.ic_favorite_selected
                        )
                )
                Toast.makeText(requireActivity(),
                        resources.getString(R.string.msg_added_to_favorites),
                        Toast.LENGTH_SHORT).show()

            }
            }


    }
}