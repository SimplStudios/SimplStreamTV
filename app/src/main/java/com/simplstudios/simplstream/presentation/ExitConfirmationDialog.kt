package com.simplstudios.simplstream.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.simplstudios.simplstream.R

/**
 * Dialog shown when user tries to exit the app
 */
class ExitConfirmationDialog(
    private val onConfirm: () -> Unit,
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
        return inflater.inflate(R.layout.dialog_exit_confirmation, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val titleText = view.findViewById<TextView>(R.id.title_text)
        val messageText = view.findViewById<TextView>(R.id.message_text)
        val yesButton = view.findViewById<Button>(R.id.yes_button)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)
        
        titleText.text = getString(R.string.exit_app_title)
        messageText.text = getString(R.string.exit_app_message)
        
        yesButton.setOnClickListener {
            onConfirm()
            dismiss()
        }
        
        cancelButton.setOnClickListener {
            onCancel()
            dismiss()
        }
        
        // Focus cancel button by default (safer option)
        cancelButton.requestFocus()
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
