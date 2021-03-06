package com.deved.myepxinperu.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.deved.data.common.DataResponse
import com.deved.domain.Places
import com.deved.domain.User
import com.deved.interactors.GetDetailPlace
import com.deved.interactors.GetDetailUserPosted
import com.deved.myepxinperu.coroutines.ScopeViewModel
import kotlinx.coroutines.launch

class DetailViewModel(
    private val useCase: GetDetailPlace,
    private val useCaseUserPosted: GetDetailUserPosted
) : ScopeViewModel() {
    private var _isViewLoading = MutableLiveData<Boolean>()
    val isViewLoading: LiveData<Boolean> get() = _isViewLoading
    private var _onErrorMessage = MutableLiveData<Any>()
    val onErrorMessage: LiveData<Any> get() = _onErrorMessage
    private var _onSuccessMessage = MutableLiveData<Any>()
    val onSuccessMessage: LiveData<Any> get() = _onSuccessMessage
    private var _place = MutableLiveData<Places>()
    val place: LiveData<Places> get() = _place
    private var _userPosted = MutableLiveData<User>()
    val userPosted: LiveData<User> get() = _userPosted

    fun getDetailPlace(departmentName: String, placeName: String) {
        launch {
            _isViewLoading.postValue(true)
            doActionGetDetail(useCase.invoke(departmentName, placeName))
            _isViewLoading.postValue(false)
        }
    }

    fun getDetailUserPosted(userUid: String?) {
        launch {
            _isViewLoading.postValue(true)
            doActionGetUserDetail(useCaseUserPosted.invoke(userUid))
            _isViewLoading.postValue(false)
        }
    }

    private fun doActionGetUserDetail(invoke: DataResponse<User>) {
        when (invoke) {
            is DataResponse.Success -> _userPosted.value = invoke.data
            is DataResponse.NetworkError -> _onErrorMessage.postValue(invoke.error)
            is DataResponse.TimeOutServerError -> _onErrorMessage.postValue(invoke.error)
            is DataResponse.ExceptionError -> _onErrorMessage.postValue(invoke.errorCode.message)
            is DataResponse.ServerError -> _onErrorMessage.postValue(invoke.errorCode)
        }
    }

    private fun doActionGetDetail(invoke: DataResponse<Places>) {
        when (invoke) {
            is DataResponse.Success -> _place.value = invoke.data
            is DataResponse.NetworkError -> _onErrorMessage.postValue(invoke.error)
            is DataResponse.TimeOutServerError -> _onErrorMessage.postValue(invoke.error)
            is DataResponse.ExceptionError -> _onErrorMessage.postValue(invoke.errorCode.message)
            is DataResponse.ServerError -> _onErrorMessage.postValue(invoke.errorCode)
        }
    }
}