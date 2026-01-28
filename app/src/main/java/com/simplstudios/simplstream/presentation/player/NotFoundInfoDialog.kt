package com.simplstudios.simplstream.presentation.player

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.simplstudios.simplstream.R

/**
 * Dialog shown when user clicks "Show not found?" button
 * Explains why content might not be available and suggests switching servers
 */
class NotFoundInfoDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_not_found_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val closeButton = view.findViewById<Button>(R.id.close_button)
        closeButton.setOnClickListener {
            dismiss()
        }
        closeButton.requestFocus()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.apply {
                setBackgroundDrawableResource(android.R.color.transparent)
                setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                setGravity(Gravity.CENTER)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.8).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }

    companion object {
        const val TAG = "NotFoundInfoDialog"

        fun newInstance(): NotFoundInfoDialog {
            return NotFoundInfoDialog()
        }
    }
}
