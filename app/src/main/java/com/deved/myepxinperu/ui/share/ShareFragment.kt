package com.deved.myepxinperu.ui.share

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.deved.myepxinperu.databinding.FragmentShareBinding
import com.deved.myepxinperu.ui.common.*
import com.deved.myepxinperu.ui.model.Picture
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel

class ShareFragment : Fragment() {
    private lateinit var binding: FragmentShareBinding
    private val viewModel: ShareViewModel by lifecycleScope.viewModel(this)
    private val adapter by lazy { ShareAdapter(getListener()) }
    private lateinit var requestManager: PermissionRequester

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShareBinding.inflate(inflater, container, false)
        setUpViewModelObserver()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestManager = PermissionRequester(requireActivity())
        setUpRecyclerView()
        setUpEvents()
    }

    private fun setUpRecyclerView() {
        binding.recyclerViewPictures.layoutManager = GridLayoutManager(activity, 2)
        binding.recyclerViewPictures.adapter = adapter
    }

    private fun setUpViewModelObserver() {
        viewModel.isViewLoading.observe(viewLifecycleOwner, isViewLoadingObserver)
        viewModel.onMessageError.observe(viewLifecycleOwner, EventObserver(::showMessage))
        viewModel.onMessageSuccess.observe(viewLifecycleOwner, EventObserver(::showMessage))
        viewModel.permission.observe(viewLifecycleOwner, permissionObserver)
        viewModel.takePicture.observe(viewLifecycleOwner, takePictureObserver)
        viewModel.pictures.observe(viewLifecycleOwner, picturesObserver)
    }

    private fun setUpEvents() {
        with(binding) {
            materialButtonShareExp.setOnClickListener {
                val department = textInputEditDepartment.text?.trim().toString()
                val touristName = textInputEditTextNameTourist.text?.trim().toString()
                val touristDescription =
                    textInputEditTextTouristDescription.text?.trim().toString()

                val pictures = viewModel.pictures.value.orEmpty()
                val userId = UserSingleton.getUid()
                viewModel.validateRegisterExp(
                    department,
                    touristName,
                    touristDescription,
                    userId,
                    pictures
                )
            }

            imageButtonAdd.setOnClickListener {
                viewModel.getPicture()
            }
        }
    }

    private fun getListener(): (Picture) -> Unit {
        return {
            viewModel.deletePictureOfMemory(it)
        }
    }

    private val isViewLoadingObserver = Observer<Boolean> {
        binding.progressBarShareExp.isVisible = it
    }

    private val takePictureObserver = Observer<Event<Boolean>> {
        it?.let {
            it.getContentIfNotHandled()?.let {
                fetchPicture()
            }
        }
    }

    private val permissionObserver = Observer<RequestPermission> {
        when (it) {
            is RequestPermission.RequestStorage -> {
                requestManager.request(READ_EXTERNAL_STORAGE, ::handleGrantedPermission)
                requestManager.request(WRITE_EXTERNAL_STORAGE, ::handleGrantedPermission)
            }
        }
    }

    private val picturesObserver = Observer<MutableList<Picture>> {
        it?.let {
            adapter.setData(it)
        }
    }

    private fun handleGrantedPermission(granted: Boolean) {
        if (granted) viewModel.getPicture()
    }

    private fun showMessage(it: Any) {
        activity?.toast(it.toString())
    }

    private fun fetchPicture() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            viewModel.savePictureInMemory(Picture(data?.data, "Imagen2", "Descripción2"))
        }
    }

    companion object {
        val TAG = ShareFragment::class.java.name
        fun newInstance() = ShareFragment()
    }
}
