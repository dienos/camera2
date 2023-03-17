package jth.camera2.ui.viewmodels

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import jth.camera2.domain.usecase.GetContentValuesUseCase
import jth.camera2.domain.usecase.GetFileNameUseCase
import jth.camera2.domain.usecase.GetImageUriUseCase
import jth.camera2.domain.usecase.GetRealPathFromUriUseCase
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getImageUriUseCase: GetImageUriUseCase,
    private val getFileNameUseCase: GetFileNameUseCase,
    private val getContentValuesUseCase: GetContentValuesUseCase,
    private val getRealPathFromUriUseCase: GetRealPathFromUriUseCase
) : BaseViewModel() {
    fun getImageUri(dir: File): Uri = getImageUriUseCase.invoke(dir)

    fun getContentValues(isCroppedFile: Boolean, type: String, path: String): ContentValues =
        getContentValuesUseCase.invoke(isCroppedFile, type, path)

    fun getRealPathFromUri(resolver: ContentResolver, uri: Uri): String =
        getRealPathFromUriUseCase.invoke(resolver, uri)
}