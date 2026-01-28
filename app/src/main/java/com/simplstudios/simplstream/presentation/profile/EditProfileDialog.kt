package com.simplstudios.simplstream.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simplstudios.simplstream.R
import com.simplstudios.simplstream.domain.model.Profile

/**
 * Dialog for editing an existing profile
 */
class EditProfileDialog(
    private val profile: Profile,
    private val onProfileUpdated: (name: String, avatarIndex: Int, pin: String?, isKids: Boolean, clearPin: Boolean) -> Unit,
    private val onProfileDeleted: () -> Unit,
    private val onVerifyPin: (pin: String, onSuccess: () -> Unit, onError: () -> Unit) -> Unit
) : DialogFragment() {
    
    private var selectedAvatarIndex = profile.avatarIndex
    private var clearExistingPin = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.SimplStreamDialogTheme)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_edit_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            val titleText = view.findViewById<TextView>(R.id.dialog_title)
            val nameInput = view.findViewById<EditText>(R.id.profile_name_input)
            val pinInput = view.findViewById<EditText>(R.id.pin_input)
            val pinHint = view.findViewById<TextView>(R.id.pin_hint)
            val removePinCheckbox = view.findViewById<CheckBox>(R.id.remove_pin_checkbox)
            val kidsCheckbox = view.findViewById<CheckBox>(R.id.kids_checkbox)
            val avatarRecycler = view.findViewById<RecyclerView>(R.id.avatar_recycler)
            val saveButton = view.findViewById<Button>(R.id.save_button)
            val deleteButton = view.findViewById<Button>(R.id.delete_button)
            val cancelButton = view.findViewById<Button>(R.id.cancel_button)
            val errorText = view.findViewById<TextView>(R.id.error_text)
            
            // Set title
            titleText.text = "Edit Profile"
            
            // Pre-fill existing values
            nameInput.setText(profile.name)
            kidsCheckbox.isChecked = profile.isKidsProfile
            
            // PIN handling
            if (profile.hasPin) {
                pinHint.text = "Enter new PIN to change (leave empty to keep current)"
                removePinCheckbox.visibility = View.VISIBLE
            } else {
                pinHint.text = "Optional: 4-digit PIN for profile lock"
                removePinCheckbox.visibility = View.GONE
            }
            
            removePinCheckbox.setOnCheckedChangeListener { _, isChecked ->
                clearExistingPin = isChecked
                pinInput.isEnabled = !isChecked
                if (isChecked) {
                    pinInput.text?.clear()
                }
            }
            
            // Setup avatar selection
            avatarRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            val avatarAdapter = EditAvatarAdapter(profile.avatarIndex) { index ->
                selectedAvatarIndex = index
            }
            avatarRecycler.adapter = avatarAdapter
            
            // Scroll to selected avatar
            avatarRecycler.post {
                avatarRecycler.scrollToPosition(profile.avatarIndex)
            }
            
            // Validation
            nameInput.doAfterTextChanged {
                val name = it?.toString() ?: ""
                saveButton.isEnabled = name.length >= 2
                errorText.visibility = View.GONE
            }
            
            // Save button
            saveButton.setOnClickListener {
                val name = nameInput.text.toString().trim()
                val pin = pinInput.text.toString().takeIf { it.length == 4 }
                val isKids = kidsCheckbox.isChecked
                
                if (name.length < 2) {
                    errorText.text = "Name must be at least 2 characters"
                    errorText.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                
                if (pinInput.text.isNotEmpty() && pinInput.text.length != 4) {
                    errorText.text = "PIN must be 4 digits"
                    errorText.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                
                onProfileUpdated(name, selectedAvatarIndex, pin, isKids, clearExistingPin)
                dismiss()
            }
            
            // Delete button
            deleteButton.setOnClickListener {
                if (profile.hasPin) {
                    // Require PIN verification before deletion
                    showPinVerificationForDelete()
                } else {
                    showDeleteConfirmation()
                }
            }
            
            // Cancel button
            cancelButton.setOnClickListener {
                dismiss()
            }
            
            // Focus for TV
            nameInput.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
            dismiss()
        }
    }
    
    private fun showPinVerificationForDelete() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_pin_verify_delete, null)
        
        val pinInput = dialogView.findViewById<EditText>(R.id.pin_input)
        val errorText = dialogView.findViewById<TextView>(R.id.error_text)
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.SimplStreamDialogTheme)
            .setTitle("Enter PIN to Delete")
            .setMessage("Enter the profile PIN to confirm deletion of ${profile.name}")
            .setView(dialogView)
            .setPositiveButton("Verify & Delete", null) // Set to null, we'll override
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val enteredPin = pinInput.text.toString()
                if (enteredPin.length != 4) {
                    errorText.text = "PIN must be 4 digits"
                    errorText.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                
                onVerifyPin(
                    enteredPin,
                    {
                        dialog.dismiss()
                        showDeleteConfirmation()
                    },
                    {
                        errorText.text = "Incorrect PIN"
                        errorText.visibility = View.VISIBLE
                        pinInput.text?.clear()
                    }
                )
            }
            pinInput.requestFocus()
        }
        
        dialog.show()
    }
    
    private fun showDeleteConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.SimplStreamDialogTheme)
            .setTitle("Delete Profile")
            .setMessage("Are you sure you want to delete ${profile.name}?\n\nThis will permanently remove all watch history and watchlist for this profile.")
            .setPositiveButton("Delete") { _, _ ->
                onProfileDeleted()
                dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}

/**
 * Avatar selection adapter for edit dialog
 */
class EditAvatarAdapter(
    initialSelectedIndex: Int,
    private val onAvatarSelected: (Int) -> Unit
) : RecyclerView.Adapter<EditAvatarAdapter.AvatarViewHolder>() {
    
    private var selectedIndex = initialSelectedIndex
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_avatar, parent, false)
        return AvatarViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        holder.bind(position, position == selectedIndex)
    }
    
    override fun getItemCount(): Int = Profile.AVATAR_COLORS.size
    
    inner class AvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarView: View = itemView.findViewById(R.id.avatar_color)
        private val checkView: View = itemView.findViewById(R.id.avatar_check)
        
        fun bind(index: Int, isSelected: Boolean) {
            val color = Profile.AVATAR_COLORS[index]
            
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(color)
            }
            avatarView.background = drawable
            
            checkView.visibility = if (isSelected) View.VISIBLE else View.GONE
            
            itemView.setOnClickListener {
                val oldIndex = selectedIndex
                selectedIndex = index
                notifyItemChanged(oldIndex)
                notifyItemChanged(index)
                onAvatarSelected(index)
            }
            
            // Focus handling
            itemView.isFocusable = true
            itemView.setOnFocusChangeListener { _, hasFocus ->
                val scale = if (hasFocus) 1.2f else 1.0f
                itemView.animate().scaleX(scale).scaleY(scale).setDuration(100).start()
            }
        }
    }
}
