package com.deved.myepxinperu.data.server

import com.deved.data.common.DataResponse
import com.deved.data.source.RemoteDataSource
import com.deved.domain.Places
import com.deved.domain.User
import com.deved.myepxinperu.R
import com.deved.myepxinperu.ui.common.UiContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

class ThePlacesDataSource(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) : RemoteDataSource {
    override suspend fun logIn(user: String, password: String): DataResponse<String> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(user, password).await()
            DataResponse.Success(UiContext.getString(R.string.success_auth))
        } catch (e: FirebaseAuthException) {
            DataResponse.ExceptionError(e)
        } catch (e: Exception) {
            DataResponse.ExceptionError(e)
        }
    }

    override suspend fun registerUser(user:User): DataResponse<String> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(user.email,user.password).await()
            val userServer = hashMapOf<String,Any>()
            userServer["email"] = user.email
            userServer["name"] = user.name
            userServer["lastName"] = user.lastName
            firebaseFirestore.collection("Users").document(firebaseAuth.currentUser!!.uid).set(userServer).await()
            DataResponse.Success(UiContext.getString(R.string.success_registered_user))
        } catch (e: FirebaseAuthException) {
            DataResponse.ExceptionError(e)
        } catch (e: FirebaseFirestoreException) {
            DataResponse.ExceptionError(e)
        } catch (e: Exception) {
            DataResponse.ExceptionError(e)
        }
    }

    override suspend fun fetchAllPlaces(): DataResponse<List<Places>> {
        try {
            val result = firebaseFirestore.collection("MyExpInPeru").get().await()
            val places = arrayListOf<Places>()
            result.forEach {
                places.add(
                    Places(
                        it.getString("description"),
                        it.getString("description"),
                        it.getString("picture"),
                        null, null, null
                    )
                )
            }
            return DataResponse.Success(places)
        } catch (e: FirebaseFirestoreException) {
            return DataResponse.ExceptionError(e)
        } catch (e: Exception) {
            return DataResponse.ExceptionError(e)
        }
    }

    override suspend fun registerExp(place: Places): DataResponse<String> {
        return try {
            val pictureTourist = arrayListOf<String?>()
            pictureTourist.add(place.pictureOne)
            pictureTourist.add(place.pictureSecond)
            val touristDestination = hashMapOf<String,Any?>()
            touristDestination["description"] = place.description
            touristDestination["picture"] = pictureTourist

            val department = place.department?.replaceRange(0,1,place.department?.substring(0)?.toUpperCase().toString())
            firebaseFirestore.document("Departament/${department}")
                .collection("TouristDestination")
                .document(place.name!!).set(touristDestination)
            DataResponse.Success(UiContext.getString(R.string.success_registered_user))
        } catch (e: FirebaseFirestoreException) {
            DataResponse.ExceptionError(e)
        } catch (e: Exception) {
            DataResponse.ExceptionError(e)
        }
    }
}