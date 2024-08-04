package com.burak.firebase_egitim.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.burak.firebase_egitim.R
import com.burak.firebase_egitim.adapter.PostAdapter
import com.burak.firebase_egitim.databinding.FragmentFeedBinding
import com.burak.firebase_egitim.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FeedFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    private var _binding: FragmentFeedBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    val postList : ArrayList<Post> = arrayListOf()
    private var adapter : PostAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var auth = Firebase.auth
        db = Firebase.firestore

    }
    fun floatingButtonTıklandı(view: View){
        val popup = PopupMenu(requireContext(),binding.floatingActionButton)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.my_popup_menu,popup.menu)
        popup.setOnMenuItemClickListener(this)
        popup.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val view = binding.root
        return view}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener {floatingButtonTıklandı(it)}
        val popup = PopupMenu(requireContext(),binding.floatingActionButton)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.my_popup_menu,popup.menu)
        popup.setOnMenuItemClickListener(this)
        fireStoreVerileriAl()
        adapter = PostAdapter(postList)
        binding.FeedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.FeedRecyclerView.adapter = adapter

    }
    private fun fireStoreVerileriAl(){
        db.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if(error != null){
                Toast.makeText(requireContext(),error.localizedMessage,Toast.LENGTH_LONG).show()
            }
            else{
                if(value != null){
                    if(!value.isEmpty){
                        val documents = value.documents
                        postList.clear()
                        for (document in documents){
                            val userEmail = document.get("email") as String
                            val userComment = document.get("yorum") as String
                            val downloadUrl = document.get("downloadUrl") as String

                            val post = Post(userEmail,userComment,downloadUrl)
                            postList.add(post)

                        }
                        //adaptörü uyar yeni veri geldi
                        adapter?.notifyDataSetChanged()
                    }
                }
                }
            }

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.UploadItem){
            //foto yükleme işine gidiyor
            val action = FeedFragmentDirections.actionFeedFragmentToUploadFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
        else if(item?.itemId == R.id.çıkış){
            //giriş menüsüne geri dönüyor
            auth.signOut()
            val action = FeedFragmentDirections.actionFeedFragmentToUserFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
        //sırf dönsün deyü
        return true
    }

}