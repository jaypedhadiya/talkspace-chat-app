package com.example.talkspace.observers

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.talkspace.adapter.MessageAdapter

class MyScrollToBottomObserver(
    private val recyclerView: RecyclerView,
    private val adapter : MessageAdapter,
    private val manager : LayoutManager
): RecyclerView.AdapterDataObserver() {
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        recyclerView.scrollToPosition(positionStart)
    }
}