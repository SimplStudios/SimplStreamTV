package com.simplstudios.simplstream.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.simplstudios.simplstream.R

/**
 * Custom PIN input dialog that works with Leanback theme
 */
class PinInputDialog(
    private val profileName: String,
    private val error: String?,
    private val onConfirm: (String) -> Unit,
    private val onCancel: () -> Unit
) : DialogFragment() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.SimplStreamDialogTheme)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_pin_entry, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val titleText = view.findViewById<TextView>(R.id.title_text)
        val pinInput = view.findViewById<EditText>(R.id.pin_input)
        val errorText = view.findViewById<TextView>(R.id.error_text)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)
        
        titleText.text = getString(R.string.enter_pin_for, profileName)
        
        // Show error if any
        error?.let {
            errorText.text = it
            errorText.isVisible = true
        }
        
        confirmButton.setOnClickListener {
            val pin = pinInput.text.toString()
            if (pin.length == 4) {
                onConfirm(pin)
                dismiss()
            } else {
                errorText.text = "PIN must be 4 digits"
                errorText.isVisible = true
            }
        }
        
        cancelButton.setOnClickListener {
            onCancel()
            dismiss()
        }
        
        // Focus for TV navigation
        pinInput.requestFocus()
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
