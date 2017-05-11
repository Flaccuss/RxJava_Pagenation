package com.app.androidkt.rxapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.app.androidkt.rxapp.datasource.remot.git.GitHubApi;
import com.app.androidkt.rxapp.datasource.remot.git.GitHubService;
import com.app.androidkt.rxapp.datasource.remot.git.User;
import com.app.androidkt.rxapp.datasource.remot.git.UserPagination;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    public boolean requestOnWay = false;
    @BindView(R.id.userList)
    RecyclerView recyclerView;
    @BindView(R.id.loadUser)
    ProgressBar loadUser;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<User> usersList;
    private UserListAdapter mAdapter;
    private PublishProcessor<Integer> pagination;
    private CompositeDisposable compositeDisposable;

    private GitHubApi gitHubApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        pagination = PublishProcessor.create();
        compositeDisposable = new CompositeDisposable();
        gitHubApi = GitHubService.createGitHubService();

        recyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        usersList = new ArrayList<>();
        mAdapter = new UserListAdapter(usersList, MainActivity.this);
        recyclerView.setAdapter(mAdapter);

        final LinearLayoutManager layoutManager = (LinearLayoutManager) mLayoutManager;
        recyclerView.addOnScrollListener(new UserPagination(layoutManager) {
            @Override
            public void onLoadMore(int currentPage, int totalItemCount, View view) {
                if (!requestOnWay) {
                    pagination.onNext(mAdapter.getLastVisibleItemId());
                }
            }
        });
        Disposable disposable = pagination.onBackpressureDrop()
                .doOnNext(integer -> {
                    requestOnWay = true;
                    loadUser.setVisibility(View.VISIBLE);
                })
                .concatMap(new Function<Integer, Publisher<Response<List<User>>>>() {
                    @Override
                    public Publisher<Response<List<User>>> apply(Integer fromId) throws Exception {
                        return getUserList(fromId);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Response<List<User>>>() {
                    @Override
                    public void accept(Response<List<User>> gitHubUsers) throws Exception {
                        mAdapter.setUsers(gitHubUsers.body());
                        requestOnWay = false;
                        loadUser.setVisibility(View.INVISIBLE);
                    }
                })
                .subscribe();

        compositeDisposable.add(disposable);
        pagination.onNext(0);
    }

    private Flowable<Response<List<User>>> getUserList(int fromId) {
        return gitHubApi.getUser(fromId, Constants.PAGE_LIMIT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}
