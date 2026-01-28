package com.simplstudios.simplstream.presentation.profile

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.simplstudios.simplstream.R
import com.simplstudios.simplstream.domain.model.Profile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Profile Selection Fragment
 * Shows list of profiles with PIN protection support
 */
@AndroidEntryPoint
class ProfileSelectionFragment : Fragment() {
    
    private val viewModel: ProfileViewModel by viewModels()
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var addProfileButton: View
    private lateinit var loadingView: View
    
    private var profileAdapter: ProfileAdapter? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile_selection, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.profiles_recycler)
        addProfileButton = view.findViewById(R.id.add_profile_button)
        loadingView = view.findViewById(R.id.loading_view)
        
        setupProfileList()
        setupAddButton()
        observeState()
        observeEvents()
    }
    
    private fun setupProfileList() {
        profileAdapter = ProfileAdapter(
            onProfileClick = { profile -> viewModel.selectProfile(profile) },
            onProfileLongClick = { profile -> viewModel.setEditingProfile(profile) }
        )
        recyclerView.adapter = profileAdapter
        
        // Request focus on first item for D-pad navigation
        recyclerView.post {
            recyclerView.getChildAt(0)?.requestFocus()
        }
    }
    
    private fun setupAddButton() {
        addProfileButton.setOnClickListener {
            viewModel.showAddProfileDialog()
        }
        
        // Make focusable for D-pad
        addProfileButton.isFocusable = true
        addProfileButton.isFocusableInTouchMode = true
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    loadingView.isVisible = state.isLoading
                    recyclerView.isVisible = !state.isLoading
                    
                    profileAdapter?.submitList(state.profiles)
                    
                    // Show add profile button if less than 5 profiles
                    addProfileButton.isVisible = state.profiles.size < 5
                    
                    // Handle PIN dialog
                    state.pinDialogProfile?.let { profile ->
                        showPinDialog(profile, state.pinError)
                    }
                    
                    // Handle add dialog
                    if (state.showAddDialog) {
                        showAddProfileDialog()
                    }
                    
                    // Handle edit dialog
                    state.editingProfile?.let { profile ->
                        showEditProfileDialog(profile)
                    }
                }
            }
        }
    }
    
    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is ProfileEvent.ProfileSelected -> {
                            // Navigate to home
                            findNavController().navigate(R.id.action_profile_to_home)
                        }
                        is ProfileEvent.ProfileCreated -> {
                            Toast.makeText(requireContext(), R.string.profile_created, Toast.LENGTH_SHORT).show()
                        }
                        is ProfileEvent.ProfileUpdated -> {
                            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                        }
                        is ProfileEvent.ProfileDeleted -> {
                            Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show()
                        }
                        is ProfileEvent.LoggedOut -> {
                            // Stay on profile screen
                        }
                        is ProfileEvent.Error -> {
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
    
    private var pinDialog: PinInputDialog? = null
    
    private fun showPinDialog(profile: Profile, error: String?) {
        // Dismiss existing dialog if any
        pinDialog?.dismiss()
        
        pinDialog = PinInputDialog(
            profileName = profile.name,
            error = error,
            onConfirm = { pin -> viewModel.verifyPin(pin) },
            onCancel = { viewModel.dismissPinDialog() }
        )
        pinDialog?.show(childFragmentManager, "pin_dialog")
    }
    
    private fun showAddProfileDialog() {
        val dialog = AddProfileDialog { name, avatarIndex, pin, isKids ->
            viewModel.createProfile(name, avatarIndex, pin, isKids)
            viewModel.hideAddProfileDialog()
        }
        dialog.show(childFragmentManager, "add_profile")
    }
    
    private fun showEditProfileDialog(profile: Profile) {
        val dialog = EditProfileDialog(
            profile = profile,
            onProfileUpdated = { name, avatarIndex, pin, isKids, clearPin ->
                viewModel.updateProfileDetails(
                    profileId = profile.id,
                    name = name,
                    avatarIndex = avatarIndex,
                    newPin = pin,
                    isKidsProfile = isKids,
                    clearPin = clearPin
                )
            },
            onProfileDeleted = {
                viewModel.deleteProfile(profile.id)
            },
            onVerifyPin = { pin, onSuccess, onError ->
                viewModel.verifyPinForAction(profile.id, pin, onSuccess, onError)
            }
        )
        dialog.show(childFragmentManager, "edit_profile")
        viewModel.setEditingProfile(null)
    }
    
    private fun showDeleteConfirmation(profile: Profile) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.SimplStreamDialogTheme)
            .setTitle("Delete Profile")
            .setMessage("Are you sure you want to delete ${profile.name}? This will remove all watch history and watchlist.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteProfile(profile.id)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        profileAdapter = null
    }
}

/**
 * Profile Adapter for RecyclerView
 */
class ProfileAdapter(
    private val onProfileClick: (Profile) -> Unit,
    private val onProfileLongClick: (Profile) -> Unit
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {
    
    private var profiles: List<Profile> = emptyList()
    
    fun submitList(newProfiles: List<Profile>) {
        profiles = newProfiles
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(profiles[position])
    }
    
    override fun getItemCount(): Int = profiles.size
    
    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarView: View = itemView.findViewById(R.id.avatar)
        private val initialText: TextView = itemView.findViewById(R.id.avatar_initial)
        private val nameText: TextView = itemView.findViewById(R.id.profile_name)
        private val pinIcon: View = itemView.findViewById(R.id.pin_icon)
        private val kidsLabel: View = itemView.findViewById(R.id.kids_label)
        
        private var currentProfile: Profile? = null
        
        init {
            // Focus handling for TV - must be set in init
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = false
            itemView.isClickable = true
            
            // Click handler
            itemView.setOnClickListener { 
                currentProfile?.let { onProfileClick(it) }
            }
            
            // Long click handler
            itemView.setOnLongClickListener { 
                currentProfile?.let { onProfileLongClick(it) }
                true
            }
            
            // Explicit D-pad ENTER/CENTER key handling for Android TV
            itemView.setOnKeyListener { _, keyCode, event ->
                if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                        android.view.KeyEvent.KEYCODE_ENTER,
                        android.view.KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            currentProfile?.let { onProfileClick(it) }
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            
            // Focus change animation
            itemView.setOnFocusChangeListener { _, hasFocus ->
                val scale = if (hasFocus) 1.1f else 1.0f
                itemView.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(150)
                    .start()
                itemView.isSelected = hasFocus
            }
        }
        
        fun bind(profile: Profile) {
            currentProfile = profile
            
            // Set avatar background color
            val avatarDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(profile.avatarColor)
            }
            avatarView.background = avatarDrawable
            
            // Set initial
            initialText.text = profile.initial
            
            // Set name
            nameText.text = profile.name
            
            // Show/hide icons
            pinIcon.isVisible = profile.hasPin
            kidsLabel.isVisible = profile.isKidsProfile
        }
    }
}
