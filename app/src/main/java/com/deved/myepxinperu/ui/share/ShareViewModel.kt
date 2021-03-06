package com.deved.myepxinperu.ui.share

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.deved.data.common.DataResponse
import com.deved.data.repository.PermissionsChecker
import com.deved.domain.Department
import com.deved.domain.Places
import com.deved.interactors.RegisterExp
import com.deved.interactors.UploadPicture
import com.deved.myepxinperu.R
import com.deved.myepxinperu.coroutines.ScopeViewModel
import com.deved.myepxinperu.ui.common.Event
import com.deved.myepxinperu.ui.common.RequestPermission
import com.deved.myepxinperu.ui.common.UiContext
import com.deved.myepxinperu.ui.common.validate
import com.deved.myepxinperu.ui.model.Picture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShareViewModel(
    private val useCase: RegisterExp,
    private val uploadPicture: UploadPicture,
    private val permissionChecker: PermissionsChecker
) : ScopeViewModel() {
    private var _isViewLoading = MutableLiveData<Boolean>()
    val isViewLoading: LiveData<Boolean> get() = _isViewLoading
    private var _onMessageError = MutableLiveData<Event<Any>>()
    val onMessageError: LiveData<Event<Any>> get() = _onMessageError
    private var _onMessageSuccess = MutableLiveData<Event<Any>>()
    val onMessageSuccess: LiveData<Event<Any>> get() = _onMessageSuccess
    private var _permission = MutableLiveData<RequestPermission>()
    val permission: LiveData<RequestPermission> get() = _permission
    private var _takePicture = MutableLiveData<Event<Boolean>>()
    val takePicture: LiveData<Event<Boolean>> get() = _takePicture
    private var _pictures =
        MutableLiveData<MutableList<Picture>>().apply { value = mutableListOf() }
    val pictures: LiveData<MutableList<Picture>> get() = _pictures

    fun validateRegisterExp(
        department: String, touristName: String,
        touristDescription: String, userId: String,
        pictures: List<Picture>
    ) {
        if (!department.validate()) _onMessageError.postValue(Event(UiContext.getString(R.string.invalidInputDeparment)))
        else if (!touristName.validate()) _onMessageError.postValue(Event(UiContext.getString(R.string.invalidInputTouristName)))
        else if (!touristDescription.validate()) _onMessageError.postValue(Event(UiContext.getString(R.string.invalidInputDescription)))
        else if (pictures.isEmpty()) _onMessageError.postValue(Event(UiContext.getString(R.string.invalidInputPictureOne)))
        else if (pictures.size < 2) _onMessageError.postValue(Event(UiContext.getString(R.string.invalidSizePictures)))
        else shareExp(
            department,
            touristName,
            touristDescription,
            pictures[0].picture.toString(),
            pictures[1].picture.toString(),
            "",
            userId
        )
    }

    fun savePictureInMemory(item: Picture) {
        _pictures.value?.add(item)
        _pictures.value = _pictures.value
    }

    fun deletePictureOfMemory(item: Picture) {
        _pictures.value?.remove(item)
        _pictures.value = _pictures.value
    }


    fun getPicture() {
        if (validatePermission()) {
            if (validateSizePictures(pictures.value!!)) _takePicture.postValue(Event(true))
            else _onMessageError.postValue(Event(UiContext.getString(R.string.numberOfSizePictureIsExced)))
        } else _permission.postValue(RequestPermission.RequestStorage)
    }

    private fun validatePermission(): Boolean {
        return (permissionChecker.checkPermission(PermissionsChecker.Permissions.READ_EXTERNAL_STORAGE)
                and permissionChecker.checkPermission(PermissionsChecker.Permissions.WRITE_EXTERNAL_STORAGE))
    }

    private fun validateSizePictures(pictures: List<Picture>): Boolean {
        return pictures.size in 0..1
    }

    private fun shareExp(
        department: String,
        touristName: String,
        touristDescription: String,
        one: String,
        second: String,
        createAt: String,
        userId: String
    ) {
        launch {
            _isViewLoading.postValue(true)
            val one = withContext(Dispatchers.IO) { uploadPicture.invoke(one) }
            val second = withContext(Dispatchers.IO) { uploadPicture.invoke(second) }
            val resultFirst = uploadDoAction(one).toString()
            val resultSecond = uploadDoAction(second).toString()
            doAction(
                useCase.invoke(
                    Department(
                        department,
                        Places(touristName, touristDescription, resultFirst, resultSecond, createAt)
                    ), userId
                )
            )
            _isViewLoading.postValue(false)
        }
    }

    private fun doAction(invoke: DataResponse<String>) {
        when (invoke) {
            is DataResponse.Success -> _onMessageSuccess.postValue(Event(UiContext.getString(R.string.success_registered_shared)))
            is DataResponse.NetworkError -> _onMessageError.postValue(Event(invoke.error))
            is DataResponse.TimeOutServerError -> _onMessageError.postValue(Event(invoke.error))
            is DataResponse.ExceptionError -> _onMessageError.postValue(Event(invoke.errorCode.message.toString()))
            is DataResponse.ServerError -> _onMessageError.postValue(Event(invoke.errorCode))
        }
    }

    private fun uploadDoAction(invoke: DataResponse<String>): Any? {
        return when (invoke) {
            is DataResponse.Success -> invoke.data
            is DataResponse.NetworkError -> _onMessageError.postValue(Event(invoke.error))
            is DataResponse.TimeOutServerError -> _onMessageError.postValue(Event(invoke.error))
            is DataResponse.ExceptionError -> _onMessageError.postValue(Event(invoke.errorCode.message.toString()))
            is DataResponse.ServerError -> _onMessageError.postValue(Event(invoke.errorCode))
        }
    }
}