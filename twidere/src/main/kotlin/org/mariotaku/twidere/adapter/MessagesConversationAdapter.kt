/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.apache.commons.lang3.time.DateUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.annotation.PreviewStyle
import org.mariotaku.twidere.constant.linkHighlightOptionKey
import org.mariotaku.twidere.constant.mediaPreviewStyleKey
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.extension.model.timestamp
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.ParcelableMessage.MessageType
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.view.holder.message.AbsMessageViewHolder
import org.mariotaku.twidere.view.holder.message.MessageViewHolder
import org.mariotaku.twidere.view.holder.message.NoticeSummaryEventViewHolder
import org.mariotaku.twidere.view.holder.message.StickerMessageViewHolder
import java.util.*

class MessagesConversationAdapter(context: Context) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context),
        IItemCountsAdapter {
    private val calendars = Pair(Calendar.getInstance(), Calendar.getInstance())
    override val itemCounts: ItemCounts = ItemCounts(1)

    @PreviewStyle
    val mediaPreviewStyle: Int = preferences[mediaPreviewStyleKey]
    val linkHighlightingStyle: Int = preferences[linkHighlightOptionKey]
    val nameFirst: Boolean = preferences[nameFirstKey]
    val linkify: TwidereLinkify = TwidereLinkify(null)

    var messages: List<ParcelableMessage>? = null
        private set
    var conversation: ParcelableMessageConversation? = null
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            ITEM_TYPE_TEXT_MESSAGE -> {
                val view = inflater.inflate(MessageViewHolder.layoutResource, parent, false)
                return MessageViewHolder(view, this)
            }
            ITEM_TYPE_STICKER_MESSAGE -> {
                val view = inflater.inflate(StickerMessageViewHolder.layoutResource, parent, false)
                return StickerMessageViewHolder(view, this)
            }
            ITEM_TYPE_CONVERSATION_CREATE, ITEM_TYPE_JOIN_CONVERSATION,
            ITEM_TYPE_PARTICIPANTS_LEAVE, ITEM_TYPE_PARTICIPANTS_JOIN -> {
                val view = inflater.inflate(NoticeSummaryEventViewHolder.layoutResource, parent, false)
                return NoticeSummaryEventViewHolder(view, this)
            }
        }
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_TYPE_TEXT_MESSAGE, ITEM_TYPE_STICKER_MESSAGE, ITEM_TYPE_CONVERSATION_CREATE,
            ITEM_TYPE_JOIN_CONVERSATION, ITEM_TYPE_PARTICIPANTS_LEAVE, ITEM_TYPE_PARTICIPANTS_JOIN -> {
                val message = getMessage(position)!!
                // Display date for oldest item
                var showDate = true
                // ... or if current message is > 1 day newer than previous one
                if (position < itemCounts.getItemStartPosition(ITEM_START_MESSAGE)
                        + itemCounts[ITEM_START_MESSAGE] - 1) {
                    calendars.first.timeInMillis = getMessage(position + 1)!!.timestamp
                    calendars.second.timeInMillis = message.timestamp
                    showDate = !DateUtils.isSameDay(calendars.first, calendars.second)
                }
                (holder as AbsMessageViewHolder).display(message, showDate)
            }
        }

    }

    override fun getItemCount(): Int {
        itemCounts[ITEM_START_MESSAGE] = messages?.size ?: 0
        return itemCounts.itemCount
    }

    override fun getItemViewType(position: Int): Int {
        when (itemCounts.getItemCountIndex(position)) {
            ITEM_START_MESSAGE -> {
                when (getMessage(position)!!.message_type) {
                    MessageType.STICKER -> {
                        return ITEM_TYPE_STICKER_MESSAGE
                    }
                    MessageType.CONVERSATION_CREATE -> {
                        return ITEM_TYPE_CONVERSATION_CREATE
                    }
                    MessageType.JOIN_CONVERSATION -> {
                        return ITEM_TYPE_JOIN_CONVERSATION
                    }
                    MessageType.PARTICIPANTS_LEAVE -> {
                        return ITEM_TYPE_PARTICIPANTS_LEAVE
                    }
                    MessageType.PARTICIPANTS_JOIN -> {
                        return ITEM_TYPE_PARTICIPANTS_JOIN
                    }
                    else -> return ITEM_TYPE_TEXT_MESSAGE
                }
            }
        }
        throw UnsupportedOperationException()
    }

    fun getMessage(position: Int): ParcelableMessage? {
        return messages?.get(position - itemCounts.getItemStartPosition(0))
    }

    fun findUser(key: UserKey): ParcelableUser? {
        return conversation?.participants?.firstOrNull { it.key == key }
    }

    fun setData(conversation: ParcelableMessageConversation?, messages: List<ParcelableMessage>?) {
        this.conversation = conversation
        this.messages = messages
        notifyDataSetChanged()
    }

    companion object {
        private const val ITEM_START_MESSAGE = 0

        const val ITEM_TYPE_TEXT_MESSAGE = 1
        const val ITEM_TYPE_STICKER_MESSAGE = 2
        const val ITEM_TYPE_CONVERSATION_CREATE = 3
        const val ITEM_TYPE_JOIN_CONVERSATION = 4
        const val ITEM_TYPE_PARTICIPANTS_LEAVE = 5
        const val ITEM_TYPE_PARTICIPANTS_JOIN = 6
    }

}

