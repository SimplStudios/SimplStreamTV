package com.simplstudios.simplstream.presentation.settings

import android.app.AlertDialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.simplstudios.simplstream.R
import com.simplstudios.simplstream.domain.model.Profile
import com.simplstudios.simplstream.presentation.profile.ProfileViewModel
import com.simplstudios.simplstream.presentation.profile.ProfileEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Settings Fragment - Profile management screen
 */
@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: ProfileViewModel by viewModels()

    // Views
    private lateinit var profileAvatar: View
    private lateinit var profileNameDisplay: TextView
    private lateinit var kidsBadge: TextView
    private lateinit var pinStatusContainer: View
    private lateinit var nameInput: EditText
    private lateinit var colorSelector: LinearLayout
    private lateinit var kidsModeSwitch: SwitchCompat
    private lateinit var kidsModeRow: View
    private lateinit var changePinRow: View
    private lateinit var pinTitle: TextView
    private lateinit var pinSubtitle: TextView
    private lateinit var switchProfileRow: View
    private lateinit var signOutRow: View
    private lateinit var deleteProfileRow: View
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button

    // State
    private var currentProfile: Profile? = null
    private var selectedColorIndex: Int = 0
    private var hasPin: Boolean = false
    private var isViewsBound = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            bindViews(view)
            isViewsBound = true
            setupColorSelector()
            setupClickListeners()
            observeState()
            observeEvents()
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("SettingsFragment", "Error in onViewCreated: ${e.message}", e)
        }
    }

    private fun bindViews(view: View) {
        profileAvatar = view.findViewById(R.id.profile_avatar) ?: throw IllegalStateException("profile_avatar not found")
        profileNameDisplay = view.findViewById(R.id.profile_name_display) ?: throw IllegalStateException("profile_name_display not found")
        kidsBadge = view.findViewById(R.id.kids_badge) ?: throw IllegalStateException("kids_badge not found")
        pinStatusContainer = view.findViewById(R.id.pin_status_container) ?: throw IllegalStateException("pin_status_container not found")
        nameInput = view.findViewById(R.id.name_input) ?: throw IllegalStateException("name_input not found")
        colorSelector = view.findViewById(R.id.color_selector) ?: throw IllegalStateException("color_selector not found")
        kidsModeSwitch = view.findViewById(R.id.kids_mode_switch) ?: throw IllegalStateException("kids_mode_switch not found")
        kidsModeRow = view.findViewById(R.id.kids_mode_row) ?: throw IllegalStateException("kids_mode_row not found")
        changePinRow = view.findViewById(R.id.change_pin_row) ?: throw IllegalStateException("change_pin_row not found")
        pinTitle = view.findViewById(R.id.pin_title) ?: throw IllegalStateException("pin_title not found")
        pinSubtitle = view.findViewById(R.id.pin_subtitle) ?: throw IllegalStateException("pin_subtitle not found")
        switchProfileRow = view.findViewById(R.id.switch_profile_row) ?: throw IllegalStateException("switch_profile_row not found")
        signOutRow = view.findViewById(R.id.sign_out_row) ?: throw IllegalStateException("sign_out_row not found")
        deleteProfileRow = view.findViewById(R.id.delete_profile_row) ?: throw IllegalStateException("delete_profile_row not found")
        cancelButton = view.findViewById(R.id.cancel_button) ?: throw IllegalStateException("cancel_button not found")
        saveButton = view.findViewById(R.id.save_button) ?: throw IllegalStateException("save_button not found")
    }

    private fun setupColorSelector() {
        colorSelector.removeAllViews()
        
        Profile.AVATAR_COLORS.forEachIndexed { index, color ->
            val colorView = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(48.dpToPx(), 48.dpToPx()).apply {
                    marginEnd = 12.dpToPx()
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                }
                isFocusable = true
                isFocusableInTouchMode = true
                
                setOnClickListener {
                    selectColor(index)
                }
                
                setOnFocusChangeListener { v, hasFocus ->
                    v.scaleX = if (hasFocus) 1.2f else 1.0f
                    v.scaleY = if (hasFocus) 1.2f else 1.0f
                }
            }
            colorSelector.addView(colorView)
        }
    }

    private fun selectColor(index: Int) {
        selectedColorIndex = index
        if (index < Profile.AVATAR_COLORS.size) {
            updateAvatarColor(Profile.AVATAR_COLORS[index])
        }
        
        // Update selection indicators
        for (i in 0 until colorSelector.childCount) {
            val child = colorSelector.getChildAt(i)
            val isSelected = i == index
            (child.background as? GradientDrawable)?.apply {
                setStroke(if (isSelected) 4.dpToPx() else 0, android.graphics.Color.WHITE)
            }
        }
    }

    private fun updateAvatarColor(color: Int) {
        (profileAvatar.background as? GradientDrawable)?.setColor(color)
    }

    private fun setupClickListeners() {
        // Kids mode row
        kidsModeRow.setOnClickListener {
            kidsModeSwitch.isChecked = !kidsModeSwitch.isChecked
        }
        
        kidsModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            kidsBadge.isVisible = isChecked
        }

        // Change PIN
        changePinRow.setOnClickListener {
            showChangePinDialog()
        }

        // Switch Profile
        switchProfileRow.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_profile)
        }

        // Sign Out
        signOutRow.setOnClickListener {
            showSignOutConfirmation()
        }

        // Delete Profile
        deleteProfileRow.setOnClickListener {
            handleDeleteProfile()
        }

        // Cancel
        cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Save
        saveButton.setOnClickListener {
            saveChanges()
        }

        // Name input updates preview
        nameInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                profileNameDisplay.text = nameInput.text.toString().ifEmpty { "Profile" }
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (!isViewsBound) return@collect
                    try {
                        // Get current profile
                        val profile = state.currentProfile ?: state.profiles.firstOrNull()
                        profile?.let { loadProfile(it) }
                    } catch (e: Exception) {
                        android.util.Log.e("SettingsFragment", "Error in observeState: ${e.message}", e)
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
                        is ProfileEvent.ProfileUpdated -> {
                            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                        is ProfileEvent.ProfileDeleted -> {
                            Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_settings_to_profile)
                        }
                        is ProfileEvent.Error -> {
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun loadProfile(profile: Profile) {
        if (currentProfile?.id == profile.id) return
        currentProfile = profile
        
        // Set name
        nameInput.setText(profile.name)
        profileNameDisplay.text = profile.name
        
        // Set color
        selectedColorIndex = profile.avatarIndex
        selectColor(selectedColorIndex)
        
        // Set kids mode
        kidsModeSwitch.isChecked = profile.isKidsProfile
        kidsBadge.isVisible = profile.isKidsProfile
        
        // Set PIN status
        hasPin = profile.hasPin
        pinStatusContainer.isVisible = hasPin
        pinTitle.text = if (hasPin) "Change PIN" else "Set PIN"
        pinSubtitle.text = if (hasPin) "Update your profile PIN" else "Protect this profile with a PIN"
    }

    private fun showChangePinDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_pin, null)
        val currentPinInput = dialogView.findViewById<EditText>(R.id.current_pin_input)
        val newPinInput = dialogView.findViewById<EditText>(R.id.new_pin_input)
        val confirmPinInput = dialogView.findViewById<EditText>(R.id.confirm_pin_input)
        val currentPinContainer = dialogView.findViewById<View>(R.id.current_pin_container)
        val errorText = dialogView.findViewById<TextView>(R.id.error_text)
        val removePinButton = dialogView.findViewById<View>(R.id.remove_pin_button)

        // Show current PIN field only if profile has PIN
        currentPinContainer.isVisible = hasPin
        removePinButton.isVisible = hasPin

        val dialog = AlertDialog.Builder(requireContext(), R.style.SimplStreamDialogTheme)
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        removePinButton.setOnClickListener {
            val enteredCurrentPin = currentPinInput.text.toString()
            if (hasPin) {
                currentProfile?.let { profile ->
                    viewModel.verifyPinForAction(
                        profile.id,
                        enteredCurrentPin,
                        {
                            viewModel.updateProfileDetails(
                                profileId = profile.id,
                                name = nameInput.text.toString(),
                                avatarIndex = selectedColorIndex,
                                newPin = null,
                                isKidsProfile = kidsModeSwitch.isChecked,
                                clearPin = true
                            )
                            dialog.dismiss()
                        },
                        {
                            errorText.text = "Current PIN is incorrect"
                            errorText.isVisible = true
                        }
                    )
                }
            }
        }

        dialogView.findViewById<View>(R.id.save_button).setOnClickListener {
            errorText.isVisible = false
            
            val newPin = newPinInput.text.toString()
            val confirmPin = confirmPinInput.text.toString()

            if (newPin.length != 4) {
                errorText.text = "PIN must be 4 digits"
                errorText.isVisible = true
                return@setOnClickListener
            }

            if (newPin != confirmPin) {
                errorText.text = "PINs do not match"
                errorText.isVisible = true
                return@setOnClickListener
            }

            if (hasPin) {
                val enteredCurrentPin = currentPinInput.text.toString()
                currentProfile?.let { profile ->
                    viewModel.verifyPinForAction(
                        profile.id,
                        enteredCurrentPin,
                        {
                            viewModel.updateProfileDetails(
                                profileId = profile.id,
                                name = nameInput.text.toString(),
                                avatarIndex = selectedColorIndex,
                                newPin = newPin,
                                isKidsProfile = kidsModeSwitch.isChecked,
                                clearPin = false
                            )
                            dialog.dismiss()
                        },
                        {
                            errorText.text = "Current PIN is incorrect"
                            errorText.isVisible = true
                        }
                    )
                }
            } else {
                currentProfile?.let { profile ->
                    viewModel.updateProfileDetails(
                        profileId = profile.id,
                        name = nameInput.text.toString(),
                        avatarIndex = selectedColorIndex,
                        newPin = newPin,
                        isKidsProfile = kidsModeSwitch.isChecked,
                        clearPin = false
                    )
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showSignOutConfirmation() {
        AlertDialog.Builder(requireContext(), R.style.SimplStreamDialogTheme)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out of this profile?")
            .setPositiveButton("Sign Out") { _, _ ->
                findNavController().navigate(R.id.action_settings_to_profile)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleDeleteProfile() {
        val profile = currentProfile ?: return
        
        if (profile.hasPin) {
            showDeletePinVerification(profile)
        } else {
            showDeleteConfirmation(profile)
        }
    }

    private fun showDeletePinVerification(profile: Profile) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pin_verify_delete, null)
        val pinInput = dialogView.findViewById<EditText>(R.id.pin_input)
        val errorText = dialogView.findViewById<TextView>(R.id.error_text)

        val dialog = AlertDialog.Builder(requireContext(), R.style.SimplStreamDialogTheme)
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.verify_button).setOnClickListener {
            val enteredPin = pinInput.text.toString()
            
            if (enteredPin.length != 4) {
                errorText.text = "PIN must be 4 digits"
                errorText.isVisible = true
                return@setOnClickListener
            }

            viewModel.verifyPinForAction(
                profile.id,
                enteredPin,
                {
                    dialog.dismiss()
                    showDeleteConfirmation(profile)
                },
                {
                    errorText.text = "Incorrect PIN"
                    errorText.isVisible = true
                    pinInput.text?.clear()
                }
            )
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(profile: Profile) {
        AlertDialog.Builder(requireContext(), R.style.SimplStreamDialogTheme)
            .setTitle("Delete Profile")
            .setMessage("Are you sure you want to delete \"${profile.name}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteProfile(profile)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveChanges() {
        val profile = currentProfile ?: return
        val name = nameInput.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateProfileDetails(
            profileId = profile.id,
            name = name,
            avatarIndex = selectedColorIndex,
            newPin = null,
            isKidsProfile = kidsModeSwitch.isChecked,
            clearPin = false
        )
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
