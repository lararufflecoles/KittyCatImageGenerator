package es.rufflecol.lara.kittycatimagegenerator.API;

import es.rufflecol.lara.kittycatimagegenerator.Model.KittyCatModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;

public interface KittyCatAPI {

    @GET("/random") // The green text is an end point
    Call<KittyCatModel> getRandomCat();

    @GET("/random/kitten") // The green text is an end point
    Call<KittyCatModel> getRandomKitty();
}