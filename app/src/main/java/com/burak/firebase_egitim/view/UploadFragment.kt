package com.burak.firebase_egitim.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.burak.firebase_egitim.databinding.FragmentUploadBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class UploadFragment : Fragment() {
    private var _binding: FragmentUploadBinding? = null
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private var secilengorsel : Uri? = null
    var secilenbitmap : Bitmap? = null
    private lateinit var auth: FirebaseAuth
    private val binding get() = _binding!!
    private lateinit var storage : FirebaseStorage
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore
        registerLaunchers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.UploadButton.setOnClickListener { yukleFun(it) }
        binding.imageView.setOnClickListener { gorselSec(it) }
    }

    fun yukleFun(view: View) {
        val uuid = UUID.randomUUID()
        val gorselIsmi = "${uuid}.jpg"
        val reference = storage.reference
        val gorselReference = reference.child("images").child(gorselIsmi)
        if (secilengorsel != null){
            gorselReference.putFile(secilengorsel!!).addOnSuccessListener {uploadTask ->
                gorselReference.downloadUrl.addOnSuccessListener {uri ->
                    val downloadUrl = uri.toString()
                    //println(downloadUrl)
                    //firebase veritabanına veri kaydetmeye başlama
                    val postMap = hashMapOf<String,Any>()
                    postMap.put("downloadUrl",downloadUrl)
                    postMap.put("email",auth.currentUser!!.email!!).toString()
                    postMap.put("tarih",com.google.firebase.Timestamp.now())
                    postMap.put("yorum",binding.uploadText.text.toString())

                    db.collection("Posts").add(postMap).addOnSuccessListener {documentReference ->
                        val action = UploadFragmentDirections.actionUploadFragmentToFeedFragment()
                        view.findNavController().navigate(action)
                    }.addOnFailureListener {exception ->
                        Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                    }
                }
            }.addOnFailureListener {exception ->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }else{
            Toast.makeText(requireContext(),"Lütfen bir resim seçin",Toast.LENGTH_LONG).show()
        }
    }

    fun gorselSec(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 33+
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                // İzin yok
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)) {
                    // Kullanıcıya izin gerekçesini açıklayın
                    Snackbar.make(view, "Galeriye gitmek için izin gerekli", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin Ver", View.OnClickListener {
                            // İzin isteme
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()

                } else {
                    // İzin isteme
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                // İzin var
                // Galeriye git
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            // Android 32- için izin kontrolü ve işlemleri
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                // İzin yok
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)) {
                    // Kullanıcıya izin gerekçesini açıklayın
                    Snackbar.make(view, "Galeriye gitmek için izin gerekli", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin Ver", View.OnClickListener {
                            // İzin isteme
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()

                } else {
                    // İzin isteme
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                // İzin var
                // Galeriye git
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun registerLaunchers(){
    activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        if (result.resultCode == RESULT_OK){
            val intentFromResult = result.data
            if (intentFromResult != null){
                secilengorsel = intentFromResult.data
                try {
                    if (Build.VERSION.SDK_INT >=28){
                        val source = ImageDecoder.createSource(requireActivity().contentResolver,secilengorsel!!)
                        secilenbitmap = ImageDecoder.decodeBitmap(source)
                        binding.imageView.setImageBitmap(secilenbitmap)
                    }else{
                        secilenbitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilengorsel)
                        binding.imageView.setImageBitmap(secilenbitmap)
                    }
                }
                catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }

    }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if (result){
                //izin verildi
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                //izin verilmedi , izin reddedildi
                Toast.makeText(requireContext(),"İzin reddedildi, tekrar deneyin", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
