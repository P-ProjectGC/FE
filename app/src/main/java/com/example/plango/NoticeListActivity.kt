// com/example/plango/NoticeListActivity.kt
package com.example.plango

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plango.adapter.NoticeAdapter
import com.example.plango.data.NoticeRepository
import com.example.plango.databinding.ActivityNoticeListBinding
import kotlinx.coroutines.launch

class NoticeListActivity : ComponentActivity() {

    private lateinit var binding: ActivityNoticeListBinding
    private val noticeRepository = NoticeRepository()
    private val noticeAdapter = NoticeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupRefresh()

        loadNotices()
    }

    private fun setupToolbar() = with(binding.toolbarNotice) {
        setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() = with(binding.recyclerNotice) {
        layoutManager = LinearLayoutManager(this@NoticeListActivity)
        adapter = noticeAdapter
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadNotices()
        }
    }

    private fun loadNotices() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            val result = noticeRepository.fetchNotices()
            binding.progressBar.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false

            result.onSuccess { list ->
                noticeAdapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }.onFailure { e ->
                Toast.makeText(
                    this@NoticeListActivity,
                    e.message ?: "공지사항을 불러오지 못했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
