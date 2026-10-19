package com.example.beetles.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.beetles.databinding.FragmentRecordsBinding
import com.example.beetles.db.BeetlesDatabase
import kotlinx.coroutines.launch

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadRecords() {
        viewLifecycleOwner.lifecycleScope.launch {
            val records = BeetlesDatabase.get(requireContext()).scoreDao().getAllOrdered()
            val binding = _binding ?: return@launch
            if (records.isEmpty()) {
                binding.lvRecords.visibility = View.GONE
                binding.tvRecordsEmpty.visibility = View.VISIBLE
            } else {
                binding.tvRecordsEmpty.visibility = View.GONE
                binding.lvRecords.visibility = View.VISIBLE
                binding.lvRecords.adapter = RecordsAdapter(records)
            }
        }
    }
}
