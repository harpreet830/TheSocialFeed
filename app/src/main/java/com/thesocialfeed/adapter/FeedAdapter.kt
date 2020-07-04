package com.thesocialfeed.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.thesocialfeed.utils.Constants
import com.thesocialfeed.R
import com.thesocialfeed.activity.HomeActivity
import com.thesocialfeed.database.DatabaseHelper
import com.thesocialfeed.database.FeedItemModel
import com.thesocialfeed.utils.BitmapUtils
import kotlinx.android.synthetic.main.item_feed.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


class FeedAdapter(private var context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var feedItemsArrayList: ArrayList<FeedItemModel> = ArrayList()
    private lateinit var rootView: View
    private val LOADER_VIEW = 2
    private val DATA_VIEW = 1
    public var isLastPage = false
    public var isFromDatabase = false
    var databaseHelper: DatabaseHelper? = null

    init {
        setHasStableIds(true)
        databaseHelper = DatabaseHelper.getDatabase(context)
    }

    fun isLastPage(isLastPage: Boolean) {
        this.isLastPage = isLastPage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == DATA_VIEW) {
            rootView = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false)
            return EarningsViewHolder(rootView)
        } else {
            return LoaderViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.item_progressbar,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return feedItemsArrayList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    fun isFromDatabase(isFromDatabase: Boolean) {
        this.isFromDatabase = isFromDatabase
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EarningsViewHolder) {
            holder.setIsRecyclable(false)
            val feedItem: FeedItemModel = feedItemsArrayList.get(position)
            holder.tvEventName.text = feedItem.event_name
            if (isFromDatabase) {
                val image = BitmapUtils.byteToBitmap(feedItem.image)
                holder.ivFeedImage.setImageBitmap(image)
            } else {
                Glide.with(context)
                    .load(feedItem.thumbnail_image)
                    .apply(RequestOptions().placeholder(R.drawable.ic_logo))
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .listener(object :
                        RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            // to save data from API into local database
                            val image: ByteArray =
                                BitmapUtils.bitmapToByte(resource!!.toBitmap())
                            val dataBaseFeedItem = FeedItemModel(
                                null,
                                feedItem.thumbnail_image,
                                image,
                                feedItem.event_name,
                                feedItem.event_date,
                                feedItem.views,
                                feedItem.likes,
                                feedItem.shares
                            )
                            CoroutineScope(IO).launch {
                                insertItemsInDatabase(dataBaseFeedItem)
                            }
                            return false
                        }
                    })
                    .into(holder.ivFeedImage)
            }


            holder.parentLayout.setOnClickListener {
                (context as HomeActivity).openBottomSheetWithItemData(
                    feedItem
                )
            }
        }
    }


    class EarningsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvEventName: TextView = itemView.tvEventName
        var ivFeedImage: ImageView = itemView.ivFeedImage
        var parentLayout: ConstraintLayout = itemView.parentLayout

    }

    override fun getItemViewType(position: Int): Int {
        if (position == feedItemsArrayList.size - 1 && !isLastPage) {
            return LOADER_VIEW
        } else
            return DATA_VIEW
    }

    class LoaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }


    fun sortItemsByType(type: String) {
        when (type) {
            Constants.TYPE.LIKES -> {
                feedItemsArrayList.sortWith(Comparator { lhs, rhs -> rhs.likes.compareTo(lhs.likes) })
            }
            Constants.TYPE.SHARES -> {
                feedItemsArrayList.sortWith(Comparator { lhs, rhs -> rhs.shares.compareTo(lhs.shares) })
            }
            Constants.TYPE.VIEWS -> {
                feedItemsArrayList.sortWith(Comparator { lhs, rhs -> rhs.views.compareTo(lhs.views) })
            }
            Constants.TYPE.DATE -> {
                feedItemsArrayList.sortWith(Comparator { lhs, rhs -> rhs.event_date.compareTo(lhs.event_date) })
            }
        }
        notifyDataSetChanged()
    }

    fun addAll(ridesArrayList: ArrayList<FeedItemModel>) {
        feedItemsArrayList.addAll(ridesArrayList)
        notifyDataSetChanged()
    }

    suspend fun insertItemsInDatabase(dataBaseFeedItem: FeedItemModel) {
        databaseHelper!!.feedDao().insert(dataBaseFeedItem)
    }

}