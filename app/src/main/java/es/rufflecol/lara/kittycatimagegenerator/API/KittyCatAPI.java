package es.rufflecol.lara.kittycatimagegenerator.API;

import es.rufflecol.lara.kittycatimagegenerator.Model.KittyCatModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;

public interface KittyCatAPI {

    @GET("/random")
    Call<KittyCatModel> getRandomCat();

    @GET("/random/kitten")
    Call<KittyCatModel> getRandomKitty();
}

// The green text is an end point

// @GET is one of the HTTP request methods used when connecting to a URL, @POST, @UPDATE, @DELETE are others.