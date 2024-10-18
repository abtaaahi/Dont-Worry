package com.abtahiapp.dontworry.utils

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.TextView
import com.abtahiapp.dontworry.R

class InfoBottomSheetDialog(context: Context, private val message: String) : BottomSheetDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_bottom_sheet_dialog)

        val textView = findViewById<TextView>(R.id.info_bottom_sheet_text)
        textView?.text = message
    }
}