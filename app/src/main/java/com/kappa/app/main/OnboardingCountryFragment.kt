package com.kappa.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.kappa.app.R
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import java.io.IOException
import java.util.Locale

@AndroidEntryPoint
class OnboardingCountryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_country, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val countryInput = view.findViewById<MaterialAutoCompleteTextView>(R.id.input_onboarding_country)
        val errorText = view.findViewById<TextView>(R.id.text_onboarding_country_error)
        val nextButton = view.findViewById<MaterialButton>(R.id.button_onboarding_country_next)

        val countries = loadCountries()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, countries)
        countryInput.setAdapter(adapter)

        nextButton.setOnClickListener {
            val rawCountry = countryInput.text?.toString()?.trim().orEmpty()
            val country = normalizeCountryName(rawCountry)
            if (country.isBlank()) {
                errorText.text = "Please select your country"
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }
            errorText.visibility = View.GONE
            findNavController().currentBackStackEntry?.savedStateHandle?.set("selected_country", country)
            findNavController().navigate(R.id.navigation_onboarding_profile)
        }
    }

    private fun loadCountries(): List<String> {
        val localeCountries = runCatching {
            Locale.getISOCountries()
                .mapNotNull { code ->
                    val name = Locale("", code).getDisplayCountry(Locale.ENGLISH)
                    if (name.isBlank()) null else CountryOption(
                        code = code,
                        name = name,
                        displayName = "${flagEmoji(code)} $name"
                    )
                }
                .sortedBy { it.name }
                .map { it.displayName }
        }.getOrNull()

        if (!localeCountries.isNullOrEmpty()) {
            return localeCountries
        }

        val context = context ?: return resources.getStringArray(R.array.country_list).toList()
        return try {
            context.assets.open("countries.json").bufferedReader().use { reader ->
                val json = reader.readText()
                val array = JSONArray(json)
                List(array.length()) { index ->
                    array.optString(index)
                }.filter { it.isNotBlank() }
            }.ifEmpty {
                resources.getStringArray(R.array.country_list).toList()
            }
        } catch (error: IOException) {
            resources.getStringArray(R.array.country_list).toList()
        }
    }

    private fun normalizeCountryName(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return trimmed
        val codePoints = trimmed.codePoints().toArray()
        if (codePoints.size >= 2 &&
            isRegionalIndicator(codePoints[0]) &&
            isRegionalIndicator(codePoints[1])
        ) {
            return String(codePoints, 2, codePoints.size - 2).trim()
        }
        return trimmed
    }

    private fun isRegionalIndicator(codePoint: Int): Boolean {
        return codePoint in 0x1F1E6..0x1F1FF
    }

    private fun flagEmoji(countryCode: String): String {
        val code = countryCode.trim().uppercase(Locale.ENGLISH)
        if (code.length != 2) return ""
        val base = 0x1F1E6
        val first = base + (code[0] - 'A')
        val second = base + (code[1] - 'A')
        return String(Character.toChars(first)) + String(Character.toChars(second))
    }

    private data class CountryOption(
        val code: String,
        val name: String,
        val displayName: String
    )
}
