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
 * Dialog for creating a new profile
 */
class AddProfileDialog(
    private val onProfileCreated: (name: String, avatarIndex: Int, pin: String?, isKids: Boolean) -> Unit
) : DialogFragment() {
    
    private var selectedAvatarIndex = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.SimplStreamDialogTheme)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_add_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            val nameInput = view.findViewById<EditText>(R.id.profile_name_input)
            val pinInput = view.findViewById<EditText>(R.id.pin_input)
            val kidsCheckbox = view.findViewById<CheckBox>(R.id.kids_checkbox)
            val avatarRecycler = view.findViewById<RecyclerView>(R.id.avatar_recycler)
            val createButton = view.findViewById<Button>(R.id.create_button)
            val cancelButton = view.findViewById<Button>(R.id.cancel_button)
            val errorText = view.findViewById<TextView>(R.id.error_text)
            
            // Setup avatar selection
            avatarRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            val avatarAdapter = AvatarAdapter { index ->
                selectedAvatarIndex = index
            }
            avatarRecycler.adapter = avatarAdapter
            
            // Validation
            nameInput.doAfterTextChanged {
                val name = it?.toString() ?: ""
                createButton.isEnabled = name.length >= 2
                errorText.visibility = View.GONE
            }
            
            // Create button
            createButton.setOnClickListener {
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
                
                onProfileCreated(name, selectedAvatarIndex, pin, isKids)
                dismiss()
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
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}

/**
 * Avatar selection adapter
 */
class AvatarAdapter(
    private val onAvatarSelected: (Int) -> Unit
) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {
    
    private var selectedIndex = 0
    
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
