package com.deved.myepxinperu.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import coil.api.load
import com.deved.domain.Places
import com.deved.domain.User
import com.deved.myepxinperu.databinding.FragmentDetailBinding
import com.deved.myepxinperu.ui.common.UserSingleton
import com.deved.myepxinperu.ui.common.toast
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel

class DetailFragment : Fragment() {
    private val viewmodel: DetailViewModel by lifecycleScope.viewModel(this)
    private lateinit var binding: FragmentDetailBinding
    private var departmentName: String? = null
    private var placeName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        departmentName = arguments?.getString(mDepartmentName, "")
        placeName = arguments?.getString(mPlaceName, "")
        viewmodel.getDetailPlace(departmentName!!, placeName!!)
        viewmodel.getDetailUserPosted(UserSingleton.getUid())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        setUpViewModelObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        events()
    }

    private fun events() {
        with(binding) {
            toolbarDetail.setNavigationOnClickListener {
                activity?.supportFragmentManager?.popBackStack()
            }
        }
    }

    private fun setUpViewModelObservers() {
        viewmodel.isViewLoading.observe(viewLifecycleOwner, isViewLoadingObserver)
        viewmodel.onErrorMessage.observe(viewLifecycleOwner, onErrorMessageObserver)
        viewmodel.onSuccessMessage.observe(viewLifecycleOwner, onSuccessMessageObserver)
        viewmodel.place.observe(viewLifecycleOwner, placeObserver)
        viewmodel.userPosted.observe(viewLifecycleOwner, userPostedObserver)
    }

    private val isViewLoadingObserver = Observer<Boolean> {
        binding.progressBarDetail.isVisible = it
    }

    private val onErrorMessageObserver = Observer<Any> {
        activity?.toast(it.toString())
    }

    private val onSuccessMessageObserver = Observer<Any> {
        activity?.toast(it.toString())
    }

    private val placeObserver = Observer<Places> {
        with(binding) {
            imageViewBackgroundDetail.load(it.picturesOne)
            textViewDatePublished.text = it.createAt
            textViewDescriptionPlace.text = it.description
            setUpToolbar(it.name)
        }
    }

    private val userPostedObserver = Observer<User> {
        with(binding) {
            textViewNameAvatar.text = it.name?.plus(" ").plus(it.lastName)
        }
    }

    private fun setUpToolbar(tit: String?) {
        binding.toolbarDetail.setTitle(tit)
    }

    companion object {
        @JvmStatic
        fun newInstance(departmentName: String?, placeName: String?) = DetailFragment().apply {
            arguments = Bundle().apply {
                putString(mDepartmentName, departmentName)
                putString(mPlaceName, placeName)
            }
        }

        private const val mDepartmentName = "mDepartmentName"
        private const val mPlaceName = "mPlaceName"
    }
}
