package com.thesocialfeed.activity

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.thesocialfeed.utils.Constants
import com.thesocialfeed.R
import com.thesocialfeed.adapter.FeedAdapter
import com.thesocialfeed.database.DatabaseHelper
import com.thesocialfeed.database.FeedItemModel
import com.thesocialfeed.viewmodel.FeedViewModel
import kotlinx.android.synthetic.main.activity_feed.*
import kotlinx.android.synthetic.main.layout_bottomsheet_item_detail.*
import kotlinx.android.synthetic.main.layout_bottomsheet_sort.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HomeActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var feedViewModel: FeedViewModel
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var isLoading = false
    private var isLoadingFromDB = false
    private var isLastPage = false
    private var feedList = ArrayList<FeedItemModel>()
    private var CURRENT_PAGE = 1
    private var TOTAL_PAGES = 3
    private lateinit var sortItemBottomSheet: NestedScrollView
    private lateinit var itemDetailBottomSheet: NestedScrollView
    private lateinit var sortItemBottomSheetbehavior: BottomSheetBehavior<View>
    private lateinit var itemDetailBottomSheetbehavior: BottomSheetBehavior<View>
    var databaseHelper: DatabaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "The Social Feed"
        databaseHelper = DatabaseHelper.getDatabase(this)
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        sortItemBottomSheet = findViewById(R.id.nestedScrollViewSortItems)
        itemDetailBottomSheet = findViewById(R.id.nestedScrollViewItemDetails)
        sortItemBottomSheetbehavior = BottomSheetBehavior.from(sortItemBottomSheet)
        itemDetailBottomSheetbehavior = BottomSheetBehavior.from(itemDetailBottomSheet)
        feedViewModel = ViewModelProvider(this).get(FeedViewModel::class.java)
        initObserver()
        feedAdapter = FeedAdapter(this as Context)
        layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        setViewClickListeners()
        handleItemSortBottomSheetBehaviour()
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = feedAdapter
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.hasFixedSize()
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.getChildCount()
                val totalItemCount = layoutManager.getItemCount()
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                //pagination logic
                if (!isLoadingFromDB) {
                    if (!isLoading && !feedAdapter.isLastPage) {
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount - 1 && firstVisibleItemPosition >= 0 && !isLastPage) {
                            isLoading = true
                            CURRENT_PAGE++
                            feedViewModel.getDataFeed(CURRENT_PAGE, feedViewModel.GET_DATA_SETS)
                        }
                    }
                }
            }
        })
        feedViewModel.getDataFeed(CURRENT_PAGE, feedViewModel.GET_DATA_SETS)
        checkIfDataIsStoredInLocalDb()
        swipeToRefresh.setOnRefreshListener {
            // *bonus* used in case if data connectivity is available, user can swipe to get data from API
            feedViewModel.getDataFeed(1, feedViewModel.GET_DATA_SETS)
            swipeToRefresh.isRefreshing = false
        }
    }

    private fun checkIfDataIsStoredInLocalDb() {
        CoroutineScope(IO).launch {
            if (databaseHelper!!.feedDao().getAllFeedItems().isNotEmpty()) {
                for (i in databaseHelper!!.feedDao().getAllFeedItems().indices) {
                    Log.e(
                        "feed item - ",
                        databaseHelper!!.feedDao().getAllFeedItems().get(i).toString()
                    )
                }
            }
        }
    }

    private fun setViewClickListeners() {
        llSortItems.setOnClickListener(this)
        tvLikes.setOnClickListener(this)
        tvShares.setOnClickListener(this)
        tvDate.setOnClickListener(this)
        tvViews.setOnClickListener(this)
        rbViews.setOnClickListener(this)
        rbLikes.setOnClickListener(this)
        rbDates.setOnClickListener(this)
        rbShares.setOnClickListener(this)
    }

    private fun handleItemSortBottomSheetBehaviour() {
        sortItemBottomSheetbehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
            }

            override fun onStateChanged(p0: View, state: Int) {
                when (state) {
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        llSortItems.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        llSortItems.visibility = View.VISIBLE

                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {

                    }
                }
            }

        })
    }

    private fun initObserver() {
        feedViewModel.onDataFeedResponse.observe(this, androidx.lifecycle.Observer {
            isLoadingFromDB = false
            ivDataNotAvailable.visibility = View.GONE
            tvDataNotAvailableLabel.visibility = View.GONE
            llSortItems.visibility = View.VISIBLE

            feedList = ArrayList(
                Arrays.asList<FeedItemModel>(
                    *GsonBuilder().create().fromJson<Array<FeedItemModel>>(
                        it.getJSONArray("posts").toString(),
                        Array<FeedItemModel>::class.java
                    )
                )
            )
            CURRENT_PAGE = it.getInt("page")
            if (CURRENT_PAGE == TOTAL_PAGES) {
                feedAdapter.isLastPage(true)
            } else {
                feedAdapter.isLastPage(false)
            }
            if (feedList.size == 0) {
                Toast.makeText(this, "No data found", Toast.LENGTH_LONG).show()
            } else {
                recyclerView.visibility = View.VISIBLE
                feedAdapter.addAll(feedList)
            }
            isLoading = false
        })
// to handle network failures
        feedViewModel.onRetrofitError.observe(this, androidx.lifecycle.Observer {
            isLoading = false
            llSortItems.visibility = View.GONE
            recyclerView.visibility = View.GONE
            CoroutineScope(IO).launch {
                //To check local database if any data is saved in it
                if (databaseHelper!!.feedDao().getAllFeedItems().isNotEmpty()) {
                    CoroutineScope(Main).launch {
                        ivDataNotAvailable.visibility = View.VISIBLE
                        tvDataNotAvailableLabel.visibility = View.VISIBLE
                        tvDataNotAvailableLabel.text = getString(R.string.live_data_feed_not_available)
                        Snackbar
                            .make(
                                coordinatorLayout,
                                getString(R.string.load_data_db),
                                Snackbar.LENGTH_INDEFINITE
                            )
//                            .setAnchorView(llSortItems)
                            .setTextColor(Color.WHITE)
                            .setActionTextColor(Color.WHITE)
                            .setAction(getString(R.string.click_to_load)) {
                                llSortItems.visibility = View.VISIBLE
                                ivDataNotAvailable.visibility = View.GONE
                                tvDataNotAvailableLabel.visibility = View.GONE
                                isLoadingFromDB = true
                                recyclerView.visibility = View.VISIBLE
                                feedAdapter.isFromDatabase(true)
                                feedAdapter.isLastPage(true)
                                CoroutineScope(IO).launch {
                                    feedAdapter.addAll(
                                        databaseHelper!!.feedDao()
                                            .getAllFeedItems() as ArrayList<FeedItemModel>
                                    )
                                }
                            }.show()
                    }
                } else {
                    //the case when local database is empty
                    CoroutineScope(Main).launch {
                        ivDataNotAvailable.visibility = View.VISIBLE
                        tvDataNotAvailableLabel.visibility = View.VISIBLE
                        tvDataNotAvailableLabel.text = getString(R.string.data_not_available)
                    }
                }
            }
        })
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (sortItemBottomSheetbehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                val outRect = Rect()
                sortItemBottomSheet.getGlobalVisibleRect(outRect)
                if (!outRect.contains(
                        event.rawX.toInt(),
                        event.rawY.toInt()
                    )
                ) sortItemBottomSheetbehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            } else if (itemDetailBottomSheetbehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                val outRect = Rect()
                itemDetailBottomSheet.getGlobalVisibleRect(outRect)
                if (!outRect.contains(
                        event.rawX.toInt(),
                        event.rawY.toInt()
                    )
                ) itemDetailBottomSheetbehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun openBottomSheetWithItemData(feedItem: FeedItemModel) {
        tvItemTitle.text = feedItem.event_name
        tvLikesLabel.text = feedItem.likes.toString().plus(" ").plus(getString(R.string.likes))
        tvSharesLabel.text = feedItem.shares.toString().plus(" ").plus(getString(R.string.shares))
        tvViewsLabel.text = feedItem.views.toString().plus(" ").plus(getString(R.string.views))
        val df: DateFormat = SimpleDateFormat("dd MMM yyyy HH:mm")
        tvDateLabel.text = df.format(feedItem.event_date)
        Glide.with(this).load(feedItem.thumbnail_image)
            .apply(RequestOptions().placeholder(R.drawable.ic_logo))
            .transition(DrawableTransitionOptions.withCrossFade(200))
            .into(ivItemImage)
        itemDetailBottomSheetbehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llSortItems -> {
                sortItemBottomSheetbehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            R.id.tvLikes, R.id.rbLikes -> {
                feedAdapter.sortItemsByType(Constants.TYPE.LIKES)
                sortItemBottomSheetbehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                rbLikes.isChecked = true
                rbDates.isChecked = false
                rbShares.isChecked = false
                rbViews.isChecked = false

            }
            R.id.tvShares, R.id.rbShares -> {
                feedAdapter.sortItemsByType(Constants.TYPE.SHARES)
                sortItemBottomSheetbehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                rbShares.isChecked = true
                rbLikes.isChecked = false
                rbDates.isChecked = false
                rbViews.isChecked = false

            }
            R.id.tvDate, R.id.rbDates -> {
                feedAdapter.sortItemsByType(Constants.TYPE.DATE)
                sortItemBottomSheetbehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                rbDates.isChecked = true
                rbShares.isChecked = false
                rbLikes.isChecked = false
                rbViews.isChecked = false
            }

            R.id.tvViews, R.id.rbViews -> {
                feedAdapter.sortItemsByType(Constants.TYPE.VIEWS)
                sortItemBottomSheetbehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                rbDates.isChecked = false
                rbShares.isChecked = false
                rbLikes.isChecked = false
                rbViews.isChecked = true
            }
        }
    }


}