package org.cooltey.swipetocomplete

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.mlkit.nl.smartreply.SmartReply
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult
import com.google.mlkit.nl.smartreply.TextMessage
import kotlin.math.abs


class MainActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var hintText: TextView
    private val message = listOf("How are you?", "Very good! Want to go out?")
    private val message2 = listOf("Hi", "Good! How are you?")
    private var x1 = 0f
    private var x2 = 0f
    private val suggestion = mutableListOf<String>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editText)
        hintText = findViewById(R.id.hintText)

        editText.onFocusChangeListener = View.OnFocusChangeListener { _, focused ->
            if (focused) {
                smartReply()
            }
        }
        editText.addTextChangedListener {
            val text = editText.text.toString()
            val found = suggestion.find { it.startsWith(text) }
            if (!found.isNullOrEmpty()) {
                hintText.text = found
                Toast.makeText(this, "You can swipe to left to complete the sentence.", Toast.LENGTH_SHORT).show()
            } else {
                hintText.text = ""
            }
        }

        editText.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> x1 = motionEvent.x
                MotionEvent.ACTION_UP -> {
                    x2 = motionEvent.x
                    val deltaX = x2 - x1
                    if (abs(deltaX) > 150) {
                        if (!hintText.text.isNullOrEmpty()) {
                            editText.setText(hintText.text, TextView.BufferType.EDITABLE);
                        }
                        Toast.makeText(this, "Swiped to left.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            super.onTouchEvent(motionEvent)

        }
    }

    private fun createTextMessageList(): List<TextMessage> {
        val list = mutableListOf<TextMessage>()
        // TODO: maybe use more accurate timestamp
        list.addAll(message.map { TextMessage.createForRemoteUser(it, System.currentTimeMillis(), "Colton") })
        list.addAll(message2.map { TextMessage.createForLocalUser(it, System.currentTimeMillis()) })
        return list
    }

    private fun smartReply() {
        val smartReply = SmartReply.getClient()
        smartReply.suggestReplies( createTextMessageList() )
            .addOnSuccessListener { result ->
                if (result.status == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                    Toast.makeText(this, "Not supported.", Toast.LENGTH_SHORT).show()
                } else if (result.status == SmartReplySuggestionResult.STATUS_SUCCESS) {
                    Toast.makeText(this, result.suggestions.toString(), Toast.LENGTH_SHORT).show()
                    suggestion.addAll(result.suggestions.map { it.text })
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failure $it.", Toast.LENGTH_SHORT).show()
            }
    }
}