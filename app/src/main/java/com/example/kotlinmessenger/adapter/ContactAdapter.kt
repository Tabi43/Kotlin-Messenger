package com.example.kotlinmessenger.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmessenger.MessageActivity
import com.example.kotlinmessenger.Constants.AppConstants
import com.example.kotlinmessenger.UserModel
import com.example.kotlinmessenger.databinding.ContactItemLayoutBinding
import com.google.firebase.storage.FirebaseStorage
import kotlin.collections.ArrayList

class ContactAdapter(private var appContacts: ArrayList<UserModel>) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>(), Filterable {

    private var allContact: ArrayList<UserModel> = appContacts
    private val TAG = "CONTACT ADAPTER"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val contactItemLayoutBinding =
            ContactItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(contactItemLayoutBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userModel = allContact[position]
        if (userModel.image.isEmpty()) {
            FirebaseStorage.getInstance().reference.child(AppConstants.PATH + userModel.uid).downloadUrl
                .addOnSuccessListener {
                    userModel.image = it.toString()
                    Log.d(TAG, "Url image loaded successfully")
                    holder.contactItemLayoutBinding.userModel = userModel
                }
                .addOnFailureListener {
                    Log.d(TAG, "Failed to load url image -> $it")
                    holder.contactItemLayoutBinding.userModel = userModel
                }
        }else holder.contactItemLayoutBinding.userModel = userModel

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, MessageActivity::class.java)
            intent.putExtra("hisId", userModel.uid)
            intent.putExtra("hisImage", userModel.image)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return allContact.size
    }

    class ViewHolder(val contactItemLayoutBinding: ContactItemLayoutBinding) :
        RecyclerView.ViewHolder(contactItemLayoutBinding.root) {
    }

    override fun getFilter(): Filter? {
        return null
    /* return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val searchContent = constraint.toString()
                if (searchContent.isEmpty())
                    allContact = appContacts
                else {

                    val filterContact = ArrayList<UserModel>()
                    for (userModel in appContacts) {

                        if (userModel.name.toLowerCase(Locale.ROOT).trim()
                                .contains(searchContent.toLowerCase(Locale.ROOT).trim())
                        )
                            filterContact.add(userModel)
                    }
                    allContact = filterContact
                }

                val filterResults = FilterResults()
                filterResults.values = allContact
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                allContact = results?.values as ArrayList<UserModel>
                notifyDataSetChanged()
            }
        }*/
    }

}