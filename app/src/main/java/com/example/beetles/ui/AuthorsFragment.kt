package com.example.beetles.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.beetles.R
import com.example.beetles.databinding.FragmentAuthorsBinding
import com.example.beetles.model.Author

class AuthorsFragment : Fragment() {

    private var _binding: FragmentAuthorsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val authors = listOf(
            Author(
                name = getString(R.string.author_student_name),
                photoRes = R.drawable.author_photo
            )
        )
        binding.lvAuthors.adapter = AuthorAdapter(authors)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
