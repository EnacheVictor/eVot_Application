package com.victor.evotapplication.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.victor.evotapplication.databinding.FragmentChatBinding
import com.victor.evotapplication.models.Chat
import com.victor.evotapplication.adapters.ChatAdapter

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val messages = mutableListOf<Chat>()
    private lateinit var adapter: ChatAdapter
    private var associationId: String? = null

    private val mediaPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadMedia(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        associationId = arguments?.getString("associationId")

        setupRecyclerView()
        setupListeners()
        listenForMessages()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages)
        binding.messagesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.messagesRecycler.adapter = adapter
    }

    private fun setupListeners() {
        binding.sendBtn.setOnClickListener {
            val text = binding.messageInput.text.toString()
            if (text.isNotEmpty()) {
                sendTextMessage(text)
                binding.messageInput.setText("")
            }
        }

        binding.attachBtn.setOnClickListener {
            mediaPicker.launch("image/* video/*")
        }
    }

    private fun sendTextMessage(text: String) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("user-type").document(uid).get().addOnSuccessListener { userDoc ->
            val name = userDoc.getString("username") ?: "User-type"

            val message = Chat(
                senderId = uid,
                senderName = name,
                text = text,
                timestamp = System.currentTimeMillis()
            )

            db.collection("chats")
                .document(associationId ?: return@addOnSuccessListener)
                .collection("messages")
                .add(message)
        }
    }

    private fun uploadMedia(uri: Uri) {
        val fileType = requireContext().contentResolver.getType(uri) ?: return
        val isImage = fileType.startsWith("image/")
        val isVideo = fileType.startsWith("video/")
        val extension = if (isImage) "image" else if (isVideo) "video" else return

        val ref = storage.reference.child("chat_media/${System.currentTimeMillis()}")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    sendMediaMessage(downloadUrl.toString(), extension)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendMediaMessage(mediaUrl: String, mediaType: String) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("user-type").document(uid).get().addOnSuccessListener { userDoc ->
            val name = userDoc.getString("username") ?: "user-type"

            val message = Chat(
                senderId = uid,
                senderName = name,
                mediaUrl = mediaUrl,
                mediaType = mediaType,
                timestamp = System.currentTimeMillis()
            )

            db.collection("chats")
                .document(associationId ?: return@addOnSuccessListener)
                .collection("messages")
                .add(message)
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Nu s-a putut obține numele utilizatorului", Toast.LENGTH_SHORT).show()
        }
    }


    private fun listenForMessages() {
        db.collection("chats")
            .document(associationId ?: return)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, _ ->
                messages.clear()
                snapshots?.forEach {
                    val msg = it.toObject(Chat::class.java)
                    messages.add(msg)
                }
                adapter.notifyDataSetChanged()
                binding.messagesRecycler.scrollToPosition(messages.size - 1)
            }
    }
}
