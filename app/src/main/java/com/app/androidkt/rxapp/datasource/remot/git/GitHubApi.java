package com.app.androidkt.rxapp.datasource.remot.git;

import java.util.ArrayList;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by brijesh on 25/4/17.
 */

public interface GitHubApi {

    @GET("/users")
    Flowable<ArrayList<User>> getUser(@Query("since") int since, @Query("per_page") int perPage);
}
