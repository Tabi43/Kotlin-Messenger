package com.example.kotlinmessenger

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.*

class LanguageManager(sourceLanguage: String) {

    var foreignLanguage: String = ""
    var nativeLanguage: String? = null
    var modelAvailable: Boolean = false
    var translator: Translator? = null
    val TAG = "LANGUAGE MANAGER"

    init {
        setSourceLanguage(sourceLanguage)
        createTranslator()
    }

    fun identifyLanguage(message: String, callback: (String) -> Unit) {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(message)
            .addOnSuccessListener {
                callback(it)
                Log.d(
                    TAG,
                    "from language: ${TranslateLanguage.fromLanguageTag(it)} invece: ${TranslateLanguage.ENGLISH}"
                )
                foreignLanguage = TranslateLanguage.fromLanguageTag(it)!!
            }
            .addOnFailureListener {
                callback("und")
            }
        /* [Come si usa]
        *  lManager.identifyLanguage("ciao sono Mario") {
            Toast.makeText(this,"Lingua rilevata: $it",Toast.LENGTH_SHORT).show()
        }*/
    }

    fun setSourceLanguage(language: String) {
        foreignLanguage = language
    }

    fun createTranslator() {
        Log.e(TAG, "String: $foreignLanguage")
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(foreignLanguage)
            .setTargetLanguage(TranslateLanguage.ITALIAN)
            .build()
        translator = Translation.getClient(options)
        RemoteModelManager.getInstance().getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener {
                it.forEach {
                    if (it.language == foreignLanguage) modelAvailable = true
                    else checkConditions()
                }
            }
    }

    fun checkConditions() {
        val conditions = DownloadConditions.Builder()
            .build()
        translator!!.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                Log.i(TAG, "Model correctly downloaded")
                modelAvailable = true
            }
            .addOnFailureListener {
                Log.e(TAG, "Error : model not correctly downloaded -> $it")
                modelAvailable = false
            }
    }

    fun translate(message: String, callback: (String?) -> Unit) {
        Log.e(TAG, "dio $modelAvailable")
        if (!modelAvailable) {
            checkConditions()
            callback(null)
        }
        translator!!.translate(message)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully translated! original: $message to: $it")
                callback(it)
            }
            .addOnFailureListener {
                Log.e(TAG, "Error during translation -> $it")
            }
    }


}