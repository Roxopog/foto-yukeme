package com.burak.firebase_egitim.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.burak.firebase_egitim.databinding.RecyclerRowBinding
import com.burak.firebase_egitim.model.Post
import com.squareup.picasso.Picasso

class PostAdapter(private val postList : ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.PostHolder>(){

    class PostHolder(val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.PostHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(binding)
    }

    override fun onBindViewHolder(holder: PostAdapter.PostHolder, position: Int) {
        holder.binding.editTextEmailAddress.text = postList.get(position).email
        holder.binding.yorumText.text = postList.get(position).yorum
        Picasso.get().load(postList.get(position).downloadUrl).into(holder.binding.RecyclerViewImageView)

    }

    override fun getItemCount(): Int {
        return postList.size
    }
}