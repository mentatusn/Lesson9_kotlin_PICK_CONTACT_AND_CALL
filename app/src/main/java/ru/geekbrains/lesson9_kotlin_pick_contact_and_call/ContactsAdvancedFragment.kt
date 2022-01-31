package ru.geekbrains.lesson9_kotlin_pick_contact_and_call

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.fragment.app.Fragment
import ru.geekbrains.lesson9_kotlin_pick_contact_and_call.databinding.FragmentContactsAdvancedBinding


class ContactsAdvancedFragment : Fragment() {
    companion object {
        fun newInstance() = ContactsAdvancedFragment()
    }

    private var _ui: FragmentContactsAdvancedBinding? = null
    private val ui get() = _ui!!

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permitted ->
        val read = permitted.getValue(Manifest.permission.READ_CONTACTS)
        val call = permitted.getValue(Manifest.permission.CALL_PHONE)
        when {
            read && call -> {
                getContacts()
            }
            !shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                showGoSettings()
            }
            !shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE) -> {
                showGoSettings()
            }
            else -> {
                showRatio()
            }
        }
    }

    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { checkPermission() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentContactsAdvancedBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
    }

    private fun checkPermission() {
        val resultRead =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
        val resultCall =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
        if (resultRead == PERMISSION_GRANTED && resultCall == PERMISSION_GRANTED) {
            getContacts()
        } else {
            requestPermission()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _ui = null
    }

    private fun showRatio() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.need_permission_header))
            .setMessage(getString(R.string.need_permission_text))
            .setPositiveButton(getString(R.string.need_permission_give)) { _, _ ->
                requestPermission()
            }
            .setNegativeButton(getString(R.string.need_permission_dont_give)) { dialog, _ ->
                dialog.dismiss()
                requireActivity().finish()
            }
            .create()
            .show()
    }

    private fun showGoSettings() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.need_permission_header))
            .setMessage(
                "${getString(R.string.need_permission_text)} \n" +
                        getString(R.string.need_permission_text_again)
            )
            .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                openApplicationSettings()
            }
            .setNegativeButton(getString(android.R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                requireActivity().finish()
            }
            .create()
            .show()
    }

    private fun showPhones(
        numberHome: String?,
        numberMobile: String?,
        numberWork: String?,
        numberOther: String?
    ) {
        val linear = LinearLayout(requireContext())
        linear.orientation = VERTICAL
        numberHome?.let { number ->
            linear.addView(createButton(number, getString(R.string.home)))
        }
        numberMobile?.let { number ->
            linear.addView(createButton(number, getString(R.string.mobile)))
        }
        numberWork?.let { number ->
            linear.addView(createButton(number, getString(R.string.work)))
        }
        numberOther?.let { number ->
            linear.addView(createButton(number, getString(R.string.other)))
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.call))
            .setView(linear)
            .create()
            .show()
    }

    private fun createButton(number: String, text: String): Button {
        val button = Button(requireContext())
        button.text = text
        button.setOnClickListener {
            makeCall(number)
        }
        return button
    }

    private fun makeCall(number: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        startActivity(intent)
    }

    private fun requestPermission() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CALL_PHONE
            )
        )
    }

    private fun getContacts() {
        context?.let { context: Context ->
            val cr = context.contentResolver
            val cursor = cr.query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            )

            cursor?.let { cursor: Cursor ->
                for (i in 0..cursor.count) {
                    if (cursor.moveToPosition(i)) {
                        val name =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        val contactId =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))

                        val view = TextView(context).apply {
                            text = name
                            textSize = 25f
                        }
                        view.setOnClickListener {
                            getNumberFromID(cr, contactId)
                        }
                        ui.containerForContacts.addView(view)
                    }
                }
                cursor.close()
            }
        }
    }

    private fun getNumberFromID(cr: ContentResolver, contactId: String) {
        val phones = cr.query(
            Phone.CONTENT_URI, null,
            Phone.CONTACT_ID + " = " + contactId, null, null
        )
        var number: String
        var home: String? = null
        var mobile: String? = null
        var work: String? = null
        var other: String? = null
        phones?.let { cursor ->
            while (cursor.moveToNext()) {
                number = cursor.getString(cursor.getColumnIndex(Phone.NUMBER))
                when (cursor.getInt(cursor.getColumnIndex(Phone.TYPE))) {
                    Phone.TYPE_HOME -> home = number
                    Phone.TYPE_MOBILE -> mobile = number
                    Phone.TYPE_WORK -> work = number
                    else -> other = number
                }
            }
            phones.close()
        }
        showPhones(home, mobile, work, other)
    }

    private fun openApplicationSettings() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${requireActivity().packageName}")
        )
        settingsLauncher.launch(appSettingsIntent)
    }
}