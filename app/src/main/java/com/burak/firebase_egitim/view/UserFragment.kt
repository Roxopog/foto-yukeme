package com.burak.firebase_egitim.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.burak.firebase_egitim.databinding.FragmentUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class UserFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentUserBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.kayitButton.setOnClickListener { kayitol(it) }
        binding.girisButton.setOnClickListener { girisyap(it) }
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val action = UserFragmentDirections.actionUserFragmentToFeedFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }
//kullanıcıyı kaydetme
    fun kayitol(view: View) {
        val email = binding.editTextTextEmailAddress.text.toString()
        val sifre = binding.editTextTextPassword.text.toString()
        if (email.equals("") || sifre.equals("")) {
            Toast.makeText(requireContext(), "şifre veya parola boş olamaz", Toast.LENGTH_LONG).show()
            return
        } else {
            auth.createUserWithEmailAndPassword(email, sifre)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val action = UserFragmentDirections.actionUserFragmentToFeedFragment()
                        Navigation.findNavController(view).navigate(action)
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG)
                        .show()
                }


        }
    }
    //kayıtlı kullanıcı giriş yapma
        fun girisyap(view: View) {
            val email = binding.editTextTextEmailAddress.text.toString()
            val sifre = binding.editTextTextPassword.text.toString()
        if (email.equals("") || sifre.equals("")) {
            Toast.makeText(requireContext(), "şifre veya parola boş olamaz", Toast.LENGTH_LONG).show()
            return
        }
        else{
            auth.signInWithEmailAndPassword(email, sifre)
                .addOnSuccessListener {
                    val action = UserFragmentDirections.actionUserFragmentToFeedFragment()
                    Navigation.findNavController(view).navigate(action)

                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG)
                        .show()
                }
        }
        }
        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }

