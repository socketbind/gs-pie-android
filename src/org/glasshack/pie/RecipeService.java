package org.glasshack.pie;

import org.glasshack.pie.model.Recipe;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by gabriel on 2014.05.25..
 */
public interface RecipeService {

    @GET("/recipe/{id}")
    Recipe getRecipeById(@Path("id") String id);

    @GET("/recipe/{id}")
    void getRecipeById(@Path("id") String id, Callback<Recipe> cb);

}
