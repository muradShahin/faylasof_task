package com.murad.faylasoof.utils

import android.text.Editable
import android.text.TextWatcher

class MyTextWatcher(val myWatcher: MyWatcher) : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        myWatcher.onTextChanged(s, start, before, count)
    }
}